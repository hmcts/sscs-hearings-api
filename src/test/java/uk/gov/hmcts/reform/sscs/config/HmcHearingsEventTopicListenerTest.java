package uk.gov.hmcts.reform.sscs.config;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.reform.sscs.model.hmcmessage.HmcMessage;

import static org.assertj.core.api.Assertions.assertThat;

class HmcHearingsEventTopicListenerTest {

    @ParameterizedTest
    @CsvSource({
        "BBA3,BBA3,true",
        "BBA3,SSA1,false"
    })
    void testMessageRelevantForService(String messageServiceId, String serviceId, boolean expected) {
        HmcMessage hmcMessage = HmcMessage.builder()
            .hmctsServiceID(messageServiceId)
            .build();

        boolean isMessageRelevantForService = HmcHearingsEventTopicListener
            .isMessageRelevantForService(hmcMessage, serviceId);

        assertThat(isMessageRelevantForService)
            .as("Only messages containing the service's service ID should be processed.")
            .isEqualTo(expected);
    }
}
