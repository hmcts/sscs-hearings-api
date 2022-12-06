package uk.gov.hmcts.reform.sscs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.ExhaustedRetryException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.Hearing;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.State;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.exception.UnhandleableHearingStateException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingsRequestMapping;
import uk.gov.hmcts.reform.sscs.helper.mapping.OverridesMapping;
import uk.gov.hmcts.reform.sscs.helper.service.HearingsServiceHelper;
import uk.gov.hmcts.reform.sscs.model.HearingEvent;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequest;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus;
import uk.gov.hmcts.reform.sscs.model.multi.hearing.CaseHearing;
import uk.gov.hmcts.reform.sscs.model.multi.hearing.HearingsGetResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingCancelRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HmcUpdateResponse;
import uk.gov.hmcts.reform.sscs.reference.data.model.CancellationReason;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMapping.buildHearingPayload;
import static uk.gov.hmcts.reform.sscs.helper.service.HearingsServiceHelper.getHearingId;

@SuppressWarnings({"PMD.UnusedFormalParameter"})
// TODO Unsuppress in future
@Slf4j
@Service
@RequiredArgsConstructor
public class HearingsService {

    @Value("${retry.hearing-response-update.max-retries}")
    private static int hearingResponseUpdateMaxRetries;

    private final HmcHearingApiService hmcHearingApiService;

    private final HmcHearingsApiService hmcHearingsApiService;

    private final CcdCaseService ccdCaseService;

    private final ReferenceDataServiceHolder referenceDataServiceHolder;

    public void processHearingRequest(HearingRequest hearingRequest) throws GetCaseException, UnhandleableHearingStateException, UpdateCaseException, InvalidMappingException {
        log.info("Processing Hearing Request for Case ID {}, Hearing State {} and Route {} and Cancellation Reason {}",
                hearingRequest.getCcdCaseId(),
                hearingRequest.getHearingState(),
                hearingRequest.getHearingRoute(),
                hearingRequest.getCancellationReason());

        processHearingWrapper(createWrapper(hearingRequest));
    }

    public void processHearingWrapper(HearingWrapper wrapper)
        throws UnhandleableHearingStateException, UpdateCaseException, InvalidMappingException {

        log.info("Processing Hearing Wrapper for Case ID {} and Hearing State {}",
                wrapper.getCaseData().getCcdCaseId(),
                wrapper.getState().getState());

        switch (wrapper.getState()) {
            case CREATE_HEARING:
                createHearing(wrapper);
                break;
            case UPDATE_HEARING:
                updateHearing(wrapper);
                break;
            case UPDATED_CASE:
                updatedCase(wrapper);
                break;
            case CANCEL_HEARING:
                cancelHearing(wrapper);
                break;
            case PARTY_NOTIFIED:
                partyNotified(wrapper);
                break;
            default:
                UnhandleableHearingStateException err = new UnhandleableHearingStateException(wrapper.getState());
                log.error(err.getMessage(),err);
                throw err;
        }
    }

    private void createHearing(HearingWrapper wrapper) throws UpdateCaseException, InvalidMappingException {
        SscsCaseData caseData = wrapper.getCaseData();
        String caseId = caseData.getCcdCaseId();

        HearingsGetResponse hearingsGetResponse = hmcHearingsApiService.getHearingsRequest(caseId, null);

        CaseHearing hearing = HearingsServiceHelper.findExistingRequestedHearings(hearingsGetResponse);

        HmcUpdateResponse hmcUpdateResponse;

        if (isNull(hearing)) {
            OverridesMapping.setDefaultOverrideFields(wrapper, referenceDataServiceHolder);
            HearingRequestPayload hearingPayload = buildHearingPayload(wrapper, referenceDataServiceHolder);
            hmcUpdateResponse = hmcHearingApiService.sendCreateHearingRequest(hearingPayload);

            if (caseData.getState() == State.LISTING_ERROR) {
                hmcUpdateResponse.setStatus(HmcStatus.EXCEPTION);

                HearingEvent event = HearingsServiceHelper.getHearingEvent(wrapper.getState());
                ccdCaseService.updateCaseData(
                    caseData,
                    event.getEventType(),
                    event.getSummary(),
                    event.getDescription());

                throw new InvalidMappingException("Missing Listing Requirements");
            }

            log.debug("Received Create Hearing Request Response for Case ID {}, Hearing State {} and Response:\n{}",
                caseId,
                wrapper.getState().getState(),
                hmcUpdateResponse.toString());
        } else {
            hmcUpdateResponse = HmcUpdateResponse.builder()
                .hearingRequestId(hearing.getHearingId())
                .versionNumber(hearing.getRequestVersion())
                .status(hearing.getHmcStatus())
                .build();

            log.debug("Existing hearing found, skipping Create Hearing Request for Case ID {}, Hearing State {} and "
                    + "Hearing Id {}",
                caseId,
                hearing.getHmcStatus(),
                hearing.getHearingId());
        }

        hearingResponseUpdate(wrapper, hmcUpdateResponse);
    }

