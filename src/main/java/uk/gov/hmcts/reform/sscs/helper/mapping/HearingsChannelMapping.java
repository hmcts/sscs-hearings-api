package uk.gov.hmcts.reform.sscs.helper.mapping;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.sscs.ccd.domain.CcdValue;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingSubtype;
import uk.gov.hmcts.reform.sscs.ccd.domain.OverrideFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel.FACE_TO_FACE;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel.NOT_ATTENDING;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel.PAPER;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel.TELEPHONE;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel.VIDEO;

@Slf4j
@SuppressWarnings("PMD.CyclomaticComplexity")
public final class HearingsChannelMapping {

    private HearingsChannelMapping() {

    }

    public static List<HearingChannel> getHearingChannels(@Valid SscsCaseData caseData) {
        return List.of(getHearingChannel(caseData));
    }

    public static List<HearingChannel> getHearingChannels(@Valid SscsCaseData caseData, ReferenceDataServiceHolder referenceDataServiceHolder) {
        return List.of(getHearingChannel(caseData, referenceDataServiceHolder));
    }

    public static HearingChannel getHearingChannel(@Valid SscsCaseData caseData) {
        List<HearingChannel> hearingChannels = getAllHearingChannelPreferences(caseData);

        if (hearingChannels.contains(FACE_TO_FACE)) {
            return FACE_TO_FACE;
        } else if (hearingChannels.contains(VIDEO)) {
            return VIDEO;
        } else if (hearingChannels.contains(TELEPHONE)) {
            return TELEPHONE;
        } else {
            return PAPER;
        }
    }

    public static HearingChannel getHearingChannel(@Valid SscsCaseData caseData, ReferenceDataServiceHolder referenceDataServiceHolder) {
        log.info("Get the next hearing channel {}", referenceDataServiceHolder.isAdjournmentFlagEnabled());
        log.info("Adjournment next hearing channel {}", caseData.getAdjournment().getTypeOfNextHearing());
        if (referenceDataServiceHolder.isAdjournmentFlagEnabled() && isYes(caseData.getAdjournment().getAdjournmentInProgress())
            && nonNull(caseData.getAdjournment().getTypeOfNextHearing())) {
            return getAdjournmentNextHearingChannel(caseData);
        }

        return getHearingChannel(caseData);
    }

    private static HearingChannel getAdjournmentNextHearingChannel(SscsCaseData caseData) {
        log.info("Adjournment Next hearing type {} for case {}", caseData.getAdjournment().getTypeOfNextHearing(),
                 caseData.getCaseCode()
        );
        return Arrays.stream(HearingChannel.values())
            .filter(hearingChannel -> caseData.getAdjournment().getTypeOfNextHearing().getHearingChannel().getValueTribunals().equalsIgnoreCase(
                hearingChannel.getValueTribunals()))
            .findFirst().orElse(PAPER);
    }

    public static List<HearingChannel> getAllHearingChannelPreferences(@Valid SscsCaseData caseData) {

        HearingChannel individualPreferredHearingChannel = getIndividualPreferredHearingChannel(
            caseData.getAppeal().getHearingSubtype(),
            caseData.getAppeal().getHearingOptions(),
            caseData.getSchedulingAndListingFields().getOverrideFields()
        );

        List<HearingChannel> hearingChannels = new ArrayList<>();

        hearingChannels.add(individualPreferredHearingChannel);

        if (nonNull(caseData.getOtherParties())) {
            hearingChannels.addAll(
                caseData.getOtherParties().stream()
                    .map(CcdValue::getValue)
                    .map(otherParty -> getIndividualPreferredHearingChannel(
                        otherParty.getHearingSubtype(),
                        otherParty.getHearingOptions(), null
                    )).collect(Collectors.toList()));
        }

        return hearingChannels;
    }

    public static HearingChannel getIndividualPreferredHearingChannel(HearingSubtype hearingSubtype, HearingOptions hearingOptions, OverrideFields overrideFields) {

        if (nonNull(overrideFields) && nonNull(overrideFields.getAppellantHearingChannel())) {
            return overrideFields.getAppellantHearingChannel();
        }

        if (isNull(hearingOptions)) {
            return null;
        }

        if (isFalse(hearingOptions.isWantsToAttendHearing())) {
            return NOT_ATTENDING;
        }

        if (nonNull(hearingSubtype)) {
            if (isYes(hearingSubtype.getWantsHearingTypeFaceToFace())) {
                return FACE_TO_FACE;
            } else if (shouldPreferVideoHearingChannel(hearingSubtype)) {
                return VIDEO;
            } else if (shouldPreferTelephoneHearingChannel(hearingSubtype)) {
                return TELEPHONE;
            }
        } else {
            return null;
        }

        if (isTrue(hearingOptions.isWantsToAttendHearing())) {
            return FACE_TO_FACE;
        }

        throw new IllegalStateException("Failed to determine a preferred hearing channel");
    }

    public static boolean shouldPreferVideoHearingChannel(HearingSubtype hearingSubtype) {
        return isYes(hearingSubtype.getWantsHearingTypeVideo())
            && nonNull(hearingSubtype.getHearingVideoEmail());
    }

    public static boolean shouldPreferTelephoneHearingChannel(HearingSubtype hearingSubtype) {
        return isYes(hearingSubtype.getWantsHearingTypeTelephone()) && nonNull(hearingSubtype.getHearingTelephoneNumber());
    }

    public static boolean isPaperCase(SscsCaseData caseData) {
        return PAPER == getHearingChannel(caseData);
    }

}
