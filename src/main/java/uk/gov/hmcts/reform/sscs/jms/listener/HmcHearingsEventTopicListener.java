package uk.gov.hmcts.reform.sscs.jms.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.jms.message.JmsBytesMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.exception.HmcEventProcessingException;
import uk.gov.hmcts.reform.sscs.model.hmcmessage.HmcMessage;
import uk.gov.hmcts.reform.sscs.service.hmc.topic.ProcessHmcMessageService;

import java.nio.charset.StandardCharsets;
import javax.jms.JMSException;

@Slf4j
@Component
@ConditionalOnProperty("flags.hmc-to-hearings-api.enabled")
public class HmcHearingsEventTopicListener {

    private final ObjectMapper objectMapper;

    private final String serviceCode;

    private final ProcessHmcMessageService processHmcMessageService;

    public HmcHearingsEventTopicListener(@Value("${sscs.serviceCode}") String serviceCode,
                                         ProcessHmcMessageService processHmcMessageService) {
        this.serviceCode = serviceCode;
        this.processHmcMessageService = processHmcMessageService;
        this.objectMapper = new ObjectMapper();
    }

    @JmsListener(
        destination = "${azure.service-bus.hmc-to-hearings-api.topicName}",
        subscription = "${azure.service-bus.hmc-to-hearings-api.subscriptionName}",
        containerFactory = "hmcHearingsEventTopicContainerFactory"
    )
    public void onMessage(JmsBytesMessage message) throws JMSException, HmcEventProcessingException {

        byte[] messageBytes = new byte[(int) message.getBodyLength()];
        message.readBytes(messageBytes);
        String convertedMessage = new String(messageBytes, StandardCharsets.UTF_8);

        try {
            HmcMessage hmcMessage = objectMapper.readValue(convertedMessage, HmcMessage.class);

            if (isMessageRelevantForService(hmcMessage, serviceCode)) {
                log.info("Processing hearing ID: {} for case reference: {}", hmcMessage.getHearingID(),
                    hmcMessage.getCaseRef());

                processHmcMessageService.processEventMessage(hmcMessage);
            }
        }  catch (JsonProcessingException | HmcEventProcessingException ex) {
            throw new HmcEventProcessingException(String.format("Unable to successfully deliver HMC message: %s",
                convertedMessage), ex);
        }
    }

    private boolean isMessageRelevantForService(HmcMessage hmcMessage, String serviceCode) {
        return serviceCode.equals(hmcMessage.getHmctsServiceCode());
    }

}
