package uk.gov.hmcts.reform.sscs.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;

@Getter
public enum LocationType {

    COURT("court"),
    CLUSTER("cluster"),
    RESION("region");

    private final String locationLabel;

    LocationType(String locationLabel) {
        this.locationLabel = locationLabel;
    }

    public static LocationType getByLabel(String label) {
        return Arrays.stream(LocationType.values())
            .filter(eachLocation -> eachLocation.toString().toLowerCase(Locale.ROOT)
                .equals(label.toLowerCase(Locale.ROOT))).findAny().orElse(null);
    }
}
