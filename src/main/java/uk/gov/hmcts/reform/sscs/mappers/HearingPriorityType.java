package uk.gov.hmcts.reform.sscs.mappers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HearingPriorityType {
    HIGH("high", "High Priority"),
    NORMAL("normal", "Normal Priority");

    private final String type;
    private final String description;

    public String getType() {
        return this.type;
    }

    public String toString() {
        return this.type;
    }
}
