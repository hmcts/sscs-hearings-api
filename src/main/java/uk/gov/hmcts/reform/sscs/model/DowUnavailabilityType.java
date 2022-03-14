package uk.gov.hmcts.reform.sscs.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;

@Getter
public enum DowUnavailabilityType {

    AM("AM"),
    PM("PM"),
    ALLDAY("All Day");

    private final String label;

    DowUnavailabilityType(String label) {
        this.label = label;
    }

    public static DowUnavailabilityType getByLabel(String label) {
        return Arrays.stream(DowUnavailabilityType.values())
            .filter(eachDowUnavailable -> eachDowUnavailable.toString().toLowerCase(Locale.ROOT)
                .equals(label.toLowerCase(Locale.ROOT))).findAny().orElse(null);
    }
}
