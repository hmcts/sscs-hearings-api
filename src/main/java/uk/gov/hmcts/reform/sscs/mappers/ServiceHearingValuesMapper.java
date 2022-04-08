package uk.gov.hmcts.reform.sscs.mappers;

import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.hearing.mapping.PartyFlagsMapping;
import uk.gov.hmcts.reform.sscs.helper.HearingsMapping;
import uk.gov.hmcts.reform.sscs.model.servicehearingvalues.ShvCaseFlags;
import uk.gov.hmcts.reform.sscs.model.servicehearingvalues.ShvHearingWindow;
import uk.gov.hmcts.reform.sscs.model.servicehearingvalues.ShvHearingWindowDateRange;
import uk.gov.hmcts.reform.sscs.model.servicehearingvalues.ShvPartyDetails;
import uk.gov.hmcts.reform.sscs.model.servicehearingvalues.UnavailabilityRange;
import uk.gov.hmcts.reform.sscs.model.single.hearing.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Component
public class ServiceHearingValuesMapper {
    public static final String FACE_TO_FACE = "faceToFace";
    public static final String TELEPHONE = "telephone";
    public static final String VIDEO = "video";
    public static final String PAPER = "paper";

    public ServiceHearingValues mapServiceHearingValues(SscsCaseDetails caseDetails) {
        if (caseDetails == null) {
            return null;
        }

        SscsCaseData caseData = caseDetails.getData();

        return ServiceHearingValues.builder()
                .caseName(getCaseName(caseData))
                .autoListFlag(false) // TODO to be provided in a future story, right now not populated
                .hearingType(getHearingType(caseData))
                .caseType(caseData.getBenefitCode())
                .caseSubTypes(getIssueCode(caseData))
                // TODO a method doing the same thing is in HearingsDetailsMapping -> buildHearingWindow
                //  use that one (SSCS-10321-Create-Hearing-POST-Mapping)
                .shvHearingWindow(getHearingWindow(caseData))
                .duration(0) // TODO SSCS-10116 will provide
                .hearingPriorityType(getHearingPriority(
                    caseData.getAdjournCaseCanCaseBeListedRightAway(),
                    caseData.getUrgentCase()
                ).getType())
                .numberOfPhysicalAttendees(getNumberOfPhysicalAttendees(caseData)) // TODO missing mappings
                // TODO caseData.getLanguagePreferenceWelsh() is for bilingual documents only, future work
                .hearingInWelshFlag(YesNo.isYes("No"))
                // TODO INFO this is the part which differs from SSCS-10260, we are getting the hearing locations value
                //  from the method which was created in SSCS-10245. SSCS-10245 pulled the changes from SSCS-10321
                //  so if you make a branch comparison with SSCS-10260 you will see what is included in this branch to
                //  allow making a call to  HearingsMapping.getHearingLocations method
                .hearingLocations(getHearingLocation(caseData.getCaseManagementLocation()))
                // TODO the method below "getAdditionalSecurityFlag" is already created in
                //  SSCS-10321-Create-Hearing-POST-Mapping, HearingsCaseMapping ->  shouldBeAdditionalSecurityFlag
                //  use that method
                .caseAdditionalSecurityFlag(getAdditionalSecurityFlag(caseData.getOtherParties(), caseData.getDwpUcb()))
                .facilitiesRequired(getFacilitiesRequired(caseData))
                .listingComments(getListingComments(caseData.getAppeal(), caseData.getOtherParties()))
                .hearingRequester(null)
                .privateHearingRequiredFlag(false)
                .leadJudgeContractType(null) // TODO ref data isn't available yet. List Assist may handle this value
                .shvJudiciary(null) // TODO
                .hearingIsLinkedFlag(false)
                .shvParties(getParties(caseData)) // TODO missing mappings
                .shvCaseFlags(getCaseFlags(caseData))
                .shvScreenFlow(null)
                .shvVocabulary(null)
                .hmctsServiceID("BBA3")
            .build();
    }

    private List<HearingLocations> getHearingLocation(CaseManagementLocation caseManagementLocation) {
        if(Objects.nonNull(caseManagementLocation)) {
            return HearingsMapping.getHearingLocations(caseManagementLocation);
        }
        return new ArrayList<>();
    }

