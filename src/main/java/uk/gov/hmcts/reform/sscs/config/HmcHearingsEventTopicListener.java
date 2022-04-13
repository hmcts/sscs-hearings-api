package uk.gov.hmcts.reform.sscs.config;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusFailureReason;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.sscs.model.hmcmessage.HmcMessage;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

@Slf4j
@Data
@Configuration
@ConditionalOnProperty("flags.hmc-to-hearings-api.enabled")
public class HmcHearingsEventTopicListener {

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

    @Bean
    @SuppressWarnings("PMD.CloseResource")
    public void hmcHearingEventProcessorClient() {
        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
            .retryOptions(retryOptions())
            .connectionString(connectionString)
            .processor()
            .topicName(topicName)
            .subscriptionName(subscriptionName)
            .processMessage(HmcHearingsEventTopicListener::processMessage)
            .processError(HmcHearingsEventTopicListener::processError)
            .buildProcessorClient();

        processorClient.start();
        log.info("HMC hearing event processor started.");
    }

    public static void processMessage(ServiceBusReceivedMessageContext context) {
        ServiceBusReceivedMessage message = context.getMessage();
        HmcMessage hmcMessage = message.getBody().toObject(HmcMessage.class);
        String hmctsServiceID = hmcMessage.getHmctsServiceID();

        if (isMessageRelevantForService(hmcMessage, hmctsServiceID)) {
            //TODO process all messages: SSCS-10286
            log.info("Processing hearing ID: {} for case reference: {}%n", hmcMessage.getHearingID(),
                hmcMessage.getCaseRef());
        }
    }

    public static boolean isMessageRelevantForService(HmcMessage hmcMessage, String serviceId) {
        return hmcMessage.getHmctsServiceID().contains(serviceId);
    }

    public static void processError(ServiceBusErrorContext context) {
        log.error("Error when receiving messages from namespace: '{}'. Entity: '{}'%n",
                  context.getFullyQualifiedNamespace(), context.getEntityPath()
        );

        if (!(context.getException() instanceof ServiceBusException)) {
            log.error("Non-ServiceBusException occurred: {}%n", context.getException().toString());
            return;
        }

        ServiceBusException exception = (ServiceBusException) context.getException();
        ServiceBusFailureReason reason = exception.getReason();
        if (Objects.equals(reason, ServiceBusFailureReason.MESSAGING_ENTITY_DISABLED)
            || Objects.equals(reason, ServiceBusFailureReason.MESSAGING_ENTITY_NOT_FOUND)
            || Objects.equals(reason, ServiceBusFailureReason.UNAUTHORIZED)) {
            log.error("An unrecoverable error occurred. Stopping processing with reason {}: {}%n",
                      reason, exception.getMessage()
            );
        } else if (Objects.equals(reason, ServiceBusFailureReason.MESSAGE_LOCK_LOST)) {
            log.warn("Message lock lost for message: {}%n", context.getException().toString());
        } else {
            log.error("Error source {}, reason {}, message: {}%n", context.getErrorSource(),
                      reason, context.getException()
            );
        }
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
