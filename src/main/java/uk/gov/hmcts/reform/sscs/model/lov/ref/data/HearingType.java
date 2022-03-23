package uk.gov.hmcts.reform.sscs.model.lov.ref.data;

public enum HearingType {
    SUBSTANTIVE("BBA3-substantive", "Substantive"),
    DIRECTION_HEARINGS("BBA3-directionHearings", "Direction Hearings"),
    CHAMBERS_OUTCOME("BBA3-chambersOutcome", "Chambers Outcome");

    private final String key;
    private final String value;

    HearingType(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public static HearingType getHearingTypeByValue(String value) {
        HearingType hearingType = null;
        for (HearingType ht : HearingType.values()) {
            if (ht.getValue().equals(value)) {
                hearingType = ht;
                break;
            }
        }
        return hearingType;
    }

    public String getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }
}
