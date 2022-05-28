package uk.gov.hmcts.reform.sscs.config;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import uk.gov.hmcts.reform.sscs.service.servicebus.TribunalsEventQueueListenerService;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty("flags.tribunals-to-hearings-api.enabled")
public class TribunalsEventQueueListenerConfig {

    @Value("${azure.service-bus.tribunals-to-hearings-api.queueName}")
    private String queueName;
    @Value("${azure.service-bus.tribunals-to-hearings-api.connectionString}")
    private String connectionString;
    @Value("${azure.service-bus.tribunals-to-hearings-api.retryTimeout}")
    private Long retryTimeout;
    @Value("${azure.service-bus.tribunals-to-hearings-api.retryDelay}")
    private Long retryDelay;
    @Value("${azure.service-bus.tribunals-to-hearings-api.maxRetries}")
    private Integer maxRetries;

    TribunalsEventQueueListenerService eventQueueService;

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
                eventQueueService.processMessage(receiverClient);
            }
        });
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
