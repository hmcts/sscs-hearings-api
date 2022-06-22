package uk.gov.hmcts.reform.sscs.helper.mapping;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.sscs.ccd.domain.CcdValue;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingSubtype;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.YES;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsPartiesMapping.getIndividualPreferredHearingChannel;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel.FACE_TO_FACE;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel.NOT_ATTENDING;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel.TELEPHONE;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel.VIDEO;

@Slf4j
public final class HearingChannelMapping {

    private HearingChannelMapping() {

    }

    static List<HearingChannel> getHearingChannel(SscsCaseData caseData) {
        if (caseData.getDwpIsOfficerAttending() != null && caseData.getDwpIsOfficerAttending().equals(YES.getValue())) {
            return Collections.singletonList(FACE_TO_FACE);
        }

        List<HearingChannel> hearingChannels = new ArrayList<>();

        HearingChannel individualPreferredHearingChannel = getIndividualPreferredHearingChannel(
            caseData.getAppeal().getHearingSubtype(),
            caseData.getAppeal().getHearingOptions()
        );

        if (isNull(individualPreferredHearingChannel)) {
            log.error("Individual Preferred Hearing Channel returned Null");
            return null;
        }

        hearingChannels.add(individualPreferredHearingChannel);

        if (nonNull(caseData.getOtherParties())) {
            hearingChannels.addAll(caseData.getOtherParties().stream().map(CcdValue::getValue).map(
                otherParty -> getIndividualPreferredHearingChannel(
                    otherParty.getHearingSubtype(),
                    otherParty.getHearingOptions()
                )).collect(Collectors.toList()));
        }

        if (hearingChannels.contains(FACE_TO_FACE)) {
            return Collections.singletonList(FACE_TO_FACE);
        } else if (hearingChannels.contains(VIDEO)) {
            return Collections.singletonList(VIDEO);
        } else if (hearingChannels.contains(TELEPHONE)) {
            return Collections.singletonList(TELEPHONE);
        } else {
            return Collections.singletonList(NOT_ATTENDING);
        }
    }

    static String getPreferredHearingChannelReference(HearingSubtype hearingSubtype, HearingOptions hearingOptions) {
        if (isNull(hearingSubtype) || isNull(hearingOptions)) {
            return null;
        }
        return getIndividualPreferredHearingChannel(hearingSubtype, hearingOptions).getHmcReference();
    }

}
