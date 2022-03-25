package uk.gov.hmcts.reform.sscs.messaging;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HmcMessage {
    private String hmctsServiceID;
    private String caseRef;
    private String hearingID;
    private String hearingUpdate;
}
