package uk.gov.hmcts.reform.sscs.service.hmc.topic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.State;
import uk.gov.hmcts.reform.sscs.exception.InvalidHmcMessageException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.model.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.ListAssistCaseStatus;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.reference.data.mappings.CancellationReason;

import javax.validation.Valid;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.sscs.ccd.domain.State.DORMANT_APPEAL_STATE;
import static uk.gov.hmcts.reform.sscs.ccd.domain.State.HEARING;
import static uk.gov.hmcts.reform.sscs.ccd.domain.State.READY_TO_LIST;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseStateUpdateService {

    private final CaseHearingLocationService caseHearingLocationService;

    public void updateListed(HearingGetResponse hearingResponse, HmcMessage hmcMessage, @Valid SscsCaseData sscsCaseData)
            throws UpdateCaseException, InvalidHmcMessageException {
        ListAssistCaseStatus listAssistCaseStatus = hearingResponse.getHearingResponse().getListAssistCaseStatus();

        State state = mapHmcCreatedOrUpdatedToCcd(listAssistCaseStatus, sscsCaseData.getCcdCaseId());
        setState(sscsCaseData, state);

        caseHearingLocationService.updateVenue(hmcMessage, sscsCaseData);
    }

    public void updateCancelled(HearingGetResponse hearingResponse, @Valid SscsCaseData sscsCaseData)
            throws InvalidHmcMessageException {
        String cancellationReason = hearingResponse.getHearingResponse().getHearingCancellationReason();

        State state = mapHmcCancelledToCcdState(cancellationReason, sscsCaseData.getCcdCaseId());
        setState(sscsCaseData, state);
    }

    public void updateFailed(@Valid SscsCaseData sscsCaseData) {
        setState(sscsCaseData, State.HANDLING_ERROR);
    }

    private void setState(SscsCaseData sscsCaseData, State state) {
        sscsCaseData.setState(state);
        log.info("CCD state has been updated to {} for caseId {}", state, sscsCaseData.getCcdCaseId());
    }

    private State mapHmcCreatedOrUpdatedToCcd(ListAssistCaseStatus listAssistCaseStatus, String caseId)
            throws InvalidHmcMessageException {
        if (isNull(listAssistCaseStatus)) {
            throw new InvalidHmcMessageException(String.format("Can not map listing Case Status %s for Case ID %s",
                    listAssistCaseStatus, caseId));
        }

        switch (listAssistCaseStatus) {
            case LISTED:
                return HEARING;
            case AWAITING_LISTING:
                return READY_TO_LIST;
            default:
                throw new InvalidHmcMessageException(String.format("Can not map HMC updated or create listing status %s "
                                + "with CCD state for Case ID %s",
                    listAssistCaseStatus, caseId));
        }
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    private State mapHmcCancelledToCcdState(String cancellationReasonLabel, String caseId)
            throws InvalidHmcMessageException {
        CancellationReason cancellationReason = CancellationReason.getCancellationReasonByLabel(cancellationReasonLabel);
        if (isNull(cancellationReason)) {
            throw new InvalidHmcMessageException(String.format("Can not map cancellation reason label %s for Case ID %s",
                    cancellationReasonLabel, caseId));
        }

        switch (cancellationReason) {
            case WITHDRAWN:
            case STRUCK_OUT:
            case LAPSED:
                return DORMANT_APPEAL_STATE;
            case PARTY_UNABLE_TO_ATTEND:
            case EXCLUSION:
            case INCOMPLETE_TRIBUNAL:
            case LISTED_IN_ERROR:
            case OTHER:
            case PARTY_DID_NOT_ATTEND:
                return READY_TO_LIST;
            default:
                throw new InvalidHmcMessageException(String.format("Cancellation reason %s not supported regarding Case ID %s",
                        cancellationReason, caseId));
        }

    }


}
