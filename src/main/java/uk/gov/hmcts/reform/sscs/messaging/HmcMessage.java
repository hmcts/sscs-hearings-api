package uk.gov.hmcts.reform.sscs.messaging;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HmcMessage {
    //TODO json object may need to change at a later stage when we get HMC data
    private String hmctsServiceID;
    private String caseRef;
    private String hearingID;
    private String hearingUpdate;
}
