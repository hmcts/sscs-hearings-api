package uk.gov.hmcts.reform.sscs.service.servicebus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.sscs.model.hmc.message.HmcMessage;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class HmcEventMessageProcessServiceTest {

    public static final String SSCS_SERVICE_CODE = "BBA3";

    @InjectMocks
    private HmcEventMessageProcessService eventTopicService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(eventTopicService, "sscsServiceCode", SSCS_SERVICE_CODE);
    }

    @DisplayName("When the service code of a message matches the correct this services code "
        + "isMessageRelevantForService returns false")
    @Test
    void testMessageRelevantForService() {
        HmcMessage hmcMessage = HmcMessage.builder()
            .hmctsServiceCode(SSCS_SERVICE_CODE)
            .build();

        boolean result = eventTopicService.isMessageNotRelevantForService(hmcMessage);

        assertThat(result)
            .as("This message does not have the correct service ID.")
            .isFalse();
    }

    @DisplayName("When the service code of a message does not match this service's code "
        + "isMessageRelevantForService returns true")
    @ParameterizedTest
    @ValueSource(strings = {"PP4","SSA1"})
    @EmptySource
    void testMessageRelevantForService(String value) {
        HmcMessage hmcMessage = HmcMessage.builder()
            .hmctsServiceCode(value)
            .build();

        boolean result = eventTopicService.isMessageNotRelevantForService(hmcMessage);

        assertThat(result)
            .as("This service ID should not of matched.")
            .isTrue();
    }
}
