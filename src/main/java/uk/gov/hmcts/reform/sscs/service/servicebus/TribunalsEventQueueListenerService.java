package uk.gov.hmcts.reform.sscs.service.servicebus;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingState;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequest;
import uk.gov.hmcts.reform.sscs.service.HearingsService;

@Service
@Slf4j
public class TribunalsEventQueueListenerService {

    private HearingsService hearingsService;

    public void processMessage(ServiceBusSessionReceiverClient receiverClient) {

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
}
