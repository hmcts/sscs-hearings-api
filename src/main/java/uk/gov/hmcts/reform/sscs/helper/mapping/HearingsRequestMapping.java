package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDeleteRequestPayload;

@SuppressWarnings({"PMD.UnusedFormalParameter"})
// TODO Unsuppress in future
public final class HearingsRequestMapping {

    private HearingsRequestMapping() {

    }

    public static HearingDeleteRequestPayload buildDeleteHearingPayload(HearingWrapper wrapper) {
        return HearingDeleteRequestPayload.builder()
            .cancellationReasonCode(getCancellationReason(wrapper))
            .build();
    }

    public static String getCancellationReason(HearingWrapper wrapper) {
        // TODO Get Reason in Ticket: SSCS-10366
        return null;
    }

}
