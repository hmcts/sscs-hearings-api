package uk.gov.hmcts.reform.sscs.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Builder (builderMethodName = "DescendantCaseDataBuilder")
public class DescendantCaseData extends uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData {

    private Map<String, String> caseFlags;
}
