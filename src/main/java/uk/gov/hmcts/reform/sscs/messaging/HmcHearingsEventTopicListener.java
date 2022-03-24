package uk.gov.hmcts.reform.sscs.messaging;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusFailureReason;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Data
@SuppressWarnings({
    "PMD.LawOfDemeter",
    "PMD.CompareObjectsWithEquals",
    "PMD.CloseResource"
})
public final class HmcHearingsEventTopicListener {

    private final HmcHearingsTopicListenerProperties properties;

    public void receiveMessages() {
        CountDownLatch countdownLatch = new CountDownLatch(1);

        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
            .connectionString(properties.getConnectionString())
            .processor()
            .topicName(properties.getTopicName())
            .subscriptionName(properties.getSubName())
            .processMessage(HmcHearingsEventTopicListener::processMessage)
            .processError(context -> processError(context, countdownLatch))
            .buildProcessorClient();

        processorClient.start();
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            log.error("Client processing interrupted: {}%n ", e.getMessage());
        }
        processorClient.close();
    }

    private static void processMessage(ServiceBusReceivedMessageContext context) {
        ServiceBusReceivedMessage message = context.getMessage();
        log.info("Processing message. Session: {}, Sequence #: {}. Contents: {}%n", message.getMessageId(),
                 message.getSequenceNumber(), message.getBody());
    }

    private void processError(ServiceBusErrorContext context, CountDownLatch countdownLatch) {
        log.error("Error when receiving messages from namespace: '{}'. Entity: '{}'%n",
                  context.getFullyQualifiedNamespace(), context.getEntityPath());

        if (!(context.getException() instanceof ServiceBusException)) {
            log.warn("Non-ServiceBusException occurred: {}%n", context.getException().toString());
            return;
        }

        ServiceBusException exception = (ServiceBusException) context.getException();
        ServiceBusFailureReason reason = exception.getReason();

        if (reason == ServiceBusFailureReason.MESSAGING_ENTITY_DISABLED
            || reason == ServiceBusFailureReason.MESSAGING_ENTITY_NOT_FOUND
            || reason == ServiceBusFailureReason.UNAUTHORIZED) {
            log.error("An unrecoverable error occurred. Stopping processing with reason {}: {}%n",
                      reason, exception.getMessage());
            countdownLatch.countDown();
        } else if (reason == ServiceBusFailureReason.MESSAGE_LOCK_LOST) {
            log.warn("Message lock lost for message: {}%n", context.getException().toString());
        } else if (reason == ServiceBusFailureReason.SERVICE_BUSY) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                log.warn("Unable to sleep for period of time");
            }
        } else {
            log.error("Error source {}, reason {}, message: {}%n", context.getErrorSource(),
                      reason, context.getException());
        }
    }
}
