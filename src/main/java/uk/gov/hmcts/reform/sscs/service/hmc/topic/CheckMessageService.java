
package uk.gov.hmcts.reform.sscs.service.hmc.topic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.exception.*;
import uk.gov.hmcts.reform.sscs.model.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.ListAssistCaseStatus;

import static java.util.Objects.isNull;

@Slf4j
@RequiredArgsConstructor
@Service
public class CheckMessageService {

    @Value("${sscs.serviceCode}")
    private String sscsServiceCode;

    private final ProcessMessageService processMessageService;

    public void checkMessage(HmcMessage hmcMessage)
            throws UpdateCaseException, GetCaseException, InvalidIdException, GetHearingException,
            InvalidHmcMessageException, InvalidMappingException, InvalidHearingDataException {

        validateHmcMessage(hmcMessage);

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

        processMessageService.processEventMessage(hmcMessage);

        log.info("Hearing message {} processed for case reference {}",
            hmcMessage.getHearingId(),
            hmcMessage.getCaseId());
    }

    public void validateHmcMessage(HmcMessage hmcMessage) throws InvalidHmcMessageException {

        if (isNull(hmcMessage)) {
            throw new InvalidHmcMessageException("HMC message must not be mull");
        }

        if (isNull(hmcMessage.getHearingId())) {
            throw new InvalidHmcMessageException("HMC message field hearingID is missing");
        }

        if (isNull(hmcMessage.getHearingUpdate())) {
            throw new InvalidHmcMessageException("HMC message field HearingUpdate is missing");
        }

        if (isNull(hmcMessage.getHearingUpdate().getHmcStatus())) {
            throw new InvalidHmcMessageException("HMC message field HmcStatus is missing");
        }
    }

    public boolean isMessageNotRelevantForService(HmcMessage hmcMessage) {
        return !sscsServiceCode.equals(hmcMessage.getHmctsServiceCode());
    }
}
