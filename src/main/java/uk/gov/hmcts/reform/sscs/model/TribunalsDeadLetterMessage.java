package uk.gov.hmcts.reform.sscs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequest;

@Builder(toBuilder = true)
@AllArgsConstructor
@Data
public class TribunalsDeadLetterMessage implements Message {
    private HearingRequest hearingsRequest;
    private Long caseID;
}
