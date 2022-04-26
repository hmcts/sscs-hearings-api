package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDeleteRequestPayload;

import static org.assertj.core.api.Assertions.assertThat;

class HearingsRequestMappingTest {

    public static final String CANCELLATION_REASON = "AWAITING_LISTING";

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

}
