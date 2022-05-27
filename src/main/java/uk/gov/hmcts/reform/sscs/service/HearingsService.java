package uk.gov.hmcts.reform.sscs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.InvalidIdException;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.exception.UnhandleableHearingStateException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingsRequestMapping;
import uk.gov.hmcts.reform.sscs.helper.service.HearingsServiceHelper;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.model.HearingEvent;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequest;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingCancelRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingResponse;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMapping.buildHearingPayload;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMapping.updateIds;
import static uk.gov.hmcts.reform.sscs.helper.service.HearingsServiceHelper.getHearingId;

@SuppressWarnings({"PMD.UnusedFormalParameter", "PMD.TooManyMethods"})
// TODO Unsuppress in future
@Slf4j
@Service
@RequiredArgsConstructor
public class HearingsService {
    private final HmcHearingApi hmcHearingApi;

    private final CcdCaseService ccdCaseService;

    private final IdamService idamService;

    private final ReferenceDataServiceHolder referenceDataServiceHolder;




    public void processHearingRequest(HearingRequest hearingRequest) throws GetCaseException, UnhandleableHearingStateException, UpdateCaseException, InvalidIdException, InvalidMappingException {
        log.info("Processing Hearing Request for Case ID {}, Hearing State {} and Hearing Route {}",
                hearingRequest.getCcdCaseId(),
                hearingRequest.getHearingState(),
                hearingRequest.getHearingRoute());

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
        updateIds(wrapper);
        HearingResponse response = sendCreateHearingRequest(wrapper);

        log.debug("Received Create Hearing Request Response for Case ID {}, Hearing State {} and Response:\n{}",
                wrapper.getCaseData().getCcdCaseId(),
                wrapper.getState().getState(),
                response.toString());

        hearingResponseUpdate(wrapper, response);
    }

    private void updateHearing(HearingWrapper wrapper) throws UpdateCaseException, InvalidMappingException {
        updateIds(wrapper);
        HearingResponse response = sendUpdateHearingRequest(wrapper);

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
        HearingResponse response = sendCancelHearingRequest(wrapper);// TODO: Get Reason in Ticket: SSCS-10366

        log.debug("Received Cancel Hearing Request Response for Case ID {}, Hearing State {} and Response:\n{}",
                wrapper.getCaseData().getCcdCaseId(),
                wrapper.getState().getState(),
                response.toString());
        // TODO process hearing response
    }

    private void partyNotified(HearingWrapper wrapper) {
        // TODO SSCS-10075 - implement mapping for the event when a party has been notified, might not be needed
    }

    private HearingResponse sendCreateHearingRequest(HearingWrapper wrapper) throws InvalidMappingException {
        HearingRequestPayload hearingPayload = buildHearingPayload(wrapper, referenceDataServiceHolder);
        log.debug("Sending Create Hearing Request for Case ID {}, Hearing State {} and request:\n{}",
                wrapper.getCaseData().getCcdCaseId(),
                wrapper.getState().getState(),
                hearingPayload.toString());
        return hmcHearingApi.createHearingRequest(
                idamService.getIdamTokens().getIdamOauth2Token(),
                idamService.getIdamTokens().getServiceAuthorization(),
                hearingPayload
        );
    }

    private HearingResponse sendUpdateHearingRequest(HearingWrapper wrapper) throws InvalidMappingException {
        HearingRequestPayload hearingPayload = buildHearingPayload(wrapper, referenceDataServiceHolder);
        log.debug("Sending Update Hearing Request for Case ID {}, Hearing State {} and request:\n{}",
                wrapper.getCaseData().getCcdCaseId(),
                wrapper.getState().getState(),
                hearingPayload.toString());
        return hmcHearingApi.updateHearingRequest(
                idamService.getIdamTokens().getIdamOauth2Token(),
                idamService.getIdamTokens().getServiceAuthorization(),
                getHearingId(wrapper),
                hearingPayload
        );
    }

    public HearingResponse sendCancelHearingRequest(HearingWrapper wrapper) {
        HearingCancelRequestPayload hearingPayload = HearingsRequestMapping.buildCancelHearingPayload(null); // TODO: Get Reason in Ticket: SSCS-10366
        log.debug("Sending Update Hearing Request for Case ID {}, Hearing State {} and request:\n{}",
                wrapper.getCaseData().getCcdCaseId(),
                wrapper.getState().getState(),
                hearingPayload.toString());
        return hmcHearingApi.cancelHearingRequest(
                idamService.getIdamTokens().getIdamOauth2Token(),
                idamService.getIdamTokens().getServiceAuthorization(),
                String.valueOf(wrapper.getCaseData().getSchedulingAndListingFields().getActiveHearingId()),
                hearingPayload
        );
    }

    public void hearingResponseUpdate(HearingWrapper wrapper, HearingResponse response)
        throws UpdateCaseException, InvalidIdException {

        log.info("Updating Case with Hearing Response for Case ID {} and Hearing State {}",
                wrapper.getCaseData().getCcdCaseId(),
                wrapper.getState().getState());

        HearingsServiceHelper.updateHearingId(wrapper, response);
        HearingsServiceHelper.updateVersionNumber(wrapper, response);

        HearingEvent event = HearingsServiceHelper.getHearingEvent(wrapper.getState());
        ccdCaseService.updateCaseData(
                wrapper.getCaseData(),
                event.getEventType(),
                event.getSummary(),
                event.getDescription());

        log.info("Case Updated with Hearing Response for Case ID {}, Hearing State {} and CCD Event {}",
                wrapper.getCaseData().getCcdCaseId(),
                wrapper.getState().getState(),
                event.getEventType().getCcdType());
    }

    private HearingWrapper createWrapper(HearingRequest hearingRequest) throws GetCaseException, UnhandleableHearingStateException, InvalidIdException {
        if (isNull(hearingRequest.getHearingState())) {
            UnhandleableHearingStateException err = new UnhandleableHearingStateException();
            log.error(err.getMessage(), err);
            throw err;
        }

        return HearingWrapper.builder()
                .caseData(ccdCaseService.getCaseDetails(hearingRequest.getCcdCaseId()).getData())
                .state(hearingRequest.getHearingState())
                .build();
    }
}
