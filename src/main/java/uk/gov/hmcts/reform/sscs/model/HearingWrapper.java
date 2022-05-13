package uk.gov.hmcts.reform.sscs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingState;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HearingWrapper {
    private SscsCaseData caseData;
    private SscsCaseData updatedCaseData;
    private EventType event;
    private HearingState state;
    private String sscsServiceCode;
    private String exUiUrl;
}
