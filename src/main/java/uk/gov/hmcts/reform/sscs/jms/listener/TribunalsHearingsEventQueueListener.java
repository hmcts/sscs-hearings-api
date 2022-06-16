package uk.gov.hmcts.reform.sscs.jms.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingState;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.exception.TribunalsEventProcessingException;
import uk.gov.hmcts.reform.sscs.exception.UnhandleableHearingStateException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequest;
import uk.gov.hmcts.reform.sscs.service.HearingsService;

@Slf4j
@Component
@ConditionalOnProperty("flags.tribunals-to-hearings-api.enabled")
public class TribunalsHearingsEventQueueListener {

    private final HearingsService hearingsService;

    public TribunalsHearingsEventQueueListener(HearingsService hearingsService) {
        this.hearingsService = hearingsService;
    }

    @JmsListener(
        destination = "${azure.service-bus.tribunals-to-hearings-api.queueName}",
        containerFactory = "tribunalsHearingsEventQueueContainerFactory"
    )
    public void handleIncomingMessage(HearingRequest message) throws TribunalsEventProcessingException {
        log.info("Message Received");
        try {
            String caseId = message.getCcdCaseId();
            HearingState event = message.getHearingState();
            log.info("Attempting to process hearing event {} from hearings event queue for case ID {}",
                     event, caseId);

            hearingsService.processHearingRequest(message);
            log.info("Hearing event {} for case ID {} successfully processed", event, caseId);
        } catch (GetCaseException | UnhandleableHearingStateException | UpdateCaseException
                 | InvalidMappingException ex) {
            throw new TribunalsEventProcessingException("An exception occurred whilst processing hearing event", ex);
        }
    }
}
