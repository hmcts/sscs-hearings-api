package uk.gov.hmcts.reform.sscs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.UnhandleableHearingStateException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingsRequestMapping;
import uk.gov.hmcts.reform.sscs.helper.service.HearingsServiceHelper;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.model.HearingEvent;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequest;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingResponse;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMapping.*;

@SuppressWarnings({"PMD.UnusedFormalParameter", "PMD.LawOfDemeter"})
// TODO Unsuppress in future
@Slf4j
@Service
@RequiredArgsConstructor
public class HearingsService {
    private final HmcHearingApi hmcHearingApi;

    private final CcdCaseService ccdCaseService;

    private final IdamService idamService;

    public void processHearingRequest(HearingRequest hearingRequest) throws GetCaseException, UnhandleableHearingStateException {
        processHearingWrapper(createWrapper(hearingRequest));
    }

    public void processHearingWrapper(HearingWrapper wrapper) throws UnhandleableHearingStateException {
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

    private HearingResponse sendCreateHearingRequest(HearingWrapper wrapper) {

        return hmcHearingApi.createHearingRequest(
                idamService.getIdamTokens().getIdamOauth2Token(),
                idamService.getIdamTokens().getServiceAuthorization(),
                buildHearingPayload(wrapper));
    }

    private void createHearing(HearingWrapper wrapper) {
        updateIds(wrapper);
        buildRelatedParties(wrapper);
        sendCreateHearingRequest(wrapper);
        // TODO Store response with SSCS-10274
    }


    private void updateHearing(HearingWrapper wrapper) {
        // TODO implement mapping for the event when the hearing's details are updated
    }

    private void updatedCase(HearingWrapper wrapper) {
        // TODO implement mapping for the event when a case is updated
    }

    public HearingResponse sendCancelHearingRequest(HearingWrapper wrapper) {
        return hmcHearingApi.cancelHearingRequest(
                idamService.getIdamTokens().getIdamOauth2Token(),
                idamService.getIdamTokens().getServiceAuthorization(),
                String.valueOf(wrapper.getCaseData().getSchedulingAndListingFields().getActiveHearingId()),
                HearingsRequestMapping.buildCancelHearingPayload(null) // TODO: Get Reason in Ticket: SSCS-10366
        );
	}

    private void cancelHearing(HearingWrapper wrapper) {
        sendCancelHearingRequest(wrapper); // TODO: Get Reason in Ticket: SSCS-10366
    }

    private void partyNotified(HearingWrapper wrapper) {
        // TODO SSCS-10075 - implement mapping for the event when a party has been notified, might not be needed
    }

    public void hearingResponseUpdate(HearingWrapper wrapper, HearingResponse response) throws UpdateCaseException {
        HearingsServiceHelper.updateHearingId(wrapper, response);
        HearingsServiceHelper.updateVersionNumber(wrapper, response);
        HearingsServiceHelper.addEvent(wrapper);

        HearingEvent event = HearingsServiceHelper.getHearingEvent(wrapper.getState());
        ccdCaseService.updateCaseData(
                wrapper.getCaseData(),
                event.getEventType(),
                event.getSummary(),
                event.getDescription());
    }


    private HearingWrapper createWrapper(HearingRequest hearingRequest) throws GetCaseException, UnhandleableHearingStateException {
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
