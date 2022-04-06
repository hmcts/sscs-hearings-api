package uk.gov.hmcts.reform.sscs.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.*;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RequestDetails.RequestDetailsBuilder;

import static java.util.Objects.nonNull;

@Component
public class HearingsRequestMapping {

    public HearingsRequestMapping() {

    }

    public RequestDetails buildHearingRequestDetails(HearingWrapper wrapper) {
        SscsCaseData caseData = wrapper.getUpdatedCaseData();
        RequestDetailsBuilder hmcRequestDetailsBuilder = RequestDetails.builder();
        hmcRequestDetailsBuilder.versionNumber(getVersion(caseData));
        return hmcRequestDetailsBuilder.build();
    }

    public Long getVersion(SscsCaseData caseData) {
        if (nonNull(caseData.getSchedulingAndListingFields())
                && nonNull(caseData.getSchedulingAndListingFields().getActiveHearingVersionNumber())
                && caseData.getSchedulingAndListingFields().getActiveHearingVersionNumber() > 0) {
            return caseData.getSchedulingAndListingFields().getActiveHearingVersionNumber();
        } else {
            return null;
        }
    }
}
