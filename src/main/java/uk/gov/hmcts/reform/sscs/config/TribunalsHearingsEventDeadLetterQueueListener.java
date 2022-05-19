package uk.gov.hmcts.reform.sscs.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.models.SubQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequest;
import uk.gov.hmcts.reform.sscs.service.AppInsightsService;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty("flags.tribunals-to-hearings-api.enabled")
public class TribunalsHearingsEventDeadLetterQueueListener {

    private final TribunalsHearingEventQueueProperties tribunalsHearingEventQueueProperties;
    private final AppInsightsService appInsightsService;
    private boolean keepRun = true;

    @Bean
    public void processDeadLetterQueue() {
        try (ServiceBusReceiverClient sessionReceiver = tribunalsHearingsEventProcessorClientDeadLetter()) {
            while (keepRun) {
                consumeMessage(sessionReceiver);
            }
        } catch (Exception ex) {
            log.error("Error Processing Queue: " + ex);
        }
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
        try {
            receiver.receiveMessages(1).forEach(
                message -> {
                    final String messageId = message.getMessageId();
                    try {
                        log.info("Received Dead Letter Message with ID: {} ", messageId);
                        TribunalsDeadLetterMessage failMsg = obtainFailedMessage(message);
                        appInsightsService.sendAppInsightsEvent(failMsg);
                        receiver.complete(message);
                        log.info("Dead Letter Queue message with id '{}' handled successfully", messageId);
                    } catch (Exception ex) {
                        log.error("Error processing Dead Letter Queue message with id '{}' - "
                                      + "abandon the processing", messageId);
                        receiver.abandon(message);
                    }
                }
            );
        } catch (Exception ex) {
            log.info("Failed to consume message with error {}", ex);
        }
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

    public void stop() {
        keepRun = false;
    }

}
