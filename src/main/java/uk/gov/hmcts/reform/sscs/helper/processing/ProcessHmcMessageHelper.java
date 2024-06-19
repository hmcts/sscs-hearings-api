package uk.gov.hmcts.reform.sscs.helper.processing;

import uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.AWAITING_LISTING;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.CANCELLED;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.EXCEPTION;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.LISTED;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.UPDATE_SUBMITTED;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.ListingStatus.CNCL;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.ListingStatus.FIXED;

public final class ProcessHmcMessageHelper {

    private ProcessHmcMessageHelper() {
    }

    public static boolean stateNotHandled(HmcStatus hmcStatus, HearingGetResponse hearingResponse) {
        return !(isHearingUpdated(hmcStatus, hearingResponse) || isHearingCancelled(hmcStatus, hearingResponse)
            || isStatusException(hmcStatus));
    }

    public static boolean isHearingUpdated(HmcStatus hmcStatus, HearingGetResponse hearingResponse) {
        return isHearingListedOrUpdateSubmitted(hmcStatus)
            && isStatusFixed(hearingResponse);
    }

    private static boolean isHearingListedOrUpdateSubmitted(HmcStatus hmcStatus) {
        return hmcStatus == LISTED || hmcStatus == AWAITING_LISTING || hmcStatus == UPDATE_SUBMITTED;
    }

    private static boolean isStatusFixed(HearingGetResponse hearingResponse) {
        return FIXED == hearingResponse.getHearingResponse().getListingStatus();
    }

    private static boolean isHearingCancelled(HmcStatus hmcStatus, HearingGetResponse hearingResponse) {
        return hmcStatus == CANCELLED
            || isNotEmpty(hearingResponse.getRequestDetails().getCancellationReasonCodes())
            || CNCL == hearingResponse.getHearingResponse().getListingStatus();
    }

    public static boolean isStatusException(HmcStatus hmcStatus) {
        return hmcStatus == EXCEPTION;
    }
}
