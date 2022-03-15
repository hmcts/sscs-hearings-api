package uk.gov.hmcts.reform.sscs.model;

import lombok.Getter;

@Getter
public enum DOW {

    Monday("Monday"),
    Tuesday("Tuesday"),
    Wednesday("Wednesday"),
    Thursday("Thursday"),
    Friday("Friday"),
    Saturday("Saturday"),
    Sunday("Sunday");

    private final String value;

    DOW(String value) {
        this.value = value;
    }
}
