package uk.gov.hmcts.reform.sscs.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

@Data
@Builder
public class HearingWrapper {
    private SscsCaseData originalCaseData;
    private SscsCaseData updatedCaseData;
    private EventType event;
    private HearingState state; // TODO when SSCS-10222 is deployed, replace with SSCS-Common version of this class
}
