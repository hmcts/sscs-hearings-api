package uk.gov.hmcts.reform.sscs.model.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HmcMessage {

    private String hmctsServiceID;
    private String caseRef;
    private String hearingID;
    private HearingUpdate hearingUpdate;
}
