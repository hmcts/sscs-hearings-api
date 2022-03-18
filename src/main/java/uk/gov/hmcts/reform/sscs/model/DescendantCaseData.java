package uk.gov.hmcts.reform.sscs.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder (builderMethodName = "DescendantCaseDataBuilder")
public class DescendantCaseData extends uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData {

    private Map<String, Object> caseFlags;
}
