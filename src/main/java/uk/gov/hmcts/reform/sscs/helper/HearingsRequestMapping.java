package uk.gov.hmcts.reform.sscs.helper;

import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.*;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RequestDetails.RequestDetailsBuilder;

<<<<<<< HEAD
=======
import static java.util.Objects.nonNull;

>>>>>>> 9692bd47575ca92d430a80443fd0fbc7af1611a8
public final class HearingsRequestMapping {

    private HearingsRequestMapping() {

    }

    public static RequestDetails buildHearingRequestDetails(HearingWrapper wrapper) {
        SscsCaseData caseData = wrapper.getUpdatedCaseData();
        RequestDetailsBuilder hmcRequestDetailsBuilder = RequestDetails.builder();
        hmcRequestDetailsBuilder.versionNumber(getVersion(caseData));
        return hmcRequestDetailsBuilder.build();
    }

<<<<<<< HEAD
    private static Long getVersion(SscsCaseData caseData) {
        return caseData.getSchedulingAndListingFields().getActiveHearingVersionNumber();
=======
    public static Long getVersion(SscsCaseData caseData) {
        if (nonNull(caseData.getSchedulingAndListingFields())
                && nonNull(caseData.getSchedulingAndListingFields().getActiveHearingVersionNumber())
                && caseData.getSchedulingAndListingFields().getActiveHearingVersionNumber() > 0) {
            return caseData.getSchedulingAndListingFields().getActiveHearingVersionNumber();
        } else {
            return null;
        }
>>>>>>> 9692bd47575ca92d430a80443fd0fbc7af1611a8
    }
}
