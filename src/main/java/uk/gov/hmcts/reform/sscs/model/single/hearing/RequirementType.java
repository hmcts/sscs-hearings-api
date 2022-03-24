package uk.gov.hmcts.reform.sscs.model.single.hearing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RequirementType {

    MUSTINC("MUSTINC"),
    OPTINC("OPTINC"),
    EXCLUDE("EXCLUDE");

    private final String requirementLabel;
}
