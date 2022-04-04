package uk.gov.hmcts.reform.sscs.helper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDeleteRequestPayload;

@ExtendWith(MockitoExtension.class)
public class HearingsMappingTest {

    private HearingsMapping hearingsMapping;

    @BeforeEach
    void setUp() {
        hearingsMapping = new HearingsMapping();
    }

    @DisplayName("When a valid hearing wrapper is given buildDeleteHearingPayload returns the correct Hearing Request Payload")
    @Test
    void buildDeleteHearingPayloadTest() {
        String CANCELLATION_REASON = "AWAITING_LISTING";
        HearingWrapper hearingWrapper = HearingWrapper.builder().build();
        HearingDeleteRequestPayload payload = hearingsMapping.buildDeleteHearingPayload(hearingWrapper);
        assertThat(payload.getCancellationReasonCode()).isEqualTo(CANCELLATION_REASON);
    }

}
