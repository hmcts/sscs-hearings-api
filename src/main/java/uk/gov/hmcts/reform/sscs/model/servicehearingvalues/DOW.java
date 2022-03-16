package uk.gov.hmcts.reform.sscs.model.servicehearingvalues;

import lombok.Getter;

@Getter
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public enum DOW {

    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday"),
    SATURDAY("Saturday"),
    SUNDAY("Sunday");

    private final String value;

    DOW(String value) {
        this.value = value;
    }
}
