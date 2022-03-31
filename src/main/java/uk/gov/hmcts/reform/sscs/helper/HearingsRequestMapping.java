package uk.gov.hmcts.reform.sscs.helper;

import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.*;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RequestDetails.RequestDetailsBuilder;

public final class HearingsRequestMapping {

    private HearingsRequestMapping() {

    }

    public static RequestDetails buildHearingRequestDetails(HearingWrapper wrapper) {
        SscsCaseData caseData = wrapper.getUpdatedCaseData();
        RequestDetailsBuilder hmcRequestDetailsBuilder = RequestDetails.builder();
        hmcRequestDetailsBuilder.versionNumber(getVersion(caseData));
        return hmcRequestDetailsBuilder.build();
    }

    public static Long getVersion(SscsCaseData caseData) {
        return caseData.getSchedulingAndListingFields().getActiveHearingVersionNumber();
    }
}
