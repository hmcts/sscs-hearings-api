package uk.gov.hmcts.reform.sscs.model.single.hearing;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.sscs.validator.EnumPattern;

@Data
@RequiredArgsConstructor
@Builder
public class PanelPreference {

    private String memberID;

    private String memberType;

    @EnumPattern(enumClass = RequirementType.class, fieldName = "requirementType")
    private String requirementType;
}
