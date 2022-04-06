package uk.gov.hmcts.reform.sscs.config;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.sscs.helper.QueueHelper;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingRequest;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

@Slf4j
@Data
@Configuration
public class HearingsEventQueueListener {

    @Value("${azure.hearings-queue.inboundConnectionString}")
    private String connectionString;
    @Value("${azure.hearings-queue.topicName}")
    private String topicName;
    @Value("${azure.hearings-queue.subscriptionName}")
    private String subscriptionName;
    @Value("${azure.hearings-queue.timeout}")
    private Duration tryTimeout;
    @Value("${azure.hearings-queue.delay}")
    private Duration delay;
    @Value("${azure.hearings-queue.retries}")
    private Integer maxRetries;

    public void receiveMessages() {
        CountDownLatch countdownLatch = new CountDownLatch(1);

        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
            .retryOptions(configureRetryOptions())
            .connectionString(connectionString)
            .processor()
            .topicName(topicName)
            .subscriptionName(subscriptionName)
            .processMessage(HearingsEventQueueListener::processMessage)
            .processError(context -> QueueHelper.processError(context, countdownLatch))
            .buildProcessorClient();

        processorClient.start();
    }

    private static void processMessage(ServiceBusReceivedMessageContext context) {
        ServiceBusReceivedMessage message = context.getMessage();
        HearingRequest hearingRequest = message.getBody().toObject(HearingRequest.class);
        log.info("Message {} received from Hearings Event Queue for Case ID {}", hearingRequest.toString(), hearingRequest.getCcdCaseId());
    }

    private AmqpRetryOptions configureRetryOptions() {
        AmqpRetryOptions amqpRetryOptions = new AmqpRetryOptions();

        if (tryTimeout != null)
            amqpRetryOptions.setTryTimeout(tryTimeout);
        if (maxRetries != null)
            amqpRetryOptions.setMaxRetries(maxRetries);
        if (delay != null)
            amqpRetryOptions.setDelay(delay);

        return amqpRetryOptions;
    }
}
