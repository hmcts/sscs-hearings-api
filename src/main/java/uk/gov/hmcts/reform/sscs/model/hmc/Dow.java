package uk.gov.hmcts.reform.sscs.model.hmc;

import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;

@Getter
public enum Dow {

    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday"),
    SATURDAY("Saturday"),
    SUNDAY("Sunday");

    private final String dowLabel;

    Dow(String dowLabel) {
        this.dowLabel = dowLabel;
    }


    public static Dow getByLabel(String label) {
        return Arrays.stream(Dow.values())
            .filter(eachDow -> eachDow.toString().toLowerCase(Locale.ROOT)
                .equals(label.toLowerCase(Locale.ROOT))).findAny().orElse(null);
    }
}
