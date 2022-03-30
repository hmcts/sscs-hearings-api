package uk.gov.hmcts.reform.sscs.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.exception.UnhandleableHearingState;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HmcHearingResponse;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.sscs.helper.HearingsMapping.buildHearingPayload;
import static uk.gov.hmcts.reform.sscs.helper.HearingsMapping.buildRelatedParties;
import static uk.gov.hmcts.reform.sscs.helper.HearingsMapping.updateIds;

@SuppressWarnings({"PMD.UnusedFormalParameter", "PMD.LawOfDemeter", "PMD.CyclomaticComplexity"})
// TODO Unsuppress in future
@Slf4j
@Service
public class HearingsService {

    private final HmcHearingApi hmcHearingApi;

    private final CcdCaseService ccdCaseService;

    private final IdamService idamService;

    public HearingsService(HmcHearingApi hmcHearingApi, CcdCaseService ccdCaseService,
                           IdamService idamService) {
        this.hmcHearingApi = hmcHearingApi;
        this.ccdCaseService = ccdCaseService;
        this.idamService = idamService;
    }

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
                updateIds(wrapper);
                HmcHearingResponse response = sendCreateHearingRequest(wrapper);
                updateCaseHearingId(wrapper, response.getHearingRequestId());
                break;
            case UPDATE_HEARING:
                sendUpdateHearingRequest(wrapper);
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

    private void updateCaseHearingId(HearingWrapper wrapper, Long hearingId) {
        SscsCaseData caseData = wrapper.getOriginalCaseData();
        caseData.getSchedulingAndListingFields().setActiveHearingId(hearingId);
        try {
            ccdCaseService.updateCaseDetails(caseData, EventType.HEARING_BOOKED,
                                             "Case Updated", "Active hearing ID set");
        } catch (UpdateCaseException e) {
            //Error handling? Should we do anything here?
            e.printStackTrace();
        }
    }

    private HmcHearingResponse sendCreateHearingRequest(HearingWrapper wrapper) {
        HearingRequestPayload payload = buildHearingPayload(wrapper);

        return hmcHearingApi.createHearingRequest(idamService.getIdamTokens().getIdamOauth2Token(),
            idamService.getIdamTokens().getServiceAuthorization(), payload);
    }

    private void sendUpdateHearingRequest(HearingWrapper wrapper) {
        hmcHearingApi.updateHearingRequest(
            idamService.getIdamTokens().getIdamOauth2Token(),
            idamService.getIdamTokens().getServiceAuthorization(),
            wrapper.getOriginalCaseData().getSchedulingAndListingFields().getActiveHearingId().toString(),
            buildHearingPayload(wrapper));
    }

    private void createHearing(HearingWrapper wrapper) {
        updateIds(wrapper);
        buildRelatedParties(wrapper);
        sendCreateHearingRequest(wrapper);
        // TODO Store response with SSCS-10274
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
        // HearingResponse hearingResponse = HearingResponse.builder().build();
        //
        // hearingResponse.setHearingRequestId(hearingRequestId);
        // hearingResponse.setHmcStatus(hmcStatus);
        // hearingResponse.setVersion(version);
        //
        // wrapper.getUpdatedCaseData().getLatestHmcHearing().setHearingResponse(hearingResponse);
    }

    public void updateHearingResponse(HearingWrapper wrapper, String hmcStatus, Number version) {
        // To be called by hearing PUT response
        // HearingResponse hearingResponse = wrapper.getUpdatedCaseData().getLatestHmcHearing().getHearingResponse();
        // hearingResponse.setHmcStatus(hmcStatus);
        // hearingResponse.setVersion(version);
    }

    public void updateHearingResponse(HearingWrapper wrapper, String hmcStatus, Number version,
                                      String cancellationReasonCode) {
        // To be called after hearing Delete response
        updateHearingResponse(wrapper, hmcStatus, version);
        // wrapper.getUpdatedCaseData().getLatestHmcHearing().getHearingResponse()
        // wrapper.getUpdatedCaseData().getLatestHmcHearing().setHearingCancellationReason(cancellationReasonCode);
    }


}
