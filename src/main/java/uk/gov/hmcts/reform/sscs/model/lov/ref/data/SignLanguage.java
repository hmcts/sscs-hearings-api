package uk.gov.hmcts.reform.sscs.model.lov.ref.data;

public enum SignLanguage {
    AMERICAN_SIGN_LANGUAGE("americanSignLanguage", "American Sign Language (ASL)"),
    BRITISH_SIGN_LANGUAGE("britishSignLanguage", "British Sign Language (BSL)"),
    HANDS_ON_SIGNING("handsOnSigning", "Hands on signing"),
    INTERNATIONAL_SIGN("internationalSign", "International Sign (IS)"),
    LIPSPEAKER("lipspeaker", "Lipspeaker"),
    MAKATON("makaton", "Makaton"),
    DEAFBLIND_MANUAL_ALPHABET("deafblindManualAlphabet", "Deafblind manual alphabet"),
    NOTETAKER("notetaker", "Notetaker"),
    DEAF_RELAY("deafRelay", "Deaf Relay"),
    SPEECH_SUPPORTED_ENGLISH("speechSupportedEnglish", "Speech Supported English (SSE)"),
    VISUAL_FRAME_SIGNING("visualFrameSigning", "Visual frame signing"),
    PALANTYPIST("palantypist", "Palantypist / Speech to text");

    private final String key;
    private final String value;

    SignLanguage(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public static SignLanguage getSignLanguageByValue(String value) {
        SignLanguage signLanguage = null;
        for (SignLanguage sl : SignLanguage.values()) {
            if (sl.getValue().equals(value)) {
                signLanguage = sl;
                break;
            }
        }
        return signLanguage;
    }

    public String getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }
}