    private Boolean getAdditionalSecurityFlag(List<CcdValue<OtherParty>> otherParties, String dwpUcb) {
        AtomicReference<Boolean> securityFlag = new AtomicReference<>(false);
        if (Objects.nonNull(otherParties)) {
            otherParties.stream()
                .forEach(party -> {
                    if (YesNo.isYes(party.getValue().getUnacceptableCustomerBehaviour())) {
                        securityFlag.set(true);
                    }
                });

        }
        if (YesNo.isYes(dwpUcb)) {
            securityFlag.set(true);
        }
        return securityFlag.get();
    }

    private String getHearingType(SscsCaseData caseData) {
        return Optional.ofNullable(caseData.getAppeal())
            .map(Appeal::getHearingType)
            .orElse("");
    }

    private List<String> getIssueCode(SscsCaseData caseData) {
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

    private String getCaseName(SscsCaseData sscsCaseData) {
        return Optional.ofNullable(sscsCaseData.getAppeal())
            .map(Appeal::getAppellant)
            .map(Appellant::getName)
            .map(Name::getFullName)
            .orElse("");
    }

    public static ShvHearingWindow getHearingWindow(SscsCaseData caseData) {
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

        return ShvHearingWindow.builder()
            .hearingWindowFirstDate(null)
            .shvHearingWindowDateRange(ShvHearingWindowDateRange.builder()
                                        .hearingWindowStartDateRange(hearingWindowStart)
                                        .hearingWindowEndDateRange(null)
                                        .build())
            .build();
    }


    public static HearingPriorityType getHearingPriority(String isAdjournCase, String isUrgentCase) {
        HearingPriorityType hearingPriorityType = HearingPriorityType.NORMAL;

        if (YesNo.isYes(isUrgentCase) || YesNo.isYes(isAdjournCase)) {
            hearingPriorityType = HearingPriorityType.HIGH;
        }
        return hearingPriorityType;
    }

    // TODO if(face to face) appalents + dwp atendee (1) + judge (1) + panel members + representitive (1)
    private static Integer getNumberOfPhysicalAttendees(SscsCaseData sscsCaseData) {
        int numberOfAttendees = 0;
        // get a value if it is facetoface from hearingSubType -> wantsHearingTypeFaceToFace
        if (Objects.nonNull(sscsCaseData.getAppeal())
            && Objects.nonNull(sscsCaseData.getAppeal().getHearingSubtype())
            && Objects.nonNull(sscsCaseData.getAppeal().getHearingSubtype().isWantsHearingTypeFaceToFace())
            && sscsCaseData.getAppeal().getHearingSubtype().isWantsHearingTypeFaceToFace()) {
            //appalents + dwp atendee (1) + judge (1) + panel members + representitive (1)
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


    private static String getListingComments(Appeal appeal, List<CcdValue<OtherParty>> otherParties) {
        List<String> listingComments = new ArrayList<>();
        if (Objects.nonNull(appeal)
            && Objects.nonNull(appeal.getHearingOptions())
            && Objects.nonNull(appeal.getHearingOptions().getOther())) {
            listingComments.add(appeal.getHearingOptions().getOther());
        }

        if (Objects.nonNull(otherParties)) {
            listingComments.addAll(otherParties.stream()
                                       .map(o -> o.getValue().getHearingOptions().getOther())
                                       .filter(StringUtils::isNotBlank)
                                       .collect(Collectors.toList()));
        }

        return listingComments.isEmpty() ? null : String.join("\n", listingComments);
    }


    private static List<ShvPartyDetails> getParties(SscsCaseData sscsCaseData) { // + appellant
        if (Objects.nonNull(sscsCaseData.getOtherParties())) {
            return sscsCaseData.getOtherParties().stream()
                .map(CcdValue::getValue)
                .map(party -> ShvPartyDetails.builder()
                    .partyID(party.getId())
                    .partyType(PartyType.IND) // TODO use getPartyType below when sscs-common SSCS-10056 is merged
                    .partyChannel(getPartyChannel(party.getHearingSubtype()))
                    .partyName(party.getName() == null ? null : party.getName().getFullName())
                    .partyRole(party.getRole() == null ? null : party.getRole().getName())
                    .individualDetails(getIndividualDetails(party, sscsCaseData))
                    .organisationDetails(getOrganisationDetails(party)) // we assume all parties are individuals
                    .unavailabilityDow(null)
                    .unavailabilityRanges(getPartyUnavailabilityRange(party.getHearingOptions()))
                    .build())
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /* TODO use this one when Entity is merged and has organisatuon field
    public static String getPartyType(Entity entity) {
        return isNotBlank(entity.getOrganisation()) ? ORG.name() : IND.name();
    }
    */

    private static List<UnavailabilityRange> getPartyUnavailabilityRange(HearingOptions hearingOptions) {
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


    private static String getPartyChannel(HearingSubtype hearingSubtype) {
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

    private static List<String> getReasonableAdjustments(OtherParty party) {
        List<String> reasonableAdjustments = new ArrayList<>();
        if (Objects.nonNull(party.getReasonableAdjustment())
            && YesNo.isYes(party.getReasonableAdjustment().getWantsReasonableAdjustment())) {
            reasonableAdjustments.add(party.getReasonableAdjustment().getReasonableAdjustmentRequirements());
        }
        return reasonableAdjustments;
    }

    // TODO check with what is done in SSCS-10321-Create-Hearing-POST-Mapping when it is merged
    private static IndividualDetails getIndividualDetails(OtherParty party, SscsCaseData sscsCaseData) {
        // put this line to suppress PMD error, sscsCaseData would be needed to get realatedParties
        sscsCaseData.getAppeal();
        if (getPartyType(party).equals(PartyType.IND)) {
            return IndividualDetails.builder()
                .title(party.getName() == null ? null : party.getName().getTitle())
                .firstName(party.getName() == null ? null : party.getName().getFirstName())
                .lastName(party.getName() == null ? null :  party.getName().getLastName())
                .preferredHearingChannel(getPartyChannel(party.getHearingSubtype()))
                .interpreterLanguage(party.getHearingOptions() == null ? null
                                         : party.getHearingOptions().getLanguages())
                .reasonableAdjustments(getReasonableAdjustments(party))
                .vulnerableFlag(false)
                .vulnerabilityDetails(null)
                .hearingChannelEmail(party.getHearingSubtype() == null ? null
                                         : party.getHearingSubtype().getHearingVideoEmail())
                .hearingChannelPhone(party.getHearingSubtype() == null ? null
                                         : party.getHearingSubtype().getHearingTelephoneNumber())

                // TODO missing mapping et them from the method  in SSCS-10245-send-epimsID-to-HMC,
                // call with the order HearingsMapping ->  updateIds(wrapper), buildRelatedParties(wrapper)
                .relatedParties(new ArrayList<>())
                .build();
        }
        return IndividualDetails.builder()
            .build();
    }

    // TODO right now we assume all parties are Individuals,
    //  keeping the method as we may have a logic for organisations in the future
    private static OrganisationDetails getOrganisationDetails(OtherParty party) {
        /*
        if(getPartyType(party).equals(PartyType.ORG)) {
            return OrganisationDetails.builder()
                .name("")
                .cftOrganisationID("")
                .organisationType("")
                .build();
        }
        */
        // put this line to suppress PMD error
        party.getHearingOptions();
        return OrganisationDetails.builder()
            .build();
    }

    private static ShvCaseFlags getCaseFlags(SscsCaseData sscsCaseData) {
        return ShvCaseFlags.builder()
            .shvFlags(PartyFlagsMapping.getPartyFlags(sscsCaseData).stream()
                       .filter(Objects::nonNull)
                       .collect(Collectors.toList()))
            .flagAmendUrl(null)
            .build();
    }

    // TODO right now we assume all parties are Individuals
    private static PartyType getPartyType(OtherParty party) {
        /*if(Objects.nonNull(party.getOrganisation())){
            return PartyType.ORG;
        }*/
        // put this line to suppress PMD error
        party.getHearingOptions();
        return PartyType.IND;
    }
}
