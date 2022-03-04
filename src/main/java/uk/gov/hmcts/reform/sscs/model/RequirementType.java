package uk.gov.hmcts.reform.sscs.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;

@Getter
public enum RequirementType {

    MUSTINC("MUSTINC"),
    OPTINC("OPTINC"),
    EXCLUDE("EXCLUDE");

    private final String requirementLabel;

    RequirementType(String requirementLabel) {
        this.requirementLabel = requirementLabel;
    }

    public static RequirementType getByLabel(String label) {
        return Arrays.stream(RequirementType.values())
            .filter(eachRequirement -> eachRequirement.toString().toLowerCase(Locale.ROOT)
                .equals(label.toLowerCase(Locale.ROOT))).findAny().orElse(null);
    }
}
