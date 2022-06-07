package uk.gov.hmcts.reform.sscs.jms.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingState;
import uk.gov.hmcts.reform.sscs.exception.*;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequest;
import uk.gov.hmcts.reform.sscs.service.HearingsService;

import javax.jms.JMSException;

@Slf4j
@Component
@ConditionalOnProperty("flags.tribunals-to-hearings-api.enabled")
public class TribunalsHearingsEventQueueListener {

    private final ObjectMapper objectMapper;

    private final HearingsService hearingsService;

    public TribunalsHearingsEventQueueListener(HearingsService hearingsService) {
        this.hearingsService = hearingsService;
        this.objectMapper = new ObjectMapper();
    }

    @JmsListener(
        destination = "${azure.service-bus.tribunals-to-hearings-api.queueName}",
        containerFactory = "tribunalsHearingsEventQueueContainerFactory"
    )
    public void onMessage(String message) throws TribunalsEventProcessingException, JMSException {
        log.info("Message Received");
        String caseId = null;
        try {
            HearingRequest hearingRequest = objectMapper.readValue(message, HearingRequest.class);
            caseId = hearingRequest.getCcdCaseId();
            HearingState event = hearingRequest.getHearingState();
            log.info("Attempting to process hearing event {} from hearings event queue for case ID {}",
                     event, caseId
            );

            hearingsService.processHearingRequest(hearingRequest);
            log.info("Hearing event {} for case ID {} successfully processed", event, caseId);
        } catch (JsonProcessingException | GetCaseException | UnhandleableHearingStateException | UpdateCaseException | InvalidIdException | InvalidMappingException ex) {
            ex.printStackTrace();
            log.error("An exception occurred whilst processing hearing event for case ID {}."
                          + " Abandoning message", caseId, ex);
        }
    }
}
