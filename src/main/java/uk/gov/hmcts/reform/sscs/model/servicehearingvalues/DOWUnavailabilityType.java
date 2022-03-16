package uk.gov.hmcts.reform.sscs.model.servicehearingvalues;

import lombok.Getter;

@Getter
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public enum DOWUnavailabilityType {

    AM("AM"),
    PM("PM"),
    ALL("ALL");

    private final String value;

    DOWUnavailabilityType(String value) {
        this.value = value;
    }
}
