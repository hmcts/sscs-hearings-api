package uk.gov.hmcts.reform.sscs.config;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.jms.message.JmsBytesMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListener;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.model.hmcmessage.HmcMessage;

import javax.jms.JMSException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Data
@Configuration
@ConditionalOnProperty("flags.hmc-to-hearings-api.enabled")
public class HmcHearingsEventTopicListener {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Value("${sscs.serviceCode}")
    private String serviceCode;

    @JmsListener(
        destination = "${azure.service-bus.hmc-to-hearings-api.topicName}",
        subscription = "${azure.service-bus.hmc-to-hearings-api.subscriptionName}",
        containerFactory = "hmcHearingsEventTopicContainerFactory"
    )
    public void onMessage(JmsBytesMessage message) throws JMSException {

        byte[] messageBytes = new byte[(int) message.getBodyLength()];
        message.readBytes(messageBytes);
        String convertedMessage = new String(messageBytes, StandardCharsets.UTF_8);

        try {
            HmcMessage hmcMessage = OBJECT_MAPPER.readValue(convertedMessage, HmcMessage.class);

            if (isMessageRelevantForService(hmcMessage, serviceCode)) {
                log.info("Processing hearing ID: {} for case reference: {}", hmcMessage.getHearingID(),
                    hmcMessage.getCaseRef());

                processMessage(hmcMessage);
            }
        }  catch (GetCaseException | UpdateCaseException | JsonProcessingException ex) {
            log.error("Unable to successfully deliver HMC message: {}", convertedMessage);
            throw new JMSException(ex.getMessage());
        }
    }


    private void processMessage(HmcMessage message) throws GetCaseException, UpdateCaseException{
        //process.
    }

    private boolean isMessageRelevantForService(HmcMessage hmcMessage, String serviceCode) {
        return serviceCode.equals(hmcMessage.getHmctsServiceCode());
    }

}
