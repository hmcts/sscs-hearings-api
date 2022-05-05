package uk.gov.hmcts.reform.sscs.model.single.hearing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum DayOfWeekUnavailabilityType {

    AM("AM"),
    PM("PM"),
    ALL_DAY("All Day");

    private final String label;
}
