package uk.gov.hmcts.reform.sscs.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RequestDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HearingsRequestMappingTest extends HearingsMappingBase {

    @DisplayName("When a valid hearing wrapper is given buildHearingRequestDetails returns the correct Hearing Request")
    @Test
    void buildHearingRequestDetails() {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder()
                .schedulingAndListingFields(SchedulingAndListingFields.builder()
                        .activeHearingVersionNumber(VERSION)
                        .build())
                .build();
        HearingWrapper wrapper = HearingWrapper.builder()
                .originalCaseData(caseData)
                .updatedCaseData(caseData)
                .build();

        RequestDetails requestDetails = HearingsRequestMapping.buildHearingRequestDetails(wrapper);

        assertNotNull(requestDetails.getVersionNumber());
    }

    @DisplayName("getVersion Test")
    @Test
    void getVersion() {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder()
                .schedulingAndListingFields(SchedulingAndListingFields.builder()
                        .activeHearingVersionNumber(VERSION)
                        .build())
                .build();
        Long result = HearingsRequestMapping.getVersion(caseData);

        assertEquals(VERSION, result);
    }
}
