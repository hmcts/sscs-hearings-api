package uk.gov.hmcts.reform.sscs.jms.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.jms.message.JmsBytesMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingState;
import uk.gov.hmcts.reform.sscs.exception.*;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequest;
import uk.gov.hmcts.reform.sscs.service.HearingsService;

import javax.jms.JMSException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@ConditionalOnProperty("flag.tribunals-to-hearings-api.enabled")
public class TribunalsHearingsEventQueueListener {

    private final ObjectMapper objectMapper;

    private final HearingRequest hearingRequest;

    private final HearingsService hearingsService;

    public TribunalsHearingsEventQueueListener(HearingRequest hearingRequest,
                                               HearingsService hearingsService) {
        this.hearingRequest = hearingRequest;
        this.hearingsService = hearingsService;
        this.objectMapper = new ObjectMapper();
    }

    @JmsListener(
        destination = "${azure.service-bus.tribunals-to-hearings-api.queueName}",
        containerFactory = "tribunalsHearingsEventQueueContainerFactory"
    )
    public void onMessage(JmsBytesMessage message) throws TribunalsEventProcessingException, JMSException {
        byte[] messageBytes = new byte[(int) message.getBodyLength()];
        message.readBytes(messageBytes);
        String convertedMessage = new String(messageBytes, StandardCharsets.UTF_8);

        try {
            HearingRequest hearingRequest = objectMapper.readValue(convertedMessage, HearingRequest.class);
            String caseId = hearingRequest.getCcdCaseId();
            HearingState event = hearingRequest.getHearingState();
            log.info("Attempting to process hearing event {} from hearings event queue for case ID {}",
                     event, caseId
            );

            hearingsService.processHearingRequest(hearingRequest);
            log.info("Hearing event {} for case ID {} successfully processed", event, caseId);
        } catch (JsonProcessingException | GetCaseException | UnhandleableHearingStateException | UpdateCaseException | InvalidIdException | InvalidMappingException ex) {
            ex.printStackTrace();
            log.error("An exception occurred whilst processing hearing event for case ID {}."
                          + " Abandoning message", message.getJMSMessageID(), ex);
        }
    }
}
