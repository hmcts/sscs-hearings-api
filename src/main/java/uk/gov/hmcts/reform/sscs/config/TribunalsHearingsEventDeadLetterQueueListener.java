package uk.gov.hmcts.reform.sscs.config;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.models.SubQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Slf4j
@Configuration
//@ConditionalOnProperty("flags.tribunals-to-hearings-api.enabled")
public class TribunalsHearingsEventDeadLetterQueueListener {

    private boolean keepRun = true;

    private final TribunalsHearingEventQueueProperties tribunalsHearingEventQueueProperties;

    @Autowired
    public TribunalsHearingsEventDeadLetterQueueListener(TribunalsHearingEventQueueProperties tribunalsHearingEventQueueProperties) {
        this.tribunalsHearingEventQueueProperties = tribunalsHearingEventQueueProperties;
    }

    @Bean
    public void processDeadLetterQueue(){
        try(ServiceBusReceiverClient sessionReceiver = tribunalsHearingsEventProcessorClientDeadLetter()){
            while(keepRun){
                consumeMessage(sessionReceiver);
            }
        }catch(Exception ex){
            log.error("Error Processing Queue: "+ex);
        }
    }

    public ServiceBusReceiverClient tribunalsHearingsEventProcessorClientDeadLetter() {
        log.info("Creating Events Dead Letter Queue Session receiver");
        ServiceBusReceiverClient client = new ServiceBusClientBuilder()
            .retryOptions(retryOptions())
            .connectionString(tribunalsHearingEventQueueProperties.getConnectionString())
            .receiver()
            .queueName(tribunalsHearingEventQueueProperties.getQueueName())
            .subQueue(SubQueue.DEAD_LETTER_QUEUE)
            .buildClient();

        log.info("Dead Letter Queue Session receiver created, successfully");
        return client;
    }

    protected void consumeMessage(ServiceBusReceiverClient receiver) {
        log.info("Starting to cosume message");
        try {
            receiver.receiveMessages(1).forEach(
                message -> {
                    final String messageId = message.getMessageId();
                    try {
                        log.info("Recieved Dead Letter Message with ID: {} ", messageId);
                        receiver.complete(message);
                        log.info("Dead Letter Queue message with id '{}' handled successfully", messageId);
                    }catch(Exception ex){
                        log.error("Error processing Dead Letter Queue message with id '{}' - "
                                      + "abandon the processing", messageId);
                        receiver.abandon(message);
                    }
                }
            );
        } catch (Exception ex) {
            log.info("Failed to consume message with error {}",ex);
        }
    }

    public void stop() {
        keepRun = false;
    }

    private AmqpRetryOptions retryOptions() {
        AmqpRetryOptions amqpRetryOptions = new AmqpRetryOptions();
        amqpRetryOptions.setMode(AmqpRetryMode.EXPONENTIAL);
        amqpRetryOptions.setTryTimeout(Duration.ofMinutes(tribunalsHearingEventQueueProperties.getRetryTimeout()));
        amqpRetryOptions.setMaxRetries(tribunalsHearingEventQueueProperties.getMaxRetries());
        amqpRetryOptions.setDelay(Duration.ofSeconds(tribunalsHearingEventQueueProperties.getRetryDelay()));

        return amqpRetryOptions;
    }

}
