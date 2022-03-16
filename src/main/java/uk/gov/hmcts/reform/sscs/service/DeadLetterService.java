package uk.gov.hmcts.reform.sscs.service;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.model.DeadLetter;

@Slf4j
@Service
public class DeadLetterService {

    private DeadLetterService() {
        // Gradle styleCheck
    }

    private static final String connectionString = System.getenv("AZURE_SERVICEBUS_DL_CONNECTION_STRING");
    private static final String queueName = System.getenv("AZURE_SERVICEBUS_DL_QUEUE_NAME");

    public static void sendMessage(DeadLetter deadLetter) throws JsonProcessingException {
        ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .queueName(queueName)
            .buildClient();

        ServiceBusMessage sbm = new ServiceBusMessage(deadLetterToJson(deadLetter));
        senderClient.sendMessage(sbm);
        senderClient.close();
    }

    private static String deadLetterToJson(DeadLetter deadLetter) throws JsonProcessingException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        try {
            return ow.writeValueAsString(deadLetter);
        } catch (JsonProcessingException jpe) {
            log.error("Dead lettering JsonProcessingException for Case ID: {}", deadLetter.getCaseID());
            throw jpe;
        }
    }
}
