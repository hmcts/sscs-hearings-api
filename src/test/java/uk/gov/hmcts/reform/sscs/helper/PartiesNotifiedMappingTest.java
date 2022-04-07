package uk.gov.hmcts.reform.sscs.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.SchedulingAndListingFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.partiesnotified.PartiesNotified;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PartiesNotifiedMappingTest extends HearingsMappingBase {

    public static final String VERSION_NUMBER = "1";

    @DisplayName("When a valid hearing wrapper is given buildUpdatePartiesNotifiedPayload returns the correct Hearing Request")
    @Test
    void buildUpdatePartiesNotifiedPayload() {

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

        PartiesNotified partiesNotified = PartiesNotifiedMapping.buildUpdatePartiesNotifiedPayload(wrapper);

        assertNotNull(partiesNotified.getRequestVersion());
    }


    @DisplayName("getVersionNumber Test")
    @Test
    void getVersionNumber() {
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
        String result = PartiesNotifiedMapping.getVersionNumber(wrapper);

        assertEquals(VERSION_NUMBER, result);
    }

}
