package uk.gov.hmcts.reform.sscs.jms.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingState;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.InvalidIdException;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.exception.TribunalsEventProcessingException;
import uk.gov.hmcts.reform.sscs.exception.UnhandleableHearingStateException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.model.TribunalsDeadLetterMessage;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequest;
import uk.gov.hmcts.reform.sscs.service.AppInsightsService;
import uk.gov.hmcts.reform.sscs.service.HearingsService;

@Slf4j
@Component
@ConditionalOnProperty("flags.tribunals-to-hearings-api.enabled")
public class TribunalsHearingsEventQueueListener {

    private final ObjectMapper objectMapper;

    private final HearingsService hearingsService;

    private final AppInsightsService appInsightsService;

    public TribunalsHearingsEventQueueListener(HearingsService hearingsService, AppInsightsService appInsightsService) {
        this.hearingsService = hearingsService;
        this.appInsightsService = appInsightsService;
        this.objectMapper = new ObjectMapper();
    }

    @JmsListener(
        destination = "${azure.service-bus.tribunals-to-hearings-api.queueName}",
        containerFactory = "tribunalsHearingsEventQueueContainerFactory"
    )
    public void handleIncomingMessage(HearingRequest message) throws TribunalsEventProcessingException {
        log.info("Message Received");
        String caseId = null;
        try {
            caseId = message.getCcdCaseId();
            HearingState event = message.getHearingState();
            log.info("Attempting to process hearing event {} from hearings event queue for case ID {}",
                     event, caseId
            );

            hearingsService.processHearingRequest(message);
            log.info("Hearing event {} for case ID {} successfully processed", event, caseId);
        } catch (GetCaseException | UnhandleableHearingStateException | UpdateCaseException | InvalidIdException
                 | InvalidMappingException ex) {
            ex.printStackTrace();
            log.error("An exception occurred whilst processing hearing event for case ID {}."
                          + " Abandoning message", caseId, ex);
            handleDeadLetter(message);
            throw new TribunalsEventProcessingException("Abandoned Message", ex);
        }
    }

    @JmsListener(
        destination = "${azure.service-bus.tribunals-to-hearings-api.queueName}/$DeadLetterQueue",
        containerFactory = "tribunalsDeadLetterFactoryContainer"
    )
    public void handleDeadLetterListener(HearingRequest message) {
        log.info("Handling dead letter");
        handleDeadLetter(message);
    }

    private void handleDeadLetter(HearingRequest message) {
        TribunalsDeadLetterMessage failMsg = obtainFailedMessage(message);
        try {
            appInsightsService.sendAppInsightsEvent(failMsg);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            log.error("Sending to appInsights has failed");
        }
    }

    private TribunalsDeadLetterMessage obtainFailedMessage(HearingRequest message) {
        log.info("Obtaining Failed Message Information");
        Long caseId = Long.valueOf(message.getCcdCaseId());
        return TribunalsDeadLetterMessage.builder()
            .hearingsRequest(message)
            .caseID(caseId)
            .build();
    }


}
