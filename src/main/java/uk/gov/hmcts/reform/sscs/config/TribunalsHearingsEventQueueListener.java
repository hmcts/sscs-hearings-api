package uk.gov.hmcts.reform.sscs.config;

import com.azure.messaging.servicebus.*;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingRoute;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingState;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequest;
import uk.gov.hmcts.reform.sscs.service.HearingsService;

@Slf4j
@Data
@Configuration
//@ConditionalOnProperty("flags.tribunals-to-hearings-api.enabled")
public class TribunalsHearingsEventQueueListener {

    private final HearingsService hearingsService;
    private final TribunalsHearingEventQueueProperties tribunalsHearingEventQueueProperties;

    @Autowired
    public TribunalsHearingsEventQueueListener(HearingsService hearingsService, TribunalsHearingEventQueueProperties tribunalsHearingEventQueueProperties) {
        this.hearingsService = hearingsService;
        this.tribunalsHearingEventQueueProperties = tribunalsHearingEventQueueProperties;
    }

    @Bean
    @SuppressWarnings("PMD.CloseResource")
    public ServiceBusProcessorClient tribunalsHearingsEventProcessorClient() {
        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
            .retryOptions(tribunalsHearingEventQueueProperties.retryOptions())
            .connectionString(tribunalsHearingEventQueueProperties.getConnectionString())
            .sessionProcessor()
            .queueName(tribunalsHearingEventQueueProperties.getQueueName())
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .disableAutoComplete()
            .processMessage(this::processMessage)
            .processError(QueueHelper::processError)
            .buildProcessorClient();

        processorClient.start();

        log.info("Tribunals hearings event queue processor started.");

        return processorClient;
    }

    private void processMessage(ServiceBusReceivedMessageContext context) {
        ServiceBusReceivedMessage message = context.getMessage();
        HearingRequest hearingRequest = message.getBody().toObject(HearingRequest.class);
        String caseId = hearingRequest.getCcdCaseId();
        HearingState event = hearingRequest.getHearingState();

        try {
            log.info("Attempting to process hearing event {} from hearings event queue for case ID {}",
                event, caseId);

            hearingsService.processHearingRequest(hearingRequest);

            log.info("Hearing event {} for case ID {} successfully processed", event, caseId);
            context.complete();
        } catch (Exception ex) {
            log.error("An exception occurred whilst processing hearing event for case ID {}, event: {}. "
                    + "Abandoning message", caseId, event, ex);
            context.abandon();
        }
    }
}
