package uk.gov.hmcts.reform.sscs.model.lov.ref.data;

public enum NonStdDurationReason {
    TIME_FOR_INTERPRETATION("timeForInterpretation", "Time for interpretation"),
    COMPLEX_CASE("complexCase", "Complex Case"),
    LITIGANT_IN_PERSON("litigantInPerson", "Litigant in person");

    private final String key;
    private final String value;

    NonStdDurationReason(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public static NonStdDurationReason getNonStdDurationReasonByValue(String value) {
        NonStdDurationReason nonStdDurationReason = null;
        for (NonStdDurationReason nsdr : NonStdDurationReason.values()) {
            if (nsdr.getValue().equals(value)) {
                nonStdDurationReason = nsdr;
                break;
            }
        }
        return nonStdDurationReason;
    }

    public String getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }
}
