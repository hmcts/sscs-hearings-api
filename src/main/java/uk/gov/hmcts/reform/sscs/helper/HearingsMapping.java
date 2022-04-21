package uk.gov.hmcts.reform.sscs.helper;

import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDeleteRequestPayload;

public class HearingsMapping {

    public static HearingDeleteRequestPayload buildDeleteHearingPayload(String cancellationReason) {
        return HearingDeleteRequestPayload.builder()
            .cancellationReasonCode(cancellationReason)
            .build();
        // TODO: Get Reason in Ticket: SSCS-10366
    }

}
