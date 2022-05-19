package uk.gov.hmcts.reform.sscs.utils;

import uk.gov.hmcts.reform.sscs.ccd.domain.HearingSubtype;

import java.util.Objects;

public final class HearingUtils {
    //TODO Need to be replaced with HearingChannel statics once SSCS-10273 is done.
    public static final String FACE_TO_FACE = "faceToFace";
    public static final String TELEPHONE = "telephone";
    public static final String VIDEO = "video";
    public static final String PAPER = "paper";

    private HearingUtils() {
        throw new IllegalStateException("Utility class");
    }




    /**
     *  Return the party channel corresponding to the HearingSubtype.
     *
     * @param hearingSubtype a HearingSubtype object
     * @return a party channel
     */
    public static String getPartyChannel(HearingSubtype hearingSubtype) {
        if (Objects.nonNull(hearingSubtype)) {
            if (hearingSubtype.isWantsHearingTypeFaceToFace()) {
                return FACE_TO_FACE;
            } else if (hearingSubtype.isWantsHearingTypeTelephone()) {
                return TELEPHONE;
            } else if (hearingSubtype.isWantsHearingTypeVideo()) {
                return VIDEO;
            } else {
                return PAPER;
            }
        }
        return null;
    }
}
