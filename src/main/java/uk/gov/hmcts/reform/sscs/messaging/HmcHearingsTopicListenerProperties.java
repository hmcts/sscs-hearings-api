package uk.gov.hmcts.reform.sscs.messaging;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HmcHearingsTopicListenerProperties {

    private String connectionString;
    private String topicName;
    private String subName;
}
