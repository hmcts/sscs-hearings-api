package uk.gov.hmcts.reform.sscs.service.ccdupdate;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.State;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.sscs.model.single.hearing.ListingCaseStatus.AWAITING_LISTING;
import static uk.gov.hmcts.reform.sscs.model.single.hearing.ListingCaseStatus.LISTED;
import static uk.gov.hmcts.reform.sscs.model.single.hearing.ListingStatus.FIXED;

@Service
public class CcdStateUpdateService {

    public void updateListed(HearingGetResponse hearingResponse, SscsCaseData sscsCaseData) {
        State state = State.UNKNOWN;
        if (isHearingListingStatusFixed(hearingResponse)) {
            state = mapHmcCreatedOrUpdatedToCcd(hearingResponse);
        }

        if (isKnownState(state)) {
            sscsCaseData.setState(state);
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
        }
    }

    public void updateFailed(SscsCaseData sscsCaseData) {
        sscsCaseData.setState(State.HANDLING_ERROR);
    }

    private State mapHmcCreatedOrUpdatedToCcd(HearingGetResponse hearingResponse) {
        if (isHearingListed(hearingResponse)) {
            return State.HEARING;
        }
        if (isHearingAwaitingListing(hearingResponse)) {
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

    private boolean isHearingAwaitingListing(HearingGetResponse hearingResponse) {
        return AWAITING_LISTING.getListingCaseStatusLabel().equals(
            hearingResponse.getHearingResponse().getListingCaseStatus());
    }

    private static boolean isHearingListed(HearingGetResponse hearingResponse) {
        return LISTED.getListingCaseStatusLabel().equals(
            hearingResponse.getHearingResponse().getListingCaseStatus());
    }

    private static boolean isHearingListingStatusFixed(HearingGetResponse hearingResponse) {
        return FIXED.getListingStatusLabel().equals(
            hearingResponse.getHearingResponse().getListingStatus());
    }

    // todo clarify below ok
    private static boolean isHearingCancelled(HearingGetResponse hearingResponse) {
        return "Cancelled".equalsIgnoreCase(hearingResponse.getRequestDetails().getStatus())
            || nonNull(hearingResponse.getHearingResponse().getHearingCancellationReason());
    }
}
