package uk.gov.hmcts.reform.sscs.config;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingState;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.InvalidIdException;
import uk.gov.hmcts.reform.sscs.exception.UnhandleableHearingStateException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequest;
import uk.gov.hmcts.reform.sscs.service.HearingsService;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@ConditionalOnProperty("flags.tribunals-to-hearings-api.enabled")
@RequiredArgsConstructor
public class TribunalsHearingsEventQueueListenerConfig {

    private final HearingsService hearingsService;
    private final TribunalsHearingEventQueueProperties tribunalsHearingEventQueueProperties;

    @EventListener(ApplicationReadyEvent.class)
    @SuppressWarnings("PMD.CloseResource")
    public void tribunalsHearingsEventProcessorClient() {
        ServiceBusSessionReceiverClient receiverClient = new ServiceBusClientBuilder()
            .retryOptions(tribunalsHearingEventQueueProperties.retryOptions())
            .connectionString(tribunalsHearingEventQueueProperties.getConnectionString())
            .sessionReceiver()
            .queueName(tribunalsHearingEventQueueProperties.getQueueName())
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
                } catch (UnhandleableHearingStateException | UpdateCaseException |
                    GetCaseException | InvalidIdException ex) {
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
}
