package uk.gov.hmcts.reform.sscs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.ExhaustedRetryException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.ListingException;
import uk.gov.hmcts.reform.sscs.exception.UnhandleableHearingStateException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingsRequestMapping;
import uk.gov.hmcts.reform.sscs.helper.mapping.OverridesMapping;
import uk.gov.hmcts.reform.sscs.helper.service.HearingsServiceHelper;
import uk.gov.hmcts.reform.sscs.model.HearingEvent;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequest;
import uk.gov.hmcts.reform.sscs.model.multi.hearing.CaseHearing;
import uk.gov.hmcts.reform.sscs.model.multi.hearing.HearingsGetResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingCancelRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HmcUpdateResponse;
import uk.gov.hmcts.reform.sscs.reference.data.model.CancellationReason;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.Arrays;
import java.util.List;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.sscs.ccd.domain.HearingRoute.LIST_ASSIST;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMapping.buildHearingPayload;
import static uk.gov.hmcts.reform.sscs.helper.service.HearingsServiceHelper.getHearingId;

@SuppressWarnings({"PMD.UnusedFormalParameter", "PMD.TooManyMethods"})
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
    // Leaving blank for now until a future change is scoped and completed, then we can add the case states back in
    public static final List<State> INVALID_CASE_STATES = Arrays.asList();

    public void processHearingRequest(HearingRequest hearingRequest) throws GetCaseException,
        UnhandleableHearingStateException, UpdateCaseException, ListingException {
        log.info("Processing Hearing Request for Case ID {}, Hearing State {} and Route {} and Cancellation Reason {}",
                hearingRequest.getCcdCaseId(),
                hearingRequest.getHearingState(),
                hearingRequest.getHearingRoute(),
                hearingRequest.getCancellationReason());

        processHearingWrapper(createWrapper(hearingRequest));
    }

    public void processHearingWrapper(HearingWrapper wrapper)
        throws UnhandleableHearingStateException, UpdateCaseException, ListingException {

        String caseId = wrapper.getCaseData().getCcdCaseId();
        log.info("Processing Hearing Wrapper for Case ID {}, Case State {} and Hearing State {}",
                 caseId,
                 wrapper.getCaseState().toString(),
                 wrapper.getHearingState().getState());

        if (caseStatusInvalid(wrapper)) {
            log.info("Case is in an invalid state for a hearing request. No requests sent to the HMC. Case ID {} and Case State {}",
                     caseId,
                     wrapper.getCaseState().toString());
            return;
        }

        switch (wrapper.getHearingState()) {
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
                log.info("Parties notified API not supported. Case ID {}",
                         caseId);
                break;
            default:
                UnhandleableHearingStateException err = new UnhandleableHearingStateException(wrapper.getHearingState());
                log.error(err.getMessage(),err);
                throw err;
        }
    }

    private boolean caseStatusInvalid(HearingWrapper wrapper) {
        return INVALID_CASE_STATES.contains(wrapper.getCaseState());
    }

    private boolean isRpcInTheApprovedList(SscsCaseData caseData) {
        RegionalProcessingCenter regionalProcessingCenter = caseData.getRegionalProcessingCenter();
        if (regionalProcessingCenter != null) {
            String regionalProcessingCenterPostCode = regionalProcessingCenter.getPostcode();
            RegionalProcessingCenterService regionalProcessingCenterService = referenceDataServiceHolder.getRegionalProcessingCenterService();
            RegionalProcessingCenter processingCenterByPostCode = regionalProcessingCenterService.getByPostcode(regionalProcessingCenterPostCode);
            return LIST_ASSIST.equals(processingCenterByPostCode.getHearingRoute());
        }
        return false;
    }

    private void createHearing(HearingWrapper wrapper) throws UpdateCaseException, ListingException {
        SscsCaseData caseData = wrapper.getCaseData();

        if (!isRpcInTheApprovedList(caseData)) {
            ccdCaseService.updateCaseData(caseData, EventType.LISTING_ERROR, "", "RPC is invalid");
            return;
        }

        String caseId = caseData.getCcdCaseId();
        HearingsGetResponse hearingsGetResponse = hmcHearingsApiService.getHearingsRequest(caseId, null);
        CaseHearing hearing = HearingsServiceHelper.findExistingRequestedHearings(hearingsGetResponse);
        HmcUpdateResponse hmcUpdateResponse;

        if (isNull(hearing)) {
            OverridesMapping.setDefaultOverrideFields(wrapper, referenceDataServiceHolder);
            HearingRequestPayload hearingPayload = buildHearingPayload(wrapper, referenceDataServiceHolder);
            hmcUpdateResponse = hmcHearingApiService.sendCreateHearingRequest(hearingPayload);

            log.debug("Received Create Hearing Request Response for Case ID {}, Hearing State {} and Response:\n{}",
                caseId,
                wrapper.getHearingState().getState(),
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

    private void updateHearing(HearingWrapper wrapper) throws UpdateCaseException, ListingException {
        OverridesMapping.setDefaultOverrideFields(wrapper, referenceDataServiceHolder);
        HearingRequestPayload hearingPayload = buildHearingPayload(wrapper, referenceDataServiceHolder);
        String hearingId = getHearingId(wrapper);
        HmcUpdateResponse response = hmcHearingApiService.sendUpdateHearingRequest(hearingPayload, hearingId);

        log.debug("Received Update Hearing Request Response for Case ID {}, Hearing State {} and Response:\n{}",
                wrapper.getCaseData().getCcdCaseId(),
                wrapper.getHearingState().getState(),
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
                wrapper.getHearingState().getState(),
                response.toString());
        // TODO process hearing response
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
            wrapper.getHearingState().getState());

        Hearing hearing = HearingsServiceHelper.getHearingById(hearingRequestId, caseData);

        if (isNull(hearing)) {
            hearing = HearingsServiceHelper.createHearing(hearingRequestId);
            HearingsServiceHelper.addHearing(hearing, caseData);
        }

        HearingsServiceHelper.updateHearingId(hearing, response);
        HearingsServiceHelper.updateVersionNumber(hearing, response);

        HearingEvent event = HearingsServiceHelper.getHearingEvent(wrapper.getHearingState());
        ccdCaseService.updateCaseData(
            caseData,
            event.getEventType(),
            event.getSummary(),
            event.getDescription());

        log.info("Case Updated with Hearing Response for Case ID {}, Hearing ID {}, Hearing State {} and CCD Event {}",
            caseId,
            hearingRequestId,
            wrapper.getHearingState().getState(),
            event.getEventType().getCcdType());
    }

    @Recover
    public void hearingResponseUpdateRecover(UpdateCaseException exception, HearingWrapper wrapper, HmcUpdateResponse response) {
        log.info("Updating Case with Hearing Response has failed {} times, rethrowing exception, for Case ID {}, Hearing ID {} and Hearing State {} with the exception: {}",
            hearingResponseUpdateMaxRetries,
            wrapper.getCaseData().getCcdCaseId(),
            response.getHearingRequestId(),
            wrapper.getHearingState().getState(),
            exception);

        throw new ExhaustedRetryException("Cancellation request Response received, rethrowing exception", exception);
    }

    private HearingWrapper createWrapper(HearingRequest hearingRequest) throws GetCaseException,
        UnhandleableHearingStateException {
        if (isNull(hearingRequest.getHearingState())) {
            UnhandleableHearingStateException err = new UnhandleableHearingStateException();
            log.error(err.getMessage(), err);
            throw err;
        }

        List<CancellationReason> cancellationReasons = null;

        if (hearingRequest.getCancellationReason() != null) {
            cancellationReasons = List.of(hearingRequest.getCancellationReason());
        }

        SscsCaseDetails sscsCaseDetails = ccdCaseService.getCaseDetails(hearingRequest.getCcdCaseId());
        return HearingWrapper.builder()
                .caseData(sscsCaseDetails.getData())
                .caseState(State.getById(sscsCaseDetails.getState()))
                .hearingState(hearingRequest.getHearingState())
                .cancellationReasons(cancellationReasons)
                .build();
    }
}
