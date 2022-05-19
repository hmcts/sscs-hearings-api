package uk.gov.hmcts.reform.sscs.config;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.*;
import com.azure.messaging.servicebus.models.SubQueue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.sscs.ccd.domain.Hearing;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingRoute;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingState;
import uk.gov.hmcts.reform.sscs.model.HmcFailureMessage;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequest;
import uk.gov.hmcts.reform.sscs.service.AppInsightsService;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
//@ConditionalOnProperty("flags.tribunals-to-hearings-api.enabled")
public class TribunalsHearingsEventDeadLetterQueueListener {

    private boolean keepRun = true;

    private final TribunalsHearingEventQueueProperties tribunalsHearingEventQueueProperties;
    private final AppInsightsService appInsightsService;

    @Bean
    public void processDeadLetterQueue() throws JsonProcessingException {
        sendMessage();
        try (ServiceBusReceiverClient sessionReceiver = tribunalsHearingsEventProcessorClientDeadLetter()) {
            while (keepRun) {
                consumeMessage(sessionReceiver);
            }
        } catch (Exception ex) {
            log.error("Error Processing Queue: " + ex);
        }
    }

    void sendMessage() throws JsonProcessingException {
        /* TESTER METHOD DO NOT MERGE */
        HearingRequest test = HearingRequest.builder("0101001").hearingRoute(HearingRoute.GAPS).hearingState(HearingState.CANCEL_HEARING).build();
        ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
            .connectionString(tribunalsHearingEventQueueProperties.getConnectionString())
            .sender()
            .queueName(tribunalsHearingEventQueueProperties.getQueueName())
            .buildClient();

        ObjectMapper mapper = new ObjectMapper();
        ServiceBusMessage message = new ServiceBusMessage(mapper.writeValueAsString(test));
        message.setContentType("application/json");
        senderClient.sendMessage(message);
        System.out.println("Sent a single message to the queue: " + tribunalsHearingEventQueueProperties.getQueueName());
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
                    final String messageBody = message.getBody().toString();
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
