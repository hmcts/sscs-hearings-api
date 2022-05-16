package uk.gov.hmcts.reform.sscs.config;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.*;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.messaging.servicebus.models.SubQueue;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingState;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequest;
import uk.gov.hmcts.reform.sscs.service.HearingsService;

import java.time.Duration;

@Slf4j
@Data
@Configuration
public class TribunalsHearingsEventQueueListener {

    private final HearingsService hearingsService;



    private String connectionString = "[Hidden String]";
    private String queueName = "[Hidden String]";

    private Long retryTimeout = 1L;
    private Long retryDelay = 1L;
    private Integer maxRetries = 1;

    @Autowired
    public TribunalsHearingsEventQueueListener(HearingsService hearingsService) {
        this.hearingsService = hearingsService;
    }

    @Bean
    @SuppressWarnings("PMD.CloseResource")
    public ServiceBusProcessorClient tribunalsHearingsEventProcessorClient() {
        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
            .retryOptions(retryOptions())
            .connectionString(connectionString)
            .sessionProcessor()
            .queueName(queueName)
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .disableAutoComplete()
            .processMessage(this::processMessage)
            .processError(QueueHelper::processError)
            .buildProcessorClient();

        processorClient.start();

        log.info("Tribunals hearings event queue processor started.");

        return processorClient;
    }

    public ServiceBusReceiverClient tribunalsHearingsEventProcessorClientDeadLetter() {
        log.info("Creating Events Dead Letter Queue Session receiver");
        ServiceBusReceiverClient client = new ServiceBusClientBuilder()
            .retryOptions(retryOptions())
            .connectionString(connectionString)
            .receiver()
            .queueName(queueName)
            .subQueue(SubQueue.DEAD_LETTER_QUEUE)
            .buildClient();

        log.info("Dead Letter Queue Session receiver created, successfully");
        return client;
    }

    void sendMessage()
    {
        // create a Service Bus Sender client for the queue
        ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .queueName(queueName)
            .buildClient();

        // send one message to the queue
        senderClient.sendMessage(new ServiceBusMessage("Hello, World!"));
        System.out.println("Sent a single message to the queue: " + queueName);
    }

    private void processMessage(ServiceBusReceivedMessageContext context) {
        ServiceBusReceivedMessage message = context.getMessage();
        HearingRequest hearingRequest = message.getBody().toObject(HearingRequest.class);
        String caseId = hearingRequest.getCcdCaseId();
        HearingState event = hearingRequest.getHearingState();

        try {
            log.info("Attempting to process hearing event {} from hearings event queue for case ID {}",
                event, caseId);

            hearingsService.processHearingRequest(hearingRequest);

            log.info("Hearing event {} for case ID {} successfully processed", event, caseId);
            context.complete();
        } catch (Exception ex) {
            log.error("An exception occurred whilst processing hearing event for case ID {}, event: {}. "
                    + "Abandoning message", caseId, event, ex);
            context.abandon();
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
