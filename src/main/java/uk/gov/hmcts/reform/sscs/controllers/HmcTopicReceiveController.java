package uk.gov.hmcts.reform.sscs.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.exception.*;
import uk.gov.hmcts.reform.sscs.model.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.sscs.service.hmc.topic.CheckMessageService;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty("flags.hmc-to-hearings-api.enabled")
public class HmcTopicReceiveController {

    private final CheckMessageService eventTopicService;

    @Retryable(maxAttemptsExpression = "#{azure.service-bus.hmc-to-hearings-api.maxRetries}",
        backoff = @Backoff(delayExpression = "${azure.service-bus.hmc-to-hearings-api.retryDelay}"))
    @JmsListener(destination = "${azure.service-bus.hmc-to-hearings-api.topicName}",
        containerFactory = "hmcTopicFactory",
        subscription = "${azure.service-bus.hmc-to-hearings-api.subscriptionName}")
    public void receiveMessage(HmcMessage hmcMessage)
            throws UpdateCaseException, GetCaseException, InvalidIdException, GetHearingException, InvalidHmcMessageException,
            InvalidMappingException, InvalidHearingDataException {
        String hearingID = hmcMessage.getHearingId();
        Long caseId = hmcMessage.getCaseId();
        String hmctsServiceID = hmcMessage.getHmctsServiceCode();
        log.info("Message received from hearings topic for queue for for hearing ID {}, case id {} and service code {}",
            hearingID, caseId, hmctsServiceID);
        try {
            eventTopicService.checkMessage(hmcMessage);
        } catch (Exception ex) {
            log.error("An exception occurred whilst processing hearing event for hearing ID {}, case id {} and service code {}",
                hearingID, caseId, hmctsServiceID, ex);
            throw ex;
        }
    }

}
