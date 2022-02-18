package uk.gov.hmcts.reform.sscs.domain;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

@Data
@Builder(toBuilder = true)
public class JobWrapper {

    private long caseId;
    private EventType eventType;
    private String jobId;
    private String jobGroup;

}
