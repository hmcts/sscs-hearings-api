package uk.gov.hmcts.reform.sscs.helper;

import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDeleteRequestPayload;

public class HearingsMapping {

    private static String CANCEL_REASON_TEMP = "AWAITING_LISTING";

    private HearingsMapping() {

    }

    public static HearingDeleteRequestPayload buildDeleteHearingPayload(HearingWrapper wrapper) {
        return HearingDeleteRequestPayload.builder()
//            .cancellationReasonCode(CANCEL_REASON_TEMP)
            .build();
        // TODO: Get list of reasons E.g. wrapper.getCaseData().getCancellationCode(); Ticket: SSCS-10366
    }

}
