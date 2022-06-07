package uk.gov.hmcts.reform.sscs.config;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingState;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequest;
import uk.gov.hmcts.reform.sscs.service.HearingsService;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@ConditionalOnProperty("flags.tribunals-to-hearings-api.enabled")
@RequiredArgsConstructor
public class OldTribunalsHearingsEventQueueListenerConfig {

    private final HearingsService hearingsService;

    @Value("${azure.service-bus.tribunals-to-hearings-api.connectionString}")
    private String connectionString;
    @Value("${azure.service-bus.tribunals-to-hearings-api.queueName}")
    private String queueName;

    @Value("${azure.service-bus.tribunals-to-hearings-api.retryTimeout}")
    private Long retryTimeout;
    @Value("${azure.service-bus.tribunals-to-hearings-api.retryDelay}")
    private Long retryDelay;
    @Value("${azure.service-bus.tribunals-to-hearings-api.maxRetries}")
    private Integer maxRetries;

    @EventListener(ApplicationReadyEvent.class)
    @SuppressWarnings("PMD.CloseResource")
    public void tribunalsHearingsEventProcessorClient() {
        ServiceBusSessionReceiverClient receiverClient = new ServiceBusClientBuilder()
            .retryOptions(retryOptions())
            .connectionString(connectionString)
            .sessionReceiver()
            .queueName(queueName)
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .disableAutoComplete()
            .buildClient();

        log.info("Tribunals hearings event queue receiver starting.");

        CompletableFuture.runAsync(() -> {
            while (true) {
                processMessage(receiverClient);
            }
        });
    }

    private void processMessage(ServiceBusSessionReceiverClient  receiverClient) {

        try (ServiceBusReceiverClient receiver = receiverClient.acceptNextSession()) {
            receiver.receiveMessages(1).forEach(message -> {
                try {
                    HearingRequest hearingRequest = message.getBody().toObject(HearingRequest.class);
                    String caseId = hearingRequest.getCcdCaseId();
                    HearingState event = hearingRequest.getHearingState();
                    log.info("Attempting to process hearing event {} from hearings event queue for case ID {}",
                        event, caseId);

                    hearingsService.processHearingRequest(hearingRequest);

                    receiver.complete(message);
                    log.info("Hearing event {} for case ID {} successfully processed", event, caseId);
                } catch (Exception ex) {
                    log.error("An exception occurred whilst processing hearing event for case ID {}."
                        + " Abandoning message", message.getMessageId(), ex);
                    receiver.abandon(message);
                }
            });
        } catch (UnsupportedOperationException ex) {
            log.error("This queue does not have sessions enabled.", ex);
        } catch (AmqpException ex) {
            log.error("Receiver timed out accepting next session.", ex);
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
