package uk.gov.hmcts.reform.sscs.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.sscs.model.Message;

@Builder(toBuilder = true)
@AllArgsConstructor
@Data
public class TribunalsDeadLetterMessage implements Message {
    private String messageId;
    private String body;
    private Long caseID;
}
