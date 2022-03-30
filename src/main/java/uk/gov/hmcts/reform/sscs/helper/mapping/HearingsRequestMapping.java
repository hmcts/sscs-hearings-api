package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.*;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RequestDetails.RequestDetailsBuilder;

import static java.util.Objects.nonNull;

@SuppressWarnings({"PMD.UnusedFormalParameter"})
// TODO Unsuppress in future
public final class HearingsRequestMapping {

    private HearingsRequestMapping() {

    public static RequestDetails buildHearingRequestDetails(HearingWrapper wrapper) {
        RequestDetailsBuilder hmcRequestDetailsBuilder = RequestDetails.builder();
        hmcRequestDetailsBuilder.versionNumber(getVersion(wrapper.getCaseData()));
        return hmcRequestDetailsBuilder.build();
    }

    public static HearingCancelRequestPayload buildCancelHearingPayload(HearingWrapper wrapper) {
        return HearingCancelRequestPayload.builder()
            .cancellationReasonCode(getCancellationReason(wrapper))
            .build();
    }

    public static Long getVersion(SscsCaseData caseData) {
        if (nonNull(caseData.getSchedulingAndListingFields().getActiveHearingVersionNumber())
                && caseData.getSchedulingAndListingFields().getActiveHearingVersionNumber() > 0) {
            return caseData.getSchedulingAndListingFields().getActiveHearingVersionNumber();
        }
        return null;
    }

    public static String getCancellationReason(HearingWrapper wrapper) {
        // TODO Get Reason in Ticket: SSCS-10366
        return null;
    }
}
