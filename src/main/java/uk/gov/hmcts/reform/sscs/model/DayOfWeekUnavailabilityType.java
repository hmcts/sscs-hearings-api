package uk.gov.hmcts.reform.sscs.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DayOfWeekUnavailabilityType {

    AM("AM"),
    PM("PM"),
    ALLDAY("All Day");

    private final String label;

}
