package uk.gov.hmcts.reform.sscs.model.single.hearing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RequirementType {

    MUST_INCLUDE("MUSTINC"),
    OPTIONAL_INCLUDE("OPTINC"),
    EXCLUDE("EXCLUDE");

    private final String requirementLabel;
}
