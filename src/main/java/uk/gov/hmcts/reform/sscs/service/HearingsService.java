package uk.gov.hmcts.reform.sscs.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.exception.UnhandleableHearingStateException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingsRequestMapping;
import uk.gov.hmcts.reform.sscs.helper.service.HearingsServiceHelper;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.model.HearingEvent;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingResponse;

import static java.util.Objects.isNull;

@SuppressWarnings({"PMD.UnusedFormalParameter", "PMD.LawOfDemeter", "PMD.CyclomaticComplexity"})
// TODO Unsuppress in future
@Slf4j
@Service
public class HearingsService {

    private final HmcHearingApi hmcHearingApi;

    private final CcdCaseService ccdCaseService;

    private final IdamService idamService;

    public HearingsService(HmcHearingApi hmcHearingApi, CcdCaseService ccdCaseService, IdamService idamService) {
        this.hmcHearingApi = hmcHearingApi;
        this.ccdCaseService = ccdCaseService;
        this.idamService = idamService;
    }

    public void processHearingRequest(HearingWrapper wrapper) throws UnhandleableHearingStateException {
        if (isNull(wrapper.getState())) {
            UnhandleableHearingStateException err = new UnhandleableHearingStateException();
            log.error(err.getMessage(), err);
            throw err;
        }

        switch (wrapper.getState()) {
            case CREATE_HEARING:
                createHearing(wrapper);
                // TODO Call hearingPost method
                break;
            case UPDATE_HEARING:
                updateHearing(wrapper);
                // TODO Call hearingPut method
                break;
            case UPDATED_CASE:
                updatedCase(wrapper);
                // TODO Call hearingPut method
                break;
            case CANCEL_HEARING:
                cancelHearing(wrapper);
                break;
            case PARTY_NOTIFIED:
                partyNotified(wrapper);
                // TODO Call partiesNotifiedPost method
                break;
            default:
                UnhandleableHearingStateException err = new UnhandleableHearingStateException(wrapper.getState());
                log.error(err.getMessage(),err);
                throw err;
        }
    }

    private void createHearing(HearingWrapper wrapper) {
        //TODO Will be replaced when SSCS-10321 is merged
    }


    private void updateHearing(HearingWrapper wrapper) {
        // TODO implement mapping for the event when the hearing's details are updated
    }

    private void updatedCase(HearingWrapper wrapper) {
        // TODO implement mapping for the event when a case is updated
    }

    public HearingResponse sendDeleteHearingRequest(HearingWrapper wrapper) {
        return hmcHearingApi.deleteHearingRequest(
                idamService.getIdamTokens().getIdamOauth2Token(),
                idamService.getIdamTokens().getServiceAuthorization(),
                String.valueOf(wrapper.getCaseData().getSchedulingAndListingFields().getActiveHearingId()),
                HearingsRequestMapping.buildDeleteHearingPayload(null) // TODO: Get Reason in Ticket: SSCS-10366
        );
    }

    private void cancelHearing(HearingWrapper wrapper) {
        sendDeleteHearingRequest(wrapper);
    }

    private void partyNotified(HearingWrapper wrapper) {
        // TODO implement mapping for the event when a party has been notified, might not be needed
    }

    public void hearingResponseUpdate(HearingWrapper wrapper, HearingResponse response) throws UpdateCaseException {
        HearingsServiceHelper.updateHearingId(wrapper, response);
        HearingsServiceHelper.updateVersionNumber(wrapper, response);
        HearingsServiceHelper.addEvent(wrapper);

        HearingEvent event = HearingsServiceHelper.getHearingEvent(wrapper.getState());
        ccdCaseService.updateCaseData(wrapper.getCaseData(), event.getEventType(), event.getSummary(), event.getDescription());
    }


}
