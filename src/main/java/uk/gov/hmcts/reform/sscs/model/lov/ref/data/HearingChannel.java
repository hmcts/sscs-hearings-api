package uk.gov.hmcts.reform.sscs.model.lov.ref.data;

public enum HearingChannel {
    TELEPHONE("telephone", "Telephone"),
    VIDEO("video", "Video"),
    FACE_TO_FACE("faceToFace", "Face To Face"),
    NOT_ATTENDING("notAttending", "Not Attending");

    private final String key;
    private final String value;

    HearingChannel(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public static HearingChannel getHearingChannelByValue(String value) {
        HearingChannel hearingChannel = null;
        for (HearingChannel hc : HearingChannel.values()) {
            if (hc.getValue().equals(value)) {
                hearingChannel = hc;
                break;
            }
        }
        return hearingChannel;
    }

    public String getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }
}
