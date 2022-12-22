package uk.gov.hmcts.reform.sscs.jms.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingState;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.State;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.ListingException;
import uk.gov.hmcts.reform.sscs.exception.TribunalsEventProcessingException;
import uk.gov.hmcts.reform.sscs.exception.UnhandleableHearingStateException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequest;
import uk.gov.hmcts.reform.sscs.service.CcdCaseService;
import uk.gov.hmcts.reform.sscs.service.HearingsService;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.sscs.ccd.domain.EventType.LISTING_ERROR;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty("flags.tribunals-to-hearings-api.enabled")
public class TribunalsHearingsEventQueueListener {

    private final HearingsService hearingsService;

    private final CcdCaseService ccdCaseService;

    @JmsListener(
        destination = "${azure.service-bus.tribunals-to-hearings-api.queueName}",
        containerFactory = "tribunalsHearingsEventQueueContainerFactory"
    )
    public void handleIncomingMessage(HearingRequest message) throws TribunalsEventProcessingException, GetCaseException, UpdateCaseException {
        log.info("Message received now handling");

        if (isNull(message)) {
            throw new TribunalsEventProcessingException("An exception occurred as message did not match format");
        }
        String caseId = message.getCcdCaseId();
        HearingState event = message.getHearingState();

        log.info("Attempting to process hearing event {} from hearings event queue for case ID {}",
                 event, caseId);

        try {
            hearingsService.processHearingRequest(message);
            log.info("Hearing event {} for case ID {} successfully processed", event, caseId);
        } catch (ListingException ex) {
            log.error("Listing exception found, Summary: {}", ex.getSummary(), ex);

            SscsCaseData caseData = ccdCaseService.getCaseDetails(caseId).getData();
            ccdCaseService.updateCaseData(
                caseData,
                LISTING_ERROR,
                ex.getSummary(),
                ex.getDescription());

            log.info("Listing Error handled. State is now {}.", State.LISTING_ERROR);
        } catch (GetCaseException | UnhandleableHearingStateException | UpdateCaseException ex) {
            throw new TribunalsEventProcessingException("An exception occurred whilst processing hearing event", ex);
        }
    }
}
