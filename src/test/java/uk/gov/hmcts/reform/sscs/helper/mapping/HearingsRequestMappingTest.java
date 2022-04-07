package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingCancelRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RequestDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class HearingsRequestMappingTest extends HearingsMappingBase {

    public static final String CANCELLATION_REASON = "AWAITING_LISTING";

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
                .caseData(caseData)
                .caseData(caseData)
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
	
	
    @DisplayName("When a valid hearing wrapper is given buildDeleteHearingPayload returns the correct Hearing Request Payload")
    @Test
    void buildDeleteHearingPayloadTest() {
        HearingWrapper wrapper = HearingWrapper.builder()
                // .cancellationReasonCode(CANCEL_REASON_TEMP) // TODO: Uncomment when implemented.
                .build();
        HearingDeleteRequestPayload result = HearingsRequestMapping.buildDeleteHearingPayload(wrapper);

        assertThat(result).isNotNull();
        // assertThat(result.getCancellationReasonCode()).isEqualTo(CANCELLATION_REASON); // TODO: Uncomment when implemented.	
    }

    public static final String CANCELLATION_REASON = "AWAITING_LISTING";

    @DisplayName("When a valid hearing wrapper is given buildHearingRequestDetails returns the correct Hearing Request")
    @Test
    void buildHearingRequestDetails() {
        SscsCaseData caseData = SscsCaseData.builder()
                .schedulingAndListingFields(SchedulingAndListingFields.builder()
                        .activeHearingVersionNumber(1L)
                        .build())
                .build();
        HearingWrapper wrapper = HearingWrapper.builder()
                .caseData(caseData)
                .caseData(caseData)
                .build();

        RequestDetails requestDetails = HearingsRequestMapping.buildHearingRequestDetails(wrapper);

        assertNotNull(requestDetails.getVersionNumber());
    }

    @DisplayName("getVersion Test")
    @Test
    void getVersion() {
        SscsCaseData caseData = SscsCaseData.builder()
                .schedulingAndListingFields(SchedulingAndListingFields.builder()
                        .activeHearingVersionNumber(1L)
                        .build())
                .build();
        Long result = HearingsRequestMapping.getVersion(caseData);

        assertEquals(1L, result);
    }

    @DisplayName("getVersion null return ParameterisedTest Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "null",
        "0",
        "-1",
    }, nullValues = {"null"})
    void getVersion(Long version) {
        SscsCaseData caseData = SscsCaseData.builder()
                .schedulingAndListingFields(SchedulingAndListingFields.builder()
                        .activeHearingVersionNumber(version)
                        .build())
                .build();

        Long result = HearingsRequestMapping.getVersion(caseData);

        assertNull(result);
    }

    @DisplayName("When a valid hearing wrapper is given buildCancelHearingPayloadTest returns the correct Hearing Request Payload")
    @Test
    void buildCancelHearingPayloadTest() {
        HearingWrapper wrapper = HearingWrapper.builder()
                // .cancellationReasonCode(CANCEL_REASON_TEMP) // TODO: Uncomment when implemented.
                .build();
        HearingCancelRequestPayload result = HearingsRequestMapping.buildCancelHearingPayload(wrapper);

        assertThat(result).isNotNull();
        // assertThat(result.getCancellationReasonCode()).isEqualTo(CANCELLATION_REASON); // TODO: Uncomment when implemented.
    }
}
