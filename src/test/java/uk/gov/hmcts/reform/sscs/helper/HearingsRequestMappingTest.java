package uk.gov.hmcts.reform.sscs.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RequestDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    @DisplayName("getVersion null return ParameterisedTest Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "true,null",
        "true,0",
        "true,-1",
        "false,1",
        "false,null",
        "false,0",
        "false,-1",
    }, nullValues = {"null"})
    void getVersion(boolean fieldNotNull, Long version) {
        // TODO Finish Test when method done
        SchedulingAndListingFields schedulingAndListingFields = null;
        if (fieldNotNull) {
            schedulingAndListingFields = SchedulingAndListingFields.builder()
                    .activeHearingVersionNumber(version)
                    .build();
        }

        SscsCaseData caseData = SscsCaseData.builder()
                .schedulingAndListingFields(schedulingAndListingFields)
                .build();

        Long result = HearingsRequestMapping.getVersion(caseData);

        assertNull(result);
    }
}
