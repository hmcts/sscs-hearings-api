package uk.gov.hmcts.reform.sscs.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sscs.model.hmc.message.HmcMessage;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HmcTopicSendController {

    @Value("${azure.service-bus.hmc-to-hearings-api.topicName}")
    private String destination;

    private final JmsTemplate jmsTemplate;

    /*
Example payload:
{
    "hmctsServiceID": "BBA3",
    "caseRef": "11112",
    "hearingID": "1234",
    "hearingUpdate": {
        "listingStatus": "Fixed",
        "listAssistCaseStatus": "Awaiting Listing",
        "hmcStatus": "Cancelled"
    }
}
     */

    @GetMapping("/test/sendhmcmessage")
    public String postMessage(@RequestBody HmcMessage hmcMessage) {
        log.info("Sending message: {}", hmcMessage);
        jmsTemplate.convertAndSend(destination, hmcMessage);
        return "Success";
    }
}
