package uk.gov.hmcts.reform.sscs.model.lov.ref.data;

public enum HearingPriority {
    NORMAL("normal", "Normal"),
    HIGH("high", "High"),
    CRITICAL("critical", "Critical"),
    PENDING("pending", "Pending");

    private final String key;
    private final String value;

    HearingPriority(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public static HearingPriority getHearingPriorityByValue(String value) {
        HearingPriority hearingPriority = null;
        for (HearingPriority hp : HearingPriority.values()) {
            if (hp.getValue().equals(value)) {
                hearingPriority = hp;
                break;
            }
        }
        return hearingPriority;
    }

    public String getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }
}
