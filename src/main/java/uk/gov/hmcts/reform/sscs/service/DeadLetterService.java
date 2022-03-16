package uk.gov.hmcts.reform.sscs.service;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.model.DeadLetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@Slf4j
@Service
public class DeadLetterService {

    private static final String connectionString = System.getenv("AZURE_SERVICEBUS_DL_CONNECTION_STRING");
    private static final String queueName = System.getenv("AZURE_SERVICEBUS_DL_QUEUE_NAME");

    public static void sendMessage(DeadLetter deadLetter) {
        ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .queueName(queueName)
            .buildClient();

        ServiceBusMessage sbm = new ServiceBusMessage(deadLetterToJson(deadLetter));
        senderClient.sendMessage(sbm);
        senderClient.close();
    }

    private static String deadLetterToJson(DeadLetter deadLetter) {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = "";

        try {
            return ow.writeValueAsString(deadLetter);
        } catch (JsonProcessingException e) {
            log.info("Dead lettering JsonProcessingException for Case ID: {}", deadLetter.getCaseID());
        }

        return json;
    }
}
