package uk.gov.hmcts.reform.sscs.service.ccdupdate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.State;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.ListingCaseStatus;
import uk.gov.hmcts.reform.sscs.reference.data.mappings.CancellationReason;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.sscs.model.single.hearing.ListingCaseStatus.AWAITING_LISTING;
import static uk.gov.hmcts.reform.sscs.model.single.hearing.ListingCaseStatus.LISTED;
import static uk.gov.hmcts.reform.sscs.model.single.hearing.ListingStatus.FIXED;


@Slf4j
@Service
public class CcdStateUpdateService {

    public void updateListed(HearingGetResponse hearingResponse, SscsCaseData sscsCaseData) {
        State state = State.UNKNOWN;
        if (isHearingListingStatusFixed(hearingResponse)) {
            state = mapHmcCreatedOrUpdatedToCcd(hearingResponse);
        }

        if (isKnownState(state)) {
            sscsCaseData.setState(state);
            log.info("CCD state has been updated to {} for caseId {}", state, sscsCaseData.getCcdCaseId());
        }
    }

    public void updateCancelled(HearingGetResponse hearingResponse, SscsCaseData sscsCaseData) throws UpdateCaseException {
        State state = State.UNKNOWN;

        if (isHearingCancelled(hearingResponse)) {
            state = mapHmcCancelledToCcdState(
                hearingResponse.getHearingResponse().getHearingCancellationReason(), sscsCaseData.getCcdCaseId());
        }
        if (isKnownState(state)) {
            sscsCaseData.setState(state);
            log.info("CCD state has been updated to {} for caseId {}", state, sscsCaseData.getCcdCaseId());
        }
    }

    public void validateUpdateCancelled(HearingGetResponse hearingResponse, SscsCaseData sscsCaseData) throws UpdateCaseException {
        if (isHearingCancelled(hearingResponse)) {
            mapHmcCancelledToCcdState(
                hearingResponse.getHearingResponse().getHearingCancellationReason(), sscsCaseData.getCcdCaseId());
        }
    }

    public void updateFailed(SscsCaseData sscsCaseData) {
        sscsCaseData.setState(State.HANDLING_ERROR);
        log.info("CCD state has been updated to {} for caseId {}", State.HANDLING_ERROR, sscsCaseData.getCcdCaseId());
    }

    private State mapHmcCreatedOrUpdatedToCcd(HearingGetResponse hearingResponse) {
        if (checkHearingStatus(hearingResponse, LISTED)) {
            return State.HEARING;
        }
        if (checkHearingStatus(hearingResponse, AWAITING_LISTING)) {
            return State.READY_TO_LIST;
        }
        return State.UNKNOWN;
    }

    private boolean isKnownState(State state) {
        return !state.equals(State.UNKNOWN);
    }

    private boolean checkCancellationReasonIsDormantAppealState(CancellationReason cancellationReason) {

        switch (cancellationReason) {
            case WITHDRAWN:
            case STRUCK_OUT:
            case LAPSED:
                return true;
            default:
                return false;
        }
    }

    private boolean checkCancellationReasonIsReadyToList(CancellationReason cancellationReason) {

        switch (cancellationReason) {
            case PARTY_UNABLE_TO_ATTEND:
            case EXCLUSION:
            case INCOMPLETE_TRIBUNAL:
            case LISTED_IN_ERROR:
            case OTHER:
            case PARTY_DID_NOT_ATTEND:
                return true;
            default:
                return false;
        }
    }

    private State mapNonNullHmcCancelledToCcdState(CancellationReason cancellationReason, String caseId) throws UpdateCaseException {

        if (checkCancellationReasonIsDormantAppealState(cancellationReason)) {
            return State.DORMANT_APPEAL_STATE;
        } else if (checkCancellationReasonIsReadyToList(cancellationReason)) {
            return State.READY_TO_LIST;
        } else {
            throw new UpdateCaseException(
                String.format("Can not map cancellation reason with this value - %s for caseId %s", cancellationReason, caseId));
        }
    }

    private State mapHmcCancelledToCcdState(String cancellationReason, String caseId) throws UpdateCaseException {
        CancellationReason reason = CancellationReason.getCancellationReasonByValue(cancellationReason);

        if (reason == null) {
            throw new UpdateCaseException(
                String.format("Can not map cancellation reason with null value for caseId %s", caseId));
        }

        return mapNonNullHmcCancelledToCcdState(reason, caseId);

    }

    private static boolean checkHearingStatus(HearingGetResponse hearingResponse, ListingCaseStatus laStatus) {
        return laStatus.getListingCaseStatusLabel().equals(
            hearingResponse.getHearingResponse().getListingCaseStatus());
    }

    private static boolean isHearingListingStatusFixed(HearingGetResponse hearingResponse) {
        return FIXED.getListingStatusLabel().equals(
            hearingResponse.getHearingResponse().getListingStatus());
    }

    private static boolean isHearingCancelled(HearingGetResponse hearingResponse) {
        return "Cancelled".equalsIgnoreCase(hearingResponse.getRequestDetails().getStatus())
            || nonNull(hearingResponse.getHearingResponse().getHearingCancellationReason());
    }
}
