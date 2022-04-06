package uk.gov.hmcts.reform.sscs.model.hmcmessage;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class HmcMessage {

    //TODO json object may need to change at a later stage when we get HMC data
    private String hmctsServiceID;
    private String caseRef;
    private String hearingID;
    private HearingUpdate hearingUpdate;

    public HmcMessage(@JsonProperty("hmctsServiceID") String hmctsServiceID,
                      @JsonProperty("caseRef") String caseRef,
                      @JsonProperty("hearingID") String hearingID,
                      @JsonProperty("hearingUpdate") HearingUpdate hearingUpdate) {
        this.hmctsServiceID = hmctsServiceID;
        this.caseRef = caseRef;
        this.hearingID = hearingID;
        this.hearingUpdate = hearingUpdate;

    }
}
