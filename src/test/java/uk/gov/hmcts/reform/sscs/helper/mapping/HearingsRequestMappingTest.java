package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.Hearing;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingCancelRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RequestDetails;
import uk.gov.hmcts.reform.sscs.reference.data.model.CancellationReason;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HearingsRequestMappingTest extends HearingsMappingBase {

    @DisplayName("When a valid hearing wrapper is given buildHearingRequestDetails returns the correct Hearing Request")
    @Test
    void buildHearingRequestDetails() {
        SscsCaseData caseData = SscsCaseData.builder()
            .hearings(List.of(Hearing.builder()
                .value(HearingDetails.builder()
                    .hmcVersionNumber(1L)
                    .build())
                .build()))
            .build();
        HearingWrapper wrapper = HearingWrapper.builder()
                .caseData(caseData)
                .build();

        RequestDetails requestDetails = HearingsRequestMapping.buildHearingRequestDetails(wrapper);

        assertNotNull(requestDetails.getVersionNumber());
    }

    @DisplayName("When a valid hearing wrapper is given buildCancelHearingPayloadTest returns the correct Hearing Request Payload")
    @Test
    void buildCancelHearingPayloadTest() {
        HearingWrapper wrapper = HearingWrapper.builder()
                .cancellationReason(CancellationReason.OTHER)
                .build();
        HearingCancelRequestPayload result = HearingsRequestMapping.buildCancelHearingPayload(wrapper);

        assertThat(result).isNotNull();
        assertThat(result.getCancellationReasonCode()).isEqualTo(CancellationReason.OTHER);
    }
}
