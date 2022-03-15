package uk.gov.hmcts.reform.sscs.model;

import lombok.Getter;

@Getter
public enum RequirementType {

    MUSTINC("MUSTINC"),
    OPTINC("OPTINC"),
    EXCLUDE("EXCLUDE");

    private final String value;

    RequirementType(String value) {
        this.value = value;
    }
}
