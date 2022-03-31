package uk.gov.hmcts.reform.sscs.mappers;

import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.servicehearingvalues.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Component
public class ServiceHearingValuesMapper {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
            .autoListFlag(true) // TODO when autoListFlag is created in caseData you will map it with this value Suvarna
            .hearingType(getHearingType(caseData))
            .caseType(caseData.getBenefitCode())
            .caseSubTypes(getIssueCode(caseData))
            .hearingWindow(getHearingWindow(caseData))
            .duration(0) // TODO when duration is created in caseData you will map it with this value
            .hearingPriorityType(getHearingPriority(
                caseData.getAdjournCaseCanCaseBeListedRightAway(),
                caseData.getUrgentCase()
            ).getType())
            .numberOfPhysicalAttendees(getNumberOfPhysicalAttendees(caseData)) // TODO missing mappings
            .hearingInWelshFlag(YesNo.isYes(caseData.getLanguagePreferenceWelsh()))
            .hearingLocations(new ArrayList<>())  // TODO get these values from the method Waqas created
            .caseAdditionalSecurityFlag(getAdditionalSecurityFlag(caseData.getOtherParties(), caseData.getDwpUcb()))
            .facilitiesRequired(getFacilitiesRequired(caseData))
            .listingComments(getListingComments(caseData))
            .hearingRequester(null)
            .privateHearingRequiredFlag(false)
            .leadJudgeContractType(null) // TODO wait for info from Andrew
            .judiciary(null) // TODO check with Suvarna for panel preferences and panel composition
            .hearingIsLinkedFlag(false)
            .parties(getParties(caseData))  // TODO  missing mappings
            .caseFlags(CaseFlags.builder().build()) // TODO Zikrur will provide
            .screenFlow(null)
            .vocabulary(null)
            .hmctsServiceID("BBA3")
            .build();
    }

    private Boolean getAdditionalSecurityFlag(List<CcdValue<OtherParty>> otherParties, String dwpUcb) {
        AtomicReference<Boolean> securityFlag = new AtomicReference<>(false);
        if (Objects.nonNull(otherParties)) {
            otherParties.stream()
                .forEach(party -> {
                    if(YesNo.isYes(party.getValue().getUnacceptableCustomerBehaviour())){
                        securityFlag.set(true);
                    }
                });

        }
        if(YesNo.isYes(dwpUcb)){
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
        if(Objects.nonNull(caseData.getIssueCode())) {
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

    public static HearingWindow getHearingWindow(SscsCaseData caseData) {
        Event dwpResponded = caseData.getEvents().stream()
            .filter(c -> {
                        System.out.println("event type : " + c.getValue());
                        return EventType.DWP_RESPOND.equals(c.getValue().getEventType());
                    }
            )
            .findFirst().orElse(null);

        System.out.println("events : " + caseData.getEvents());


        ZonedDateTime dateTime = Optional.ofNullable(dwpResponded)
            .map(Event::getValue)
            .map(EventDetails::getDateTime)
            .orElse(null);

            if (Objects.nonNull(dateTime)) {
                LocalDate hearingWindowStart;
                if (YesNo.isYes(caseData.getUrgentCase())) {
                    hearingWindowStart = dateTime.plusDays(14).toLocalDate();

                } else {
                    hearingWindowStart = dateTime.plusDays(28).toLocalDate();
                }
                return HearingWindow.builder()
                    .hearingWindowFirstDate(null)
                    .hearingWindowDateRange(HearingWindowDateRange.builder()
                                                .hearingWindowStartDateRange(hearingWindowStart.toString())
                                                .hearingWindowEndDateRange(null)
                                                .build())
                    .build();
            }
        return null;
    }


    public static HearingPriorityType getHearingPriority(String isAdjournCase, String isUrgentCase) {
        // urgentCase Should go to top of queue in LA - also consider case created date
        // Flag to Lauren - how  can this be captured in HMC queue?
        // If there's an adjournment - date shouldn't reset - should also go to top priority
        HearingPriorityType hearingPriorityType = HearingPriorityType.NORMAL;

        // TODO Adjournment - Check what should be used to check if there is adjournment
        if (YesNo.isYes(isUrgentCase) || YesNo.isYes(isAdjournCase)) {
            hearingPriorityType = HearingPriorityType.HIGH;
        }
        return hearingPriorityType;
    }

    // TODO if(face to face) appalents + dwp atendee (1) + judge (1) + panel members + representitive (1)
    private static Integer getNumberOfPhysicalAttendees(SscsCaseData sscsCaseData) {
        int numberOfAttendees = 0;
        // get a value if it is facetoface from hearingSubType -> wantsHearingTypeFaceToFace
        if (Objects.nonNull(sscsCaseData.getAppeal()) &&
            Objects.nonNull(sscsCaseData.getAppeal().getHearingSubtype()) &&
            Objects.nonNull(sscsCaseData.getAppeal().getHearingSubtype().isWantsHearingTypeFaceToFace())) {
            if (sscsCaseData.getAppeal().getHearingSubtype().isWantsHearingTypeFaceToFace()) {
                //appalents + dwp atendee (1) + judge (1) + panel members + representitive (1)
                numberOfAttendees = 1;
                if (YesNo.isYes(sscsCaseData.getAppeal().getHearingOptions().getWantsToAttend())) {
                    numberOfAttendees++;
                }

                if (YesNo.isYes(sscsCaseData.getAppeal().getRep().getHasRepresentative())) {
                    numberOfAttendees++;
                }
                // TODO how to get if there is DWP atendee
                numberOfAttendees += 0;

                // TODO when panelMembers is created in caseData you will map it with the size of this value (Suvarna)
                numberOfAttendees += 0;
            }
        }
        return numberOfAttendees;
    }


    // TODO check it is definetely not this one
    private static String getListingComments(String appellant, List<CcdValue<OtherParty>> otherParties) {
        String listingComments = "";
        if (Objects.nonNull(appellant) && !appellant.isEmpty()) {
            listingComments = appellant + "\n";
        }

        if (!otherParties.isEmpty()) {
            listingComments += otherParties.stream()
                .map(o -> o.getValue().getHearingOptions().getOther())
                .collect(Collectors.joining("\n"));
        }
        return StringUtils.isBlank(listingComments) ? null : listingComments;
    }

    private static String getListingComments(SscsCaseData sscsCaseData) {
        List<String> listingComments = new ArrayList<>();
        if(Objects.nonNull(sscsCaseData.getAppeal()) &&
            Objects.nonNull(sscsCaseData.getAppeal().getHearingOptions()) &&
            Objects.nonNull(sscsCaseData.getAppeal().getHearingOptions().getOther())){
            listingComments.add(sscsCaseData.getAppeal().getHearingOptions().getOther());
        }

        if (Objects.nonNull(sscsCaseData.getOtherParties())) {
            listingComments.addAll(sscsCaseData.getOtherParties().stream()
                                       .map(o -> o.getValue().getHearingOptions().getOther())
                                       .filter(StringUtils::isNotBlank)
                                       .collect(Collectors.toList()));
        }

        return listingComments.isEmpty() ? null : String.join("\n", listingComments);
    }

    private static List<PartyDetails> getParties(SscsCaseData sscsCaseData) { // + appellant
        if(Objects.nonNull(sscsCaseData.getOtherParties())) {
            return sscsCaseData.getOtherParties().stream()
                .map(CcdValue::getValue)
                .map(party -> PartyDetails.builder()
                    .partyID(party.getId())
                    .partyType(PartyType.IND)
                    .partyChannel(getPartyChannel(party.getHearingSubtype()))
                    .partyName(party.getName() == null ? null : party.getName().getFullName())
                    .partyRole(party.getRole() == null ? null : party.getRole().getName())
                    .individualDetails(getIndividualDetails(party))
                    .organisationDetails(getOrganisationDetails(party)) // right now we assume all parties are individuals
                    .unavailabilityDow(null)
                    .unavailabilityRanges(party.getHearingOptions().getExcludeDates().stream()
                                              .map(ex ->  UnavailabilityRange.builder()
                                                            .unavailableFromDate(ex.getValue().getStart())
                                                            .unavailableToDate(ex.getValue().getEnd())
                                                            .build()).collect(Collectors.toList()))
                    .build())
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private static String getPartyChannel(HearingSubtype hearingSubtype) {
        if(Objects.nonNull(hearingSubtype)) {
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

    private static List<String> getReasonableAdjustments(OtherParty party){
        List<String> reasonableAdjustments = new ArrayList<>();
        if(Objects.nonNull(party.getReasonableAdjustment()) &&
            YesNo.isYes(party.getReasonableAdjustment().getWantsReasonableAdjustment())) {
            reasonableAdjustments.add(party.getReasonableAdjustment().getReasonableAdjustmentRequirements());
        }
        return reasonableAdjustments;
    }

    private static IndividualDetails getIndividualDetails(OtherParty party) {
        if(getPartyType(party).equals(PartyType.IND)) {
            return IndividualDetails.builder()
                .title(party.getName() == null ? null : party.getName().getTitle())
                .firstName(party.getName() == null ? null : party.getName().getFirstName())
                .lastName(party.getName() == null ? null :  party.getName().getLastName())
                .preferredHearingChannel(getPartyChannel(party.getHearingSubtype()))
                .interpreterLanguage(party.getHearingOptions() == null ? null : party.getHearingOptions().getLanguages())
                .reasonableAdjustments(getReasonableAdjustments(party))
                .vulnerableFlag(false)
                .vulnerabilityDetails(null)
                .hearingChannelEmail(party.getHearingSubtype() == null ? null : party.getHearingSubtype().getHearingVideoEmail())
                .hearingChannelPhone(party.getHearingSubtype() == null ? null : party.getHearingSubtype().getHearingTelephoneNumber())
                .relatedParties(getRelatedParties(party)) // TODO get them from the method Lucas would provide
                .build();
        }
        return IndividualDetails.builder()
            .build();
    }


    private static List<RelatedParties> getRelatedParties(Appellant appellant){
        /*if(Objects.nonNull(appellant) && Objects.nonNull(appellant.getRelatedParties())) {
            return appellant.getRelatedParties().stream()
                .map(rp -> RelatedParties.builder()
                    .relatedPartyID(rp.getRelatedPartyId())  // TODO find the logic
                    .relationshipType(rp.getRelationshipType()) // TODO find the logic
                    .build()).collect(Collectors.toList());
        }*/
        return new ArrayList<>();
    }
    // TODO Lucas will provide this method
    private static List<RelatedParties> getRelatedParties(OtherParty party){
        /*if(Objects.nonNull(party) && Objects.nonNull(party.getRelatedParties())) {
            return party.getRelatedParties().stream()
                .map(rp -> RelatedParties.builder()
                    .relatedPartyID(rp.getRelatedPartyId()) // TODO find the logic
                    .relationshipType(rp.getRelationshipType()) // TODO find the logic
                    .build()).collect(Collectors.toList());
        }*/
        return new ArrayList<>();
    }

    // TODO right now we assume all parties are Individuals, keeping the method as we may have a logic for organisations
    private static OrganisationDetails getOrganisationDetails(OtherParty party){
        /*
        if(getPartyType(party).equals(PartyType.ORG)) {
            return OrganisationDetails.builder()
                .name("")
                .cftOrganisationID("")
                .organisationType("")
                .build();
        }
        */
        return OrganisationDetails.builder()
            .build();
    }

    // TODO right now we assume all parties are Individuals
    private static PartyType getPartyType(OtherParty party) {
        /*if(Objects.nonNull(party.getOrganisation())){
            return PartyType.ORG;
        }*/
        return PartyType.IND;
    }

    /*
    public static Integer getHearingDuration(SscsCaseData caseData) {
        // TODO the logic here is default value is 30 mins, if there is caseType and caseSubType it is 60 mins,
        //  if there is Adjourn Case Next Hearing Listing Duration you get the value from there. Is this correct>
        int duration = 30;
        if (Objects.nonNull(caseData.getAdjournCaseNextHearingListingDuration())
            && Integer.parseInt(caseData.getAdjournCaseNextHearingListingDuration()) > 0) {
            // TODO Adjournments - Check this is the correct logic for Adjournments
            if ("hours".equalsIgnoreCase(caseData.getAdjournCaseNextHearingListingDurationUnits())) {
                duration = Integer.parseInt(caseData.getAdjournCaseNextHearingListingDuration()) * 60;
            } else {
                // TODO Adjournments - check no other measurement than hours, mins and null
                duration = Integer.parseInt(caseData.getAdjournCaseNextHearingListingDuration());
            }
        }
        else if (Objects.nonNull(caseData.getBenefitCode()) && Objects.nonNull(caseData.getIssueCode())) {
            // TODO Will use Session Category Reference Data
            //      depends on session category, logic to be built (manual override needed)
            duration = 60;
        }
        return duration;
    }

    private static List<String> getReasonableAdjustments(Appellant appellant){
        List<String> reasonableAdjustments = new ArrayList<>();
        if(Objects.nonNull(appellant.getReasonableAdjustment()) &&
            YesNo.isYes(appellant.getReasonableAdjustment().getWantsReasonableAdjustment())) {
            reasonableAdjustments.add(appellant.getReasonableAdjustment().getReasonableAdjustmentRequirements());
        }
        return reasonableAdjustments;
    }

    private static IndividualDetails getAppellantDetails(Appeal appeal) {
        if(Objects.nonNull(appeal) &&
            Objects.nonNull(appeal.getAppellant())) {
            return IndividualDetails.builder()
                .title(appeal.getAppellant().getName().getTitle())
                .firstName(appeal.getAppellant().getName().getFirstName())
                .lastName(appeal.getAppellant().getName().getLastName())
                .preferredHearingChannel(getPartyChannel(appeal.getHearingSubtype()))
                .interpreterLanguage((appeal.getHearingOptions().getLanguageInterpreter()))
                .reasonableAdjustments(getReasonableAdjustments(appeal.getAppellant()))
                .vulnerableFlag(false) // TODO determine the logic
                .vulnerabilityDetails("") //TODO determine the logic
                .hearingChannelEmail(appeal.getHearingSubtype().getHearingVideoEmail())
                .hearingChannelPhone(appeal.getHearingSubtype().getWantsHearingTypeTelephone())
                .relatedParties(getRelatedParties(appeal.getAppellant()))
                .build();
        }
        return null;
    }

    private static CaseFlags getCaseFlags(SscsCaseData sscsCaseData){
        return CaseFlags.builder()
            .flags(getPartyFlags(sscsCaseData))
            .flagAmendUrl("")
            .build();
    }

    private static List<PartyFlags> getPartyFlags(SscsCaseData sscsCaseData) {
        return Optional.ofNullable(sscsCaseData.getOtherParties())
            .orElseGet(Collections::emptyList)
            .stream()
            .map(CcdValue::getValue)
            .map(party -> PartyFlags.builder()
                .partyName(party.getName().getFullName())
                .flagId("")
                .flagDescription("")
                .flagStatus("")
                .flagParentId("")
                .build()).collect(Collectors.toList());
    }

    private String getCaseName(SscsCaseData sscsCaseData) {

        if (Objects.nonNull(sscsCaseData.getAppeal()) &&
            Objects.nonNull(sscsCaseData.getAppeal().getAppellant()) &&
            Objects.nonNull(sscsCaseData.getAppeal().getAppellant().getName())) {
            return sscsCaseData.getAppeal().getAppellant().getName().getFullName();
        }
        return "";
    }

    private static HearingWindow getHearingWindow2(SscsCaseData sscsCaseData) {
        if (sscsCaseData.getHmcHearings().size() > 0) {
            HearingRequest hearingRequest = getHearingRequest(sscsCaseData);

            String hearingWindowFirstDate = null;

            if (hearingRequest.getFirstDateTimeMustBe() != null) {
                hearingWindowFirstDate = hearingRequest.getFirstDateTimeMustBe().format(DATE_TIME_FORMATTER);
            }

            return HearingWindow.builder()
                //  .hearingWindowDateRange(getHearingWindowDateRange(hearingRequest.getHearingWindowRange()))  // TODO confirm which way we should get the hearingWindowRange, hearingRequest?
                .hearingWindowDateRange(getHearingWindowDateRange(getHearingWindowRange(sscsCaseData)))         // TODO or from hearing response
                .hearingWindowFirstDate(hearingWindowFirstDate)
                .build();
        }
        return HearingWindow.builder().build();
    }


    private static HearingWindowDateRange getHearingWindowDateRange(HearingWindowRange hearingWindowRange) {
        if (hearingWindowRange != null) {
            return HearingWindowDateRange.builder()
                .hearingWindowStartDateRange(hearingWindowRange.getDateRangeStart().format(DATE_TIME_FORMATTER))
                .hearingWindowEndDateRange(hearingWindowRange.getDateRangeEnd().format(DATE_TIME_FORMATTER))
                .build();
        }

        return HearingWindowDateRange.builder().build();
    }


    public static HearingWindowRange getHearingWindowRange(SscsCaseData caseData) {
        if (YesNo.isYes(caseData.getAutoListFlag())) {
            Event dwpResponded = caseData.getEvents().stream()
                .filter(c -> c.getValue().getEventType().equals(EventType.DWP_RESPOND))
                .findFirst().orElse(null);
            if (Objects.nonNull(dwpResponded) && Objects.nonNull(dwpResponded.getValue())) {
                LocalDate hearingWindowStart = dwpResponded.getValue().getDateTime().plusMonths(1).toLocalDate();
                return HearingWindowRange.builder().dateRangeStart(hearingWindowStart).build();
            }
        }
        return null;
    }

    public static List<uk.gov.hmcts.reform.sscs.model.servicehearingvalues.HearingLocation> getHearingLocations(SscsCaseData sscsCaseData) {
        HearingRequest hearingRequest = getHearingRequest(sscsCaseData);
        CaseManagementLocation caseManagementLocation = sscsCaseData.getCaseManagementLocation();

        if (hearingRequest.getHearingLocations() != null) {
            return hearingRequest.getHearingLocations().stream()
                .map(CcdValue::getValue)
                .map(location -> uk.gov.hmcts.reform.sscs.model.servicehearingvalues.HearingLocation
                    .builder()
                    .locationId(location.getLocationId())
                    .locationName(caseManagementLocation.getBaseLocation())
                    .locationType(location.getHearingLocationType().getType())
                    .region(caseManagementLocation.getRegion())
                    .build()).collect(Collectors.toList());
            // TODO check if we are correct with getting location id from hearingRequest -> hearing locations -> location id
            //                                           location name from case Management Location -> base location
            //                                           location type from hearing request -> hearing locations -> type
            //                                           region from case Management Location -> region
        }
        return new ArrayList<>();
    }

    public static String getLeadJudgeContractType(SscsCaseData sscsCaseData) {
        HearingRequest hearingRequest = getHearingRequest(sscsCaseData);
        String leadJudgeContractType = "";
        if (!StringUtils.isBlank(hearingRequest.getLeadJudgeContractType())) {
            leadJudgeContractType = hearingRequest.getLeadJudgeContractType();
        }
        return leadJudgeContractType;
    }

    public static Judiciary getJudiciary(SscsCaseData sscsCaseData) {
        HearingRequest hearingRequest = getHearingRequest(sscsCaseData);

        if (!StringUtils.isBlank(hearingRequest.getLeadJudgeContractType())) {
            PanelRequirements panelRequirements = hearingRequest.getPanelRequirements();
            return Judiciary.builder()
                    .judiciaryPreferences(panelRequirements.getPanelPreferences().stream()
                                              .map(CcdValue::getValue)
                                              .map(pf -> uk.gov.hmcts.reform.sscs.model.servicehearingvalues.PanelPreference.builder()
                                                  .memberID(pf.getMemberId())
                                                  .memberType(MemberType.valueOf(pf.getMemberType()))
                                                  .requirementType(RequirementType.valueOf(pf.getHearingRequirementType().getKey())).build()).collect(
                            Collectors.toList()))
                    .judiciarySpecialisms(panelRequirements.getPanelSpecialisms().stream()
                                              .map(CcdValue::getValue)
                                              .collect(Collectors.toList()))
                    .panelComposition(null) // TODO what is the mapping for this one? (could not spot in commons)
                    .authorisationSubType(panelRequirements.getAuthorisationSubType().stream()
                                              .map(CcdValue::getValue)
                                              .collect(Collectors.toList()))
                    .authorisationTypes(panelRequirements.getAuthorisationTypes().stream()
                                              .map(CcdValue::getValue)
                                              .collect(Collectors.toList()))
                    .roleType(panelRequirements.getRoleTypes().stream()
                                            .map(CcdValue::getValue)
                                            .collect(Collectors.toList()))
                    .build();

            // TODO is it correct to get judiciaryPreferences from hearingRequest -> panel Requirements -> Panel Preferences
            //                           judiciary Specialisms from hearingRequest -> panel Requirements -> Panel Specialisms
            //                           authorisation Sub Type from hearingRequest -> panel Requirements -> authorisation Sub Types
            //                           authorisation Types from hearingRequest -> panel Requirements -> authorisation  Types
            //                           judiciary Specialisms from hearingRequest -> panel Requirements -> role types


        }
        return Judiciary.builder().build();
    }

    private static HearingRequest getHearingRequest(SscsCaseData sscsCaseData) {
        if (Objects.nonNull(sscsCaseData.getHmcHearings()) &&
            sscsCaseData.getHmcHearings().size() > 0) { // TODO do we need a null check for hmcHearing
            HmcHearing hmcHearing = sscsCaseData.getHmcHearings().get(0); // TODO is this is the right way to retrieve the hmc hearings (we get the first one)

            return Optional.ofNullable(hmcHearing.getValue())
                .map(HmcHearingDetails::getHearingRequest)
                .filter(Objects::nonNull)
                .stream().findFirst()
                .orElse(HearingRequest.builder().build());
        }
        return HearingRequest.builder().build();
    }

    private static Integer getNumberOfPhysicalAttendees(SscsCaseData sscsCaseData) {
        if (sscsCaseData.getHmcHearings().size() > 0) {
            HmcHearing hmcHearing = sscsCaseData.getHmcHearings().get(0);   // TODO is this is the right way to retrieve the hmc hearings (we get the first one)
                                                                            // TODO we get if from hearing respnose -> hearing day schedule -> number of attendees

            HearingDaySchedule hearingDaySchedule = Optional.ofNullable(hmcHearing.getValue())
                .map(HmcHearingDetails::getHearingResponse)
                .filter(Objects::nonNull)
                .map(HearingResponse::getHearingDaySchedule)
                .filter(Objects::nonNull)
                .stream().findFirst()
                .orElse(null);

            if (Objects.nonNull(hearingDaySchedule) && Objects.nonNull(hearingDaySchedule.getAttendees())) {
                numberOfAttendees = hearingDaySchedule.getAttendees().size();
            }
        }
        return numberOfAttendees;
    }

    private String getCaseName(SscsCaseData sscsCaseData) {
       return Optional.ofNullable(sscsCaseData.getAppeal())
                .map(Appeal::getAppellant)
                .map(Appellant::getName)
                .map(Name::getFullName)
                .orElse("");
        /*
        if (Objects.nonNull(sscsCaseData.getAppeal()) &&
            Objects.nonNull(sscsCaseData.getAppeal().getAppellant()) &&
            Objects.nonNull(sscsCaseData.getAppeal().getAppellant().getName())) {
            return sscsCaseData.getAppeal().getAppellant().getName().getFullName();
        }
        return "";
    }
    */
}
