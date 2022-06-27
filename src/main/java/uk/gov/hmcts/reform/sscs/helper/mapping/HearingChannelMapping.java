package uk.gov.hmcts.reform.sscs.helper.mapping;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.sscs.ccd.domain.CcdValue;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingSubtype;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.exception.HearingChannelNotFoundException;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.YES;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel.FACE_TO_FACE;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel.NOT_ATTENDING;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel.TELEPHONE;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel.VIDEO;

@Slf4j
@SuppressWarnings("PMD.CyclomaticComplexity")
public final class HearingChannelMapping {

    private HearingChannelMapping() {

    }

    static List<HearingChannel> getHearingChannels(SscsCaseData caseData)  throws HearingChannelNotFoundException {

        if (caseData.getDwpIsOfficerAttending() != null && caseData.getDwpIsOfficerAttending().equals(YES.getValue())) {
            return List.of(FACE_TO_FACE);
        }

        if (!caseData.getAppeal().getHearingOptions().isWantsToAttendHearing()) {
            return List.of(NOT_ATTENDING);
        }

        HearingChannel individualPreferredHearingChannel = getIndividualPreferredHearingChannel(
            caseData.getAppeal().getHearingSubtype(),
            caseData.getAppeal().getHearingOptions()
        );

        if (isNull(individualPreferredHearingChannel)) {
            throw new HearingChannelNotFoundException();
        }

        List<HearingChannel> hearingChannels = new ArrayList<>();

        hearingChannels.add(individualPreferredHearingChannel);

        if (nonNull(caseData.getOtherParties())) {
            hearingChannels.addAll(caseData.getOtherParties().stream().map(CcdValue::getValue).map(
                otherParty -> getIndividualPreferredHearingChannel(
                    otherParty.getHearingSubtype(),
                    otherParty.getHearingOptions()
                )).collect(Collectors.toList()));
        }

        if (hearingChannels.contains(FACE_TO_FACE)) {
            return List.of(FACE_TO_FACE);
        } else if (hearingChannels.contains(VIDEO)) {
            return List.of(VIDEO);
        } else if (hearingChannels.contains(TELEPHONE)) {
            return List.of(TELEPHONE);
        } else {
            throw new HearingChannelNotFoundException();
        }
    }

    public static List<String> getHearingChannelsHmcReference(SscsCaseData caseData) throws HearingChannelNotFoundException {
        return getHearingChannels(caseData).stream().map(HearingChannel::getHmcReference).collect(Collectors.toList());
    }

    public static HearingChannel getIndividualPreferredHearingChannel(HearingSubtype hearingSubtype,
                                                                      HearingOptions hearingOptions) {
        if (isNull(hearingSubtype) || isNull(hearingOptions)) {
            return null;
        }

        if (hearingOptions.isWantsToAttendHearing()) {
            if (isYes(hearingSubtype.getWantsHearingTypeFaceToFace())) {
                return FACE_TO_FACE;
            } else if (shouldPreferVideoHearingChannel(hearingSubtype)) {
                return VIDEO;
            } else if (shouldPreferTelephoneHearingChannel(hearingSubtype)) {
                return TELEPHONE;
            }
        } else {
            return NOT_ATTENDING;
        }

        throw new IllegalStateException("Failed to determine a preferred hearing channel");

    }

    private static boolean shouldPreferVideoHearingChannel(HearingSubtype hearingSubtype) {
        return isYes(hearingSubtype.getWantsHearingTypeVideo())
            && nonNull(hearingSubtype.getHearingVideoEmail());
    }

    private static boolean shouldPreferTelephoneHearingChannel(HearingSubtype hearingSubtype) {
        return isYes(hearingSubtype.getWantsHearingTypeTelephone()) && nonNull(hearingSubtype.getHearingTelephoneNumber());
    }

}
