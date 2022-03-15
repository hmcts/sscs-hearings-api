package uk.gov.hmcts.reform.sscs.model;

import lombok.Getter;

@Getter
public enum DOWUnavailabilityType {

    AM("AM"),
    PM("PM"),
    ALL("ALL");

    private final String value;

    DOWUnavailabilityType(String value) {
        this.value = value;
    }
}
