package uk.gov.hmcts.reform.sscs.service.hmc.topic;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.exception.HmcEventProcessingException;
import uk.gov.hmcts.reform.sscs.model.hmcmessage.HmcMessage;

@Service
public class ProcessHmcMessageService {

    public void processEventMessage(HmcMessage hmcMessage) throws HmcEventProcessingException {
    }
}
