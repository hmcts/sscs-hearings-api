package uk.gov.hmcts.reform.sscs.utils;

import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingsPartiesMapping;
import uk.gov.hmcts.reform.sscs.helper.mapping.PartyFlagsMapping;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.CaseFlags;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.HearingWindow;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.HearingWindowDateRange;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PartyDetails;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class SscsCaseDataUtils {

    private SscsCaseDataUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String getHearingType(SscsCaseData caseData) {
        return Optional.ofNullable(caseData.getAppeal())
                .map(Appeal::getHearingType)
                .orElse("");
    }

    public static List<String> getIssueCode(SscsCaseData caseData) {
        if (Objects.nonNull(caseData.getIssueCode())) {
            return List.of(caseData.getIssueCode());
        }
        return new ArrayList<>();
    }

    public static List<String> getFacilitiesRequired(SscsCaseData sscsCaseData) {
        return Optional.ofNullable(sscsCaseData.getAppeal())
                .map(Appeal::getHearingOptions)
                .map(HearingOptions::getArrangements)
                .orElse(new ArrayList<>());
    }

    public static String getCaseName(SscsCaseData sscsCaseData) {
        return Optional.ofNullable(sscsCaseData.getAppeal())
                .map(Appeal::getAppellant)
                .map(Appellant::getName)
                .map(Name::getFullName)
                .orElse("");
    }

    public static int getNumberOfPhysicalAttendees(SscsCaseData sscsCaseData) {
        int numberOfAttendees = 0;
        // get a value if it is facetoface from hearingSubType -> wantsHearingTypeFaceToFace
        if (Objects.nonNull(sscsCaseData.getAppeal())
                && Objects.nonNull(sscsCaseData.getAppeal().getHearingSubtype())
                && Objects.nonNull(sscsCaseData.getAppeal().getHearingSubtype().isWantsHearingTypeFaceToFace())
                && sscsCaseData.getAppeal().getHearingSubtype().isWantsHearingTypeFaceToFace()) {
            //appellants + dwp attendee (1) + judge (1) + panel members + representative (1)
            numberOfAttendees = 1;
            if (YesNo.isYes(sscsCaseData.getAppeal().getHearingOptions().getWantsToAttend())) {
                numberOfAttendees++;
            }

            if (YesNo.isYes(sscsCaseData.getAppeal().getRep().getHasRepresentative())) {
                numberOfAttendees++;
            }
            // TODO get it from SSCS-10243, when it is finished
            numberOfAttendees += 0;

            // TODO when panelMembers is created in caseData you will map it with the size of this value
            //  (SSCS-10116)
            numberOfAttendees += 0;
        }
        return numberOfAttendees;
    }

    /**
     * We assume all parties are individuals.
     *
     * @param sscsCaseData an SscsCaseDate object
     * @return List of ShvPartyDetails
     */
    public static List<PartyDetails> getParties(SscsCaseData sscsCaseData) { // + appellant
        if (Objects.nonNull(sscsCaseData.getOtherParties())) {
            return sscsCaseData.getOtherParties().stream()
                    .map(CcdValue::getValue)
                    .map(party -> PartyDetails.builder()
                            .partyID(party.getId())
                            .partyType(PartyDetailsUtils.getPartyType(party))
                            .partyChannel(HearingUtils.getPartyChannel(party.getHearingSubtype()))
                            .partyName(party.getName() == null ? null : party.getName().getFullName())
                            .partyRole(party.getRole() == null ? null : party.getRole().getName())
                            .individualDetails(PartyDetailsUtils.getIndividualDetails(party, sscsCaseData))
                            .organisationDetails(PartyDetailsUtils.getOrganisationDetails(party))
                            .unavailabilityDow(null)
                            .unavailabilityRanges(HearingsPartiesMapping.getPartyUnavailabilityRange(party.getHearingOptions()))
                            .build())
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * Get ShvHearingWindow object given SscsCaseData.
     *
     * @param caseData SscsCaseData
     * @return an ShvHearingWindow.
     */
    public static HearingWindow getHearingWindow(SscsCaseData caseData) {
        String hearingWindowStart = null;
        if (Objects.nonNull(caseData.getEvents())) {
            Event dwpResponded = caseData.getEvents().stream()
                    .filter(c -> EventType.DWP_RESPOND.equals(c.getValue().getEventType()))
                    .findFirst().orElse(null);

            ZonedDateTime dwpResponseDateTime = Optional.ofNullable(dwpResponded)
                    .map(Event::getValue)
                    .map(EventDetails::getDateTime)
                    .orElse(null);

            if (Objects.nonNull(dwpResponseDateTime)) {
                if (YesNo.isYes(caseData.getUrgentCase())) {
                    hearingWindowStart = dwpResponseDateTime.plusDays(14).toLocalDate().toString();

                } else {
                    hearingWindowStart = dwpResponseDateTime.plusDays(28).toLocalDate().toString();
                }
            }
        }

        return HearingWindow.builder()
                .hearingWindowFirstDate(null)
                .hearingWindowDateRange(HearingWindowDateRange.builder()
                        .hearingWindowStartDateRange(hearingWindowStart)
                        .hearingWindowEndDateRange(null)
                        .build())
                .build();
    }


    public static CaseFlags getCaseFlags(SscsCaseData sscsCaseData) {
        return CaseFlags.builder()
                .flags(PartyFlagsMapping.getPartyFlags(sscsCaseData).stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .flagAmendUrl(null)
                .build();
    }
}
