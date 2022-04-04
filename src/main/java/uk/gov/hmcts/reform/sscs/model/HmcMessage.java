package uk.gov.hmcts.reform.sscs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HmcMessage {

    //TODO json object may need to change at a later stage when we get HMC data
    private String hmctsServiceID;
    private String caseRef;
    private String hearingID;
    private HearingUpdate hearingUpdate;
}
