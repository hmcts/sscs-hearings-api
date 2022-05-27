package uk.gov.hmcts.reform.sscs.config;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListener;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.model.hmcmessage.HmcMessage;

import java.nio.charset.StandardCharsets;

@Slf4j
@Data
@Configuration
@ConditionalOnProperty("flags.hmc-to-hearings-api.enabled")
public class HmcHearingsEventTopicListener {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Value("${azure.service-bus.hmc-to-hearings-api.maxRetries}")
    private Integer maxRetries;

    @Value("${sscs.serviceCode}")
    private String serviceCode;

    @JmsListener(
        destination = "${azure.service-bus.hmc-to-hearings-api.topicName}",
        subscription = "${azure.service-bus.hmc-to-hearings-api.subscriptionName}",
        containerFactory = "hmcHearingsEventTopicContainerFactory"
    )
    public void onMessage(byte[] messageBytes) throws JsonProcessingException, UpdateCaseException, GetCaseException {
        String message = new String(messageBytes, StandardCharsets.UTF_8);
        HmcMessage hmcMessage = OBJECT_MAPPER.readValue(message, HmcMessage.class);

        if (isMessageRelevantForService(hmcMessage, serviceCode)) {
            log.info("Processing hearing ID: {} for case reference: {}", hmcMessage.getHearingID(),
                hmcMessage.getCaseRef());
            processMessageWithRetry(hmcMessage, 1);
        }
    }

    private void processMessageWithRetry(HmcMessage hmcMessage, int retry) throws UpdateCaseException, GetCaseException {

        try {
            processMessage(hmcMessage);
        } catch (GetCaseException | UpdateCaseException e) {
            if (retry > maxRetries) {
                log.error("Maximum retries exceeded for case reference: {} ", hmcMessage.getCaseRef());
                throw e;
            } else {
                processMessageWithRetry(hmcMessage, ++retry);
            }
        }
    }

    private void processMessage(HmcMessage message) throws GetCaseException, UpdateCaseException{
        //process.
    }

    private boolean isMessageRelevantForService(HmcMessage hmcMessage, String serviceId) {
        return hmcMessage.getHmctsServiceCode().contains(serviceId);
    }

}
