package uk.gov.hmcts.reform.sscs.model.hmc.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HmcMessage {
    @JsonProperty("hmctsServiceID")
    private String hmctsServiceCode;

    @JsonProperty("caseRef")
    private Long caseId;

    @JsonProperty("hearingID")
    private String hearingId;

    private HearingUpdate hearingUpdate;
}
