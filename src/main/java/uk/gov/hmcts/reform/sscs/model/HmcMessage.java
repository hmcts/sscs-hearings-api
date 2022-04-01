package uk.gov.hmcts.reform.sscs.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HmcMessage {

    //TODO json object may need to change at a later stage when we get HMC data
    private String hmctsServiceID;
    private String caseRef;
    private String hearingID;
    private HearingUpdate hearingUpdate;
}
