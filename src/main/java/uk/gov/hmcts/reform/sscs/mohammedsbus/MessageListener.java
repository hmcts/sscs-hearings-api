package uk.gov.hmcts.reform.sscs.mohammedsbus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequest;

@Component
@Slf4j
public class MessageListener {

    private static final String messageQueueName = "";
    private static final String deadLetter = "/$deadletterqueue";

    @JmsListener(destination = messageQueueName)
    public void receiveMessage(final Message<HearingRequest> message){
        MessageHeaders headers = message.getHeaders();
        System.out.println("Headers: " + headers);

        HearingRequest hearingRequest = message.getPayload();
        System.out.println("Hearing Request = " + hearingRequest);
    }

}
