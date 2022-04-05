package uk.gov.hmcts.reform.sscs.config;

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
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.sscs.model.hmcmessage.HmcMessage;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
@Data
@Configuration
public class HmcHearingsEventTopicListener {

    @Value("${azure.service-bus.connectionString}")
    private String connectionString;
    @Value("${azure.service-bus.topicName}")
    private String topicName;
    @Value("${azure.service-bus.subscriptionName}")
    private String subscriptionName;

    //TODO add @Bean and add correct values (connectionString,topicName,subscriptionName) in application.yml
    @SuppressWarnings({"PMD.CloseResource"})
    public void receiveMessages() {
        CountDownLatch countdownLatch = new CountDownLatch(1);

        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .processor()
            .topicName(topicName)
            .subscriptionName(subscriptionName)
            .processMessage(HmcHearingsEventTopicListener::processMessage)
            .processError(context -> processError(context, countdownLatch))
            .buildProcessorClient();

        processorClient.start();
    }

    public static void processMessage(ServiceBusReceivedMessageContext context) {
        ServiceBusReceivedMessage message = context.getMessage();
        HmcMessage hmcMessage = message.getBody().toObject(HmcMessage.class);
        String hmctsServiceID = hmcMessage.getHmctsServiceID();

        if (hmctsServiceID.contains("BBA3")) {
            //TODO process all messages with BBA3
            log.info("Processing hearing #: {}. and case #: {}%n", hmcMessage.getHearingID(), hmcMessage.getCaseRef());
        }

        log.info("Processing message. Session: {}, Sequence #: {}. Contents: {}%n", message.getMessageId(),
                 message.getSequenceNumber(), message.getBody()
        );
    }

    private void processError(ServiceBusErrorContext context, CountDownLatch countdownLatch) {
        log.error("Error when receiving messages from namespace: '{}'. Entity: '{}'%n",
                  context.getFullyQualifiedNamespace(), context.getEntityPath()
        );

        if (!(context.getException() instanceof ServiceBusException)) {
            log.warn("Non-ServiceBusException occurred: {}%n", context.getException().toString());
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
            countdownLatch.countDown();
        } else if (Objects.equals(reason, ServiceBusFailureReason.MESSAGE_LOCK_LOST)) {
            log.warn("Message lock lost for message: {}%n", context.getException().toString());
        } else if (Objects.equals(reason, ServiceBusFailureReason.SERVICE_BUSY)) {
            try {
                SECONDS.sleep(1);
            } catch (InterruptedException e) {
                log.warn("Unable to sleep for period of time");
            }
        } else {
            log.error("Error source {}, reason {}, message: {}%n", context.getErrorSource(),
                      reason, context.getException()
            );
        }
    }
}
