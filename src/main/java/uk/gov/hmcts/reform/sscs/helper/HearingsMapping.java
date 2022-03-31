package uk.gov.hmcts.reform.sscs.helper;

import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDeleteRequestPayload;

public class HearingsMapping {

    private static String CANCEL_REASON_TEMP = "AWAITING_LISTING";

    public static HearingDeleteRequestPayload buildDeleteHearingPayload(HearingWrapper wrapper){
        HearingDeleteRequestPayload payload = new HearingDeleteRequestPayload();
        payload.setCancellationReasonCode(CANCEL_REASON_TEMP); // TODO: Get list of reasons E.g. wrapper.getCaseData().getCancellationCode();
        return payload;
    }

}
