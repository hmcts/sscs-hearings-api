package uk.gov.hmcts.reform.sscs.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.models.SubQueue;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequest;
import uk.gov.hmcts.reform.sscs.service.AppInsightsService;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty("flags.tribunals-to-hearings-api.enabled")
public class TribunalsHearingsEventDeadLetterQueueListener {

    private final TribunalsHearingEventQueueProperties tribunalsHearingEventQueueProperties;
    private final AppInsightsService appInsightsService;
    @Value("flag.tribunals-dead-letter-consumer.enabled") boolean deadLetterConsumer;

    @Bean
    public void processDeadLetterQueue() {
        ServiceBusReceiverClient sessionReceiver = tribunalsHearingsEventProcessorClientDeadLetter();
        CompletableFuture.runAsync(() -> {
            while (deadLetterConsumer) {
                consumeMessage(sessionReceiver);
            }
        });
    }

    public ServiceBusReceiverClient tribunalsHearingsEventProcessorClientDeadLetter() {
        log.info("Creating Events Dead Letter Queue Session receiver");
        ServiceBusReceiverClient client = new ServiceBusClientBuilder()
            .retryOptions(tribunalsHearingEventQueueProperties.retryOptions())
            .connectionString(tribunalsHearingEventQueueProperties.getConnectionString())
            .receiver()
            .queueName(tribunalsHearingEventQueueProperties.getQueueName())
            .subQueue(SubQueue.DEAD_LETTER_QUEUE)
            .buildClient();

        log.info("Dead Letter Queue Session receiver created, successfully");
        return client;
    }

    protected void consumeMessage(ServiceBusReceiverClient receiver) {
        log.info("Starting to consume message");
        receiver.receiveMessages(1).forEach(
            message -> {
                final String messageId = message.getMessageId();
                try {
                    log.info("Received Dead Letter Message with ID: {} ", messageId);
                    TribunalsDeadLetterMessage failMsg = obtainFailedMessage(message);
                    appInsightsService.sendAppInsightsEvent(failMsg);
                    receiver.complete(message);
                    log.info("Dead Letter Queue message with id '{}' handled successfully", messageId);
                } catch (JsonProcessingException ex) {
                    log.error("Error processing Dead Letter Queue message with id '{}' - "
                                  + "abandon the processing", messageId, ex);
                    receiver.abandon(message);
                }
            }
        );

    }

    private TribunalsDeadLetterMessage obtainFailedMessage(ServiceBusReceivedMessage message) {
        log.info("Obtaining Failed Message Information");
        HearingRequest hearingRequest = message.getBody().toObject(HearingRequest.class);
        Long caseId = Long.valueOf(hearingRequest.getCcdCaseId());
        return TribunalsDeadLetterMessage.builder()
            .messageId(message.getMessageId())
            .body(message.getBody().toString())
            .caseID(caseId)
            .build();
    }

}
