
package uk.gov.hmcts.reform.sscs.service.servicebus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.InvalidIdException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.model.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.ListAssistCaseStatus;
import uk.gov.hmcts.reform.sscs.service.HmcEventTopicService;

@Slf4j
@RequiredArgsConstructor
@Service
public class HmcEventMessageProcessService {

    @Value("${sscs.serviceCode}")
    private String sscsServiceCode;

    private final HmcEventTopicService hmcEventTopicService;

    public void processMessage(HmcMessage hmcMessage) throws UpdateCaseException, GetCaseException, InvalidIdException {

        Long caseId = hmcMessage.getCaseId();
        ListAssistCaseStatus listAssistCaseStatus = hmcMessage.getHearingUpdate().getListAssistCaseStatus();
        log.info("Attempting to process hearing event {} from hearings event queue for case ID {}",
            listAssistCaseStatus, caseId);


        if (isMessageNotRelevantForService(hmcMessage)) {
            log.info("Message not for this service for hearing ID {} and case reference: {}",
                hmcMessage.getHearingId(),
                hmcMessage.getCaseId());
            return;
        }

        hmcEventTopicService.processEventMessage(hmcMessage);

        log.info("Hearing message {} processed for case reference {}",
            hmcMessage.getHearingId(),
            hmcMessage.getCaseId());
    }

    public boolean isMessageNotRelevantForService(HmcMessage hmcMessage) {
        return !sscsServiceCode.equals(hmcMessage.getHmctsServiceCode());
    }
}
