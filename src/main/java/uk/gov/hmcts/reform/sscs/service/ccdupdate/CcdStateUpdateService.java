package uk.gov.hmcts.reform.sscs.service.ccdupdate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.State;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.ListAssistCaseStatus;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.reference.data.mappings.CancellationReason;

import javax.validation.Valid;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.sscs.ccd.domain.State.DORMANT_APPEAL_STATE;
import static uk.gov.hmcts.reform.sscs.ccd.domain.State.HEARING;
import static uk.gov.hmcts.reform.sscs.ccd.domain.State.READY_TO_LIST;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.ListingStatus.FIXED;


@Slf4j
@Service
public class CcdStateUpdateService {

    private static final String CANCELLED = "Cancelled";

    public void updateListed(HearingGetResponse hearingResponse, @Valid SscsCaseData sscsCaseData)
            throws UpdateCaseException {

        if (isHearingListingNotStatusFixed(hearingResponse)) {
            return;
        }

        ListAssistCaseStatus listAssistCaseStatus = hearingResponse.getHearingResponse().getListAssistCaseStatus();

        if (isNull(listAssistCaseStatus)) {
            throw new UpdateCaseException(String.format("Can not map listing Case Status %s for caseId %s",
                listAssistCaseStatus, sscsCaseData.getCcdCaseId()));
        }

        State state = mapHmcCreatedOrUpdatedToCcd(listAssistCaseStatus, sscsCaseData.getCcdCaseId());
        setState(sscsCaseData, state);
    }

    public void updateCancelled(HearingGetResponse hearingResponse, @Valid SscsCaseData sscsCaseData)
            throws UpdateCaseException {

        if (isHearingNotCancelled(hearingResponse)) {
            return;
        }

        String cancellationReason = hearingResponse.getHearingResponse().getHearingCancellationReason();
        CancellationReason reason = CancellationReason.getCancellationReasonByLabel(cancellationReason);

        if (isNull(reason)) {
            throw new UpdateCaseException(String.format("Can not map cancellation reason %s for caseId %s",
                    cancellationReason, sscsCaseData.getCcdCaseId()));
        }

        State state = mapHmcCancelledToCcdState(reason, sscsCaseData.getCcdCaseId());
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
            throws UpdateCaseException {
        switch (listAssistCaseStatus) {
            case LISTED:
                return HEARING;
            case AWAITING_LISTING:
                return READY_TO_LIST;
            default:
                throw new UpdateCaseException(String.format("Can not map HMC updated or create listing status %s "
                                + "with CCD state for caseId %s",
                    listAssistCaseStatus, caseId));
        }
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    private State mapHmcCancelledToCcdState(CancellationReason cancellationReason, String caseId)
            throws UpdateCaseException {
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
                throw new UpdateCaseException(String.format("Can not map cancellation reason %s for caseId %s",
                        cancellationReason, caseId));
        }

    }

    private static boolean isHearingListingNotStatusFixed(HearingGetResponse hearingResponse) {
        return FIXED != hearingResponse.getHearingResponse().getListingStatus();
    }

    private static boolean isHearingNotCancelled(HearingGetResponse hearingResponse) {
        return !CANCELLED.equalsIgnoreCase(hearingResponse.getRequestDetails().getStatus())
            && isNull(hearingResponse.getHearingResponse().getHearingCancellationReason());
    }
}
