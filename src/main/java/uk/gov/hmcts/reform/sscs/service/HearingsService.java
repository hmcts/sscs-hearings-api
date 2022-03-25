package uk.gov.hmcts.reform.sscs.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.exception.UnhandleableHearingState;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;

import static java.util.Objects.isNull;

@SuppressWarnings({"PMD.UnusedFormalParameter", "PMD.LawOfDemeter", "PMD.CyclomaticComplexity"})
// TODO Unsuppress in future
@Slf4j
@Service
public class HearingsService {

    public void processHearingRequest(HearingWrapper wrapper) throws UnhandleableHearingState {
        if (!EventType.READY_TO_LIST.equals(wrapper.getEvent())) {
            log.info("The Event: {}, cannot be handled for the case with the id: {}",
                    wrapper.getEvent(), wrapper.getOriginalCaseData().getCcdCaseId());
            return;
        }

        if (isNull(wrapper.getState())) {
            UnhandleableHearingState err = new UnhandleableHearingState(null);
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
                canelHearing(wrapper);
                // TODO Call hearingDelete method
                break;
            case PARTY_NOTIFIED:
                partyNotified(wrapper);
                // TODO Call partiesNotifiedPost method
                break;
            default:
                UnhandleableHearingState err = new UnhandleableHearingState(wrapper.getState());
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

    private void canelHearing(HearingWrapper wrapper) {
        // TODO implement mapping for the event when the hearing is cancelled, might not be needed
    }

    private void partyNotified(HearingWrapper wrapper) {
        // TODO implement mapping for the event when a party has been notified, might not be needed
    }

    public void addHearingResponse(HearingWrapper wrapper, String hearingRequestId, String hmcStatus, Number version) {
        // To be called by hearing POST response
    }

    public void updateHearingResponse(HearingWrapper wrapper, String hmcStatus, Number version) {
        // To be called by hearing PUT response
    }

    public void updateHearingResponse(HearingWrapper wrapper, String hmcStatus, Number version,
                                      String cancellationReasonCode) {
        // To be called after hearing Delete response
    }


}
