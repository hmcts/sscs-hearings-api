package uk.gov.hmcts.reform.sscs.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingState;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

@Data
@Builder
public class HearingWrapper {
    private SscsCaseData caseData;
    private HearingState state;
}
