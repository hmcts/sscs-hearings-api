package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.SchedulingAndListingFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.partiesnotified.PartiesNotifiedRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PartiesNotifiedMappingTest extends HearingsMappingBase {
    private static final Long VERSION_NUMBER = 1L;

    @DisplayName("When a valid hearing wrapper is given buildUpdatePartiesNotifiedPayload returns the correct Hearing Request")
    @Test
    void buildUpdatePartiesNotifiedPayload() {

        SscsCaseData caseData = SscsCaseData.builder()
                .schedulingAndListingFields(SchedulingAndListingFields.builder()
                        .activeHearingVersionNumber(VERSION)
                        .build())
                .build();
        HearingWrapper wrapper = HearingWrapper.builder()
                .caseData(caseData)
                .updatedCaseData(caseData)
                .build();

        PartiesNotifiedRequest partiesNotifiedRequest = PartiesNotifiedMapping.buildUpdatePartiesNotifiedPayload(wrapper);

        assertNotNull(partiesNotifiedRequest.getRequestVersion());
    }


    @DisplayName("getVersionNumber Test")
    @Test
    void getVersionNumber() {

        SscsCaseData caseData = SscsCaseData.builder()
                .schedulingAndListingFields(SchedulingAndListingFields.builder()
                        .activeHearingVersionNumber(VERSION)
                        .build())
                .build();

        HearingWrapper wrapper = HearingWrapper.builder()
                .caseData(caseData)
                .updatedCaseData(caseData)
                .build();
        Long result = PartiesNotifiedMapping.getVersionNumber(wrapper);

        assertEquals(VERSION_NUMBER, result);
    }
}
