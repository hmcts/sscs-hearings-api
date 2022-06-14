package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.helper.service.HearingsServiceHelper;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingCancelRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RequestDetails;
import uk.gov.hmcts.reform.sscs.reference.data.model.CancellationReason;

@SuppressWarnings({"PMD.UnusedFormalParameter"})
// TODO Unsuppress in future
public final class HearingsRequestMapping {

    private HearingsRequestMapping() {
    }

    public static RequestDetails buildHearingRequestDetails(HearingWrapper wrapper) {
        RequestDetails.RequestDetailsBuilder hmcRequestDetailsBuilder = RequestDetails.builder();
        hmcRequestDetailsBuilder.versionNumber(HearingsServiceHelper.getVersion(wrapper));
        return hmcRequestDetailsBuilder.build();
    }

    public static HearingCancelRequestPayload buildCancelHearingPayload(HearingWrapper wrapper) {
        return HearingCancelRequestPayload.builder()
            .cancellationReasonCode(getCancellationReason(wrapper))
            .build();
    }

    public static CancellationReason getCancellationReason(HearingWrapper wrapper) {
        return wrapper.getCancellationReason();
    }
}
