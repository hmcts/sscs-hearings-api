package uk.gov.hmcts.reform.sscs.config;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.InvalidIdException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.model.messaging.HmcMessage;
import uk.gov.hmcts.reform.sscs.service.ccdupdate.HearingsJourneyService;

import java.time.Duration;

@Slf4j
@Data
@Configuration
@ConditionalOnProperty("flags.hmc-to-hearings-api.enabled")
public class HmcHearingsEventTopicListener {


    @Autowired
    private HearingsJourneyService hearingsJourneyService;
    @Value("${azure.service-bus.hmc-to-hearings-api.connectionString}")
    private String connectionString;
    @Value("${azure.service-bus.hmc-to-hearings-api.topicName}")
    private String topicName;
    @Value("${azure.service-bus.hmc-to-hearings-api.subscriptionName}")
    private String subscriptionName;
    @Value("${azure.service-bus.hmc-to-hearings-api.retryTimeout}")
    private Long retryTimeout;
    @Value("${azure.service-bus.hmc-to-hearings-api.retryDelay}")
    private Long retryDelay;
    @Value("${azure.service-bus.hmc-to-hearings-api.maxRetries}")
    private Integer maxRetries;
    @Value("${sscs.serviceCode}")
    private String serviceId;

    public void processMessage(ServiceBusReceivedMessageContext context) {
        ServiceBusReceivedMessage message = context.getMessage();
        HmcMessage hmcMessage = message.getBody().toObject(HmcMessage.class);
        String hmctsServiceID = hmcMessage.getHmctsServiceID();

        if (isMessageRelevantForService(hmcMessage, hmctsServiceID)) {
            try {
                log.info("Processing hearing ID: {} for case reference: {}", hmcMessage.getHearingID(),
                         hmcMessage.getCaseRef()
                );

                hearingsJourneyService.process(hmcMessage);

                log.info("Hearing message {} processed for case reference {}", hmcMessage.getHearingID(),
                         hmcMessage.getCaseRef()
                );

                context.complete();
            } catch (GetCaseException | UpdateCaseException | InvalidIdException exc) {
                log.error("An exception occurred whilst processing hearing event for hearing ID {}, case reference: {}",
                    hmcMessage.getHearingID(),
                    hmcMessage.getCaseRef(),
                    exc);
                context.abandon();
            }
        } else {
            log.info("Nothing updated for hearing ID: {} for case reference: {}", hmcMessage.getHearingID(),
                     hmcMessage.getCaseRef()
            );
        }
    }

    public static boolean isMessageRelevantForService(HmcMessage hmcMessage, String serviceId) {
        return hmcMessage.getHmctsServiceID().contains(serviceId);
    }

    @Bean
    @SuppressWarnings("PMD.CloseResource")
    public ServiceBusProcessorClient hmcHearingEventProcessorClient() {
        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
            .retryOptions(retryOptions())
            .connectionString(connectionString)
            .processor()
            .topicName(topicName)
            .subscriptionName(subscriptionName)
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .disableAutoComplete()
            .processMessage(this::processMessage)
            .processError(QueueHelper::processError)
            .buildProcessorClient();

        processorClient.start();
        log.info("HMC hearing event processor started.");

        return processorClient;
    }

    private AmqpRetryOptions retryOptions() {
        AmqpRetryOptions amqpRetryOptions = new AmqpRetryOptions();
        amqpRetryOptions.setMode(AmqpRetryMode.EXPONENTIAL);
        amqpRetryOptions.setTryTimeout(Duration.ofMinutes(retryTimeout));
        amqpRetryOptions.setMaxRetries(maxRetries);
        amqpRetryOptions.setDelay(Duration.ofSeconds(retryDelay));

        return amqpRetryOptions;
    }
}
