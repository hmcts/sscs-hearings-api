package uk.gov.hmcts.reform.sscs.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.exception.UnhandleableHearingState;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingResponse;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.sscs.helper.HearingsMapping.*;
import static uk.gov.hmcts.reform.sscs.helper.PartiesNotifiedMapping.buildUpdatePartiesNotifiedPayload;
import static uk.gov.hmcts.reform.sscs.helper.PartiesNotifiedMapping.getVersionNumber;
import static uk.gov.hmcts.reform.sscs.helper.service.HearingsServiceHelper.getHearingId;

@SuppressWarnings({"PMD.UnusedFormalParameter", "PMD.LawOfDemeter", "PMD.CyclomaticComplexity"})
// TODO Unsuppress in future
@Slf4j
@Service
public class HearingsService {

    private final HmcHearingApi hmcHearingApi;

    private final IdamService idamService;

    private final HmcHearingPartiesNotifiedApi hmcHearingPartiesNotifiedApi;

    public HearingsService(HmcHearingApi hmcHearingApi,
                           IdamService idamService, HmcHearingPartiesNotifiedApi hmcHearingPartiesNotifiedApi) {
        this.hmcHearingApi = hmcHearingApi;
        this.idamService = idamService;
        this.hmcHearingPartiesNotifiedApi = hmcHearingPartiesNotifiedApi;
    }

    public void processHearingRequest(HearingWrapper wrapper) throws UnhandleableHearingState {
        if (isNull(wrapper.getState())) {
            UnhandleableHearingState err = new UnhandleableHearingState(null);
            log.error(err.getMessage(), err);
            throw err;
        }

        switch (wrapper.getState()) {
            case CREATE_HEARING:
                createHearing(wrapper);
                break;
            case UPDATE_HEARING:
                updateHearing(wrapper);
                break;
            case UPDATED_CASE:
                updatedCase(wrapper);
                // TODO Call hearingPut method
                break;
            case CANCEL_HEARING:
                cancelHearing(wrapper);
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

    private HearingResponse sendCreateHearingRequest(HearingWrapper wrapper) {
        return hmcHearingApi.createHearingRequest(
                idamService.getIdamTokens().getIdamOauth2Token(),
                idamService.getIdamTokens().getServiceAuthorization(),
                buildHearingPayload(wrapper)
        );
    }

    private void createHearing(HearingWrapper wrapper) {
        updateIds(wrapper);
        buildRelatedParties(wrapper);
        sendCreateHearingRequest(wrapper);
        // TODO Store response with SSCS-10274
    }

    private HearingResponse sendUpdateHearingRequest(HearingWrapper wrapper) {
        return hmcHearingApi.updateHearingRequest(
            idamService.getIdamTokens().getIdamOauth2Token(),
            idamService.getIdamTokens().getServiceAuthorization(),
            getHearingId(wrapper),
            buildHearingPayload(wrapper)
        );
    }

    private void updateHearing(HearingWrapper wrapper) {
        updateIds(wrapper);
        buildRelatedParties(wrapper);
        sendUpdateHearingRequest(wrapper);
        // store response?
    }

    private void updatedCase(HearingWrapper wrapper) {
        // TODO implement mapping for the event when a case is updated
    }

    private void cancelHearing(HearingWrapper wrapper) {
        // TODO implement mapping for the event when the hearing is cancelled, might not be needed
    }

    private void sendPartiesNotifiedUpdateRequest(HearingWrapper wrapper) {
        hmcHearingPartiesNotifiedApi.updatePartiesNotifiedHearingRequest(
            idamService.getIdamTokens().getIdamOauth2Token(),
            idamService.getIdamTokens().getServiceAuthorization(),
            getHearingId(wrapper),
            getVersionNumber(wrapper),
            buildUpdatePartiesNotifiedPayload(wrapper)

        );
    }

    private void partyNotified(HearingWrapper wrapper) {
        sendPartiesNotifiedUpdateRequest(wrapper);
    }
}
