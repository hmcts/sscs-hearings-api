
package uk.gov.hmcts.reform.sscs.service.servicebus;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.InvalidIdException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.model.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.sscs.service.HmcEventTopicService;

@Slf4j
@Service
public class HmcEventTopicListenerService {

    @Value("${sscs.serviceCode}")
    private String sscsServiceCode;

    private HmcEventTopicService hmcEventTopicService;

    public void processMessage(ServiceBusReceivedMessageContext context) {
        ServiceBusReceivedMessage message = context.getMessage();
        HmcMessage hmcMessage = message.getBody().toObject(HmcMessage.class);

        if (isMessageRelevantForService(hmcMessage)) {
            try {
                log.info("Processing hearing ID: {} for case reference: {}", hmcMessage.getHearingID(),
                    hmcMessage.getCaseId()
                );

                hmcEventTopicService.processEventMessage(hmcMessage);

                log.info("Hearing message {} processed for case reference {}", hmcMessage.getHearingID(),
                    hmcMessage.getCaseId()
                );

                context.complete();
            } catch (GetCaseException | UpdateCaseException | InvalidIdException exc) {
                log.error("An exception occurred whilst processing hearing event for hearing ID {}, case reference: {}",
                    hmcMessage.getHearingID(),
                    hmcMessage.getCaseId(),
                    exc);
                context.abandon();
            }
        } else {
            log.info("Nothing updated for hearing ID: {} for case reference: {}", hmcMessage.getHearingID(),
                hmcMessage.getCaseId()
            );
        }
    }

    public boolean isMessageRelevantForService(HmcMessage hmcMessage) {
        return sscsServiceCode.equals(hmcMessage.getHmctsServiceID());
    }
}
