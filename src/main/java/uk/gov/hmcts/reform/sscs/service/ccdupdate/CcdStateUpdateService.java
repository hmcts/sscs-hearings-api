package uk.gov.hmcts.reform.sscs.service.ccdupdate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.State;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.ListingCaseStatus;

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

    public void updateCancelled(HearingGetResponse hearingResponse, SscsCaseData sscsCaseData) {
        State state = State.UNKNOWN;

        if (isHearingCancelled(hearingResponse)) {
            state = mapHmcCancelledToCcdState(
                hearingResponse.getHearingResponse().getHearingCancellationReason());
        }
        if (isKnownState(state)) {
            sscsCaseData.setState(state);
            log.info("CCD state has been updated to {} for caseId {}", state, sscsCaseData.getCcdCaseId());
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

    @SuppressWarnings("PMD.CyclomaticComplexity")
    private State mapHmcCancelledToCcdState(String cancellationReason) {

        // todo use enum once PR 10273 is merged
        switch (cancellationReason) {
            case "Withdrawn":
            case "struckOut":
            case "Lapsed":
                return State.DORMANT_APPEAL_STATE;
            case "partyUnableToAttend":
            case "Exclusion":
            case "incompleteTribunal":
            case "listedInError":
            case "Other":
            case "partyDidNotAttend":
                return State.READY_TO_LIST;
            default:
                return State.UNKNOWN;
        }
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
