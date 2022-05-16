package uk.gov.hmcts.reform.sscs.utils;

import uk.gov.hmcts.reform.sscs.ccd.domain.ExcludeDate;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingSubtype;
import uk.gov.hmcts.reform.sscs.model.servicehearingvalues.UnavailabilityRange;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class HearingUtils {
    public static final String FACE_TO_FACE = "faceToFace";
    public static final String TELEPHONE = "telephone";
    public static final String VIDEO = "video";
    public static final String PAPER = "paper";

    private HearingUtils() {
        throw new IllegalStateException("Utility class");
    }


    /**
     * Return a List of UnavailabilityRange objects, given a HearingOptions object.
     *
     * @param hearingOptions HearingOptions object
     * @return a List of UnavailabilityRange objects
     */
    public static List<UnavailabilityRange> getPartyUnavailabilityRange(HearingOptions hearingOptions) {
        if (Objects.nonNull(hearingOptions.getExcludeDates())) {
            List<UnavailabilityRange> unavailabilityRanges = new ArrayList<>();
            for (ExcludeDate excludeDate : hearingOptions.getExcludeDates()) {
                unavailabilityRanges.add(UnavailabilityRange.builder()
                        .unavailableFromDate(excludeDate.getValue().getStart())
                        .unavailableToDate(excludeDate.getValue().getEnd()).build());
            }
            return unavailabilityRanges;
        } else {
            return new ArrayList<>();
        }
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
