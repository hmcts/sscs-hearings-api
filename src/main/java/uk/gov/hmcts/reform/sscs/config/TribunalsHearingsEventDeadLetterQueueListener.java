package uk.gov.hmcts.reform.sscs.config;

import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class TribunalsHearingsEventDeadLetterQueueListener implements Runnable{

    private final TribunalsHearingsEventQueueListener tribunalsHearingsEventQueueListener;
    private boolean keepRun = true;

    public TribunalsHearingsEventDeadLetterQueueListener(TribunalsHearingsEventQueueListener tribunalsHearingsEventQueueListener) {
        this.tribunalsHearingsEventQueueListener = tribunalsHearingsEventQueueListener;
    }

    @Bean
    public void run(){
        try(ServiceBusReceiverClient sessionReceiver = tribunalsHearingsEventQueueListener.tribunalsHearingsEventProcessorClientDeadLetter()){
            while(keepRun){
                System.out.println("RUNNING");
                consumeMessage(sessionReceiver);
            }
        }
    }

    protected void consumeMessage(ServiceBusReceiverClient receiver) {
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
                                      + "abandon the processing and ASB will re-deliver it", messageId);
                        receiver.abandon(message);
                    }
                }
            );
        } catch (Exception ex) {

        }
    }

    public void stop() {
        keepRun = false;
    }

}