    private void updateHearing(HearingWrapper wrapper) throws UpdateCaseException, InvalidMappingException {
        OverridesMapping.setDefaultOverrideFields(wrapper, referenceDataServiceHolder);
        HearingRequestPayload hearingPayload = buildHearingPayload(wrapper, referenceDataServiceHolder);
        String hearingId = getHearingId(wrapper);
        HmcUpdateResponse response = hmcHearingApiService.sendUpdateHearingRequest(hearingPayload, hearingId);

        log.debug("Received Update Hearing Request Response for Case ID {}, Hearing State {} and Response:\n{}",
                wrapper.getCaseData().getCcdCaseId(),
                wrapper.getState().getState(),
                response.toString());

        hearingResponseUpdate(wrapper, response);
    }

    private void updatedCase(HearingWrapper wrapper) {
        // TODO implement mapping for the event when a case is updated
    }

    private void cancelHearing(HearingWrapper wrapper) {
        String hearingId = getHearingId(wrapper);
        HearingCancelRequestPayload hearingPayload = HearingsRequestMapping.buildCancelHearingPayload(wrapper);
        HmcUpdateResponse response = hmcHearingApiService.sendCancelHearingRequest(hearingPayload, hearingId);

        log.debug("Received Cancel Hearing Request Response for Case ID {}, Hearing State {} and Response:\n{}",
                wrapper.getCaseData().getCcdCaseId(),
                wrapper.getState().getState(),
                response.toString());
        // TODO process hearing response
    }

    private void partyNotified(HearingWrapper wrapper) {
        // TODO SSCS-10075 - implement mapping for the event when a party has been notified, might not be needed
    }

    @Retryable(
        value = UpdateCaseException.class,
        maxAttemptsExpression = "${retry.hearing-response-update.max-retries}",
        backoff = @Backoff(delayExpression = "${retry.hearing-response-update.backoff}"))
    public void hearingResponseUpdate(HearingWrapper wrapper, HmcUpdateResponse response) throws UpdateCaseException {

        SscsCaseData caseData = wrapper.getCaseData();
        Long hearingRequestId = response.getHearingRequestId();
        String caseId = caseData.getCcdCaseId();

        log.info("Updating Case with Hearing Response for Case ID {}, Hearing ID {} and Hearing State {}",
            caseId,
            hearingRequestId,
            wrapper.getState().getState());

        Hearing hearing = HearingsServiceHelper.getHearingById(hearingRequestId, caseData);

        if (isNull(hearing)) {
            hearing = HearingsServiceHelper.createHearing(hearingRequestId);
            HearingsServiceHelper.addHearing(hearing, caseData);
        }

        HearingsServiceHelper.updateHearingId(hearing, response);
        HearingsServiceHelper.updateVersionNumber(hearing, response);

        HearingEvent event = HearingsServiceHelper.getHearingEvent(wrapper.getState());
        ccdCaseService.updateCaseData(
            caseData,
            event.getEventType(),
            event.getSummary(),
            event.getDescription());

        log.info("Case Updated with Hearing Response for Case ID {}, Hearing ID {}, Hearing State {} and CCD Event {}",
            caseId,
            hearingRequestId,
            wrapper.getState().getState(),
            event.getEventType().getCcdType());
    }

    @Recover
    public void hearingResponseUpdateRecover(UpdateCaseException exception, HearingWrapper wrapper, HmcUpdateResponse response) {

        log.info("Updating Case with Hearing Response has failed {} times, rethrowing exception, for Case ID {}, Hearing ID {} and Hearing State {} with the exception: {}",
            hearingResponseUpdateMaxRetries,
            wrapper.getCaseData().getCcdCaseId(),
            response.getHearingRequestId(),
            wrapper.getState().getState(),
            exception);

        throw new ExhaustedRetryException("Cancellation request Response received, rethrowing exception", exception);
    }

    private HearingWrapper createWrapper(HearingRequest hearingRequest) throws GetCaseException, UnhandleableHearingStateException {
        if (isNull(hearingRequest.getHearingState())) {
            UnhandleableHearingStateException err = new UnhandleableHearingStateException();
            log.error(err.getMessage(), err);
            throw err;
        }

        List<CancellationReason> cancellationReasons = nonNull(hearingRequest.getCancellationReason())
            ? List.of(hearingRequest.getCancellationReason())
            : null;
        return HearingWrapper.builder()
                .caseData(ccdCaseService.getCaseDetails(hearingRequest.getCcdCaseId()).getData())
                .state(hearingRequest.getHearingState())
                .cancellationReasons(cancellationReasons)
                .build();
    }
}
