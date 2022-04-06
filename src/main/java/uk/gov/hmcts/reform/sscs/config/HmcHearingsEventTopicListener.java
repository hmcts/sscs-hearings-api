package uk.gov.hmcts.reform.sscs.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.sscs.helper.QueueHelper;
import uk.gov.hmcts.reform.sscs.model.hmcmessage.HmcMessage;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

@Slf4j
@Data
@Configuration
public class HmcHearingsEventTopicListener {

    @Value("${azure.hearings-topic.inbound.connectionString}")
    private String connectionString;
    @Value("${azure.hearings-topic.inbound.topicName}")
    private String topicName;
    @Value("${azure.hearings-topic.inbound.subscriptionName}")
    private String subscriptionName;
    @Value("${azure.hearings-topic.duration}")
    private Duration tryTimeout;
    @Value("${azure.hearings-topic.delay}")
    private Duration delay;
    @Value("${azure.hearings-topic.retries}")
    private Integer maxRetries;
    @Value("${azure.hearings-topic.hmcServiceIdBba3}")
    private static String hmcServiceIdBba3;

    //TODO add @Bean and add correct values (connectionString,topicName,subscriptionName) in application.yml
    @SuppressWarnings({"PMD.CloseResource"})
    public void receiveMessages() {
        CountDownLatch countdownLatch = new CountDownLatch(1);

        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
            .retryOptions(QueueHelper.configureRetryOptions(tryTimeout, delay, maxRetries))
            .connectionString(connectionString)
            .processor()
            .topicName(topicName)
            .subscriptionName(subscriptionName)
            .processMessage(HmcHearingsEventTopicListener::processMessage)
            .processError(context -> QueueHelper.processError(context, countdownLatch))
            .buildProcessorClient();

        processorClient.start();
    }

    public static void processMessage(ServiceBusReceivedMessageContext context) {
        ServiceBusReceivedMessage message = context.getMessage();
        HmcMessage hmcMessage = message.getBody().toObject(HmcMessage.class);
        String hmctsServiceID = hmcMessage.getHmctsServiceID();

        if (hmctsServiceID.contains(hmcServiceIdBba3)) {
            //TODO process all messages with BBA3 and remove log message if needed.
            log.info("Processing hearing #: {}. and case #: {}%n", hmcMessage.getHearingID(), hmcMessage.getCaseRef());
        }

        log.info("Processing message. Session: {}, Sequence #: {}. Contents: {}%n", message.getMessageId(),
                 message.getSequenceNumber(), message.getBody()
        );
    }
}
