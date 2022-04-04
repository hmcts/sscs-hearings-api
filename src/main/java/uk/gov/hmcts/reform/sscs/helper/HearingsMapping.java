package uk.gov.hmcts.reform.sscs.helper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.ccd.domain.RelatedParty;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.*;
import uk.gov.hmcts.reform.sscs.model.single.hearing.CaseDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.CaseDetails.CaseDetailsBuilder;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingRequestPayload.HearingRequestPayloadBuilder;
import uk.gov.hmcts.reform.sscs.model.single.hearing.IndividualDetails.IndividualDetailsBuilder;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PartyDetails.PartyDetailsBuilder;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RequestDetails.RequestDetailsBuilder;
import uk.gov.hmcts.reform.sscs.model.single.hearing.UnavailabilityRange.UnavailabilityRangeBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;

@SuppressWarnings({"PMD.LinguisticNaming","PMD.UnnecessaryLocalBeforeReturn"})
// TODO Unsuppress in future
public final class HearingsMapping {

    public static final String CASE_TYPE = "caseType";
    public static final String CASE_SUB_TYPE = "caseSubType";
    public static final String OTHER_PARTY = "OtherParty";
    public static final String REPRESENTATIVE = "Representative";
    public static final String APPOINTEE = "Appointee";
    public static final String APPELLANT = "Appellant";
    public static final String NORMAL = "Normal";
    public static final String HIGH = "High";
    @Value("${exui.url}")
    private static String exUiUrl;

    @Value("${sscs.serviceCode}")
    private static String sscsServiceCode;

    private HearingsMapping() {

    }

    public static void updateIds(HearingWrapper wrapper) {
        SscsCaseData caseData = wrapper.getUpdatedCaseData();
        Appeal appeal = caseData.getAppeal();
        Appellant appellant = appeal.getAppellant();
        Integer maxId = getMaxId(caseData.getOtherParties(), appellant);
        updateEntityId(appellant, maxId);
        if (nonNull(appellant.getAppointee())) {
            updateEntityId(appellant.getAppointee(), maxId);
        }
        if (nonNull(appeal.getRep())) {
            updateEntityId(appellant.getRep(), maxId);
        }
        if (nonNull(caseData.getOtherParties())) {
            for (CcdValue<OtherParty> otherPartyCcdValue : caseData.getOtherParties()) {
                OtherParty otherParty = otherPartyCcdValue.getValue();
                updateEntityId(otherParty, maxId);
                if (nonNull(otherParty.getAppointee())) {
                    updateEntityId(otherParty.getAppointee(), maxId);
                }
                if (nonNull(otherParty.getRep())) {
                    updateEntityId(otherParty.getRep(), maxId);
                }
            }
        }
    }

    public static void updateEntityId(Entity entity, Integer maxId) {
        String id = entity.getId();
        if (isNotBlank(id)) {
            id = String.valueOf(++maxId);
        }
        entity.setId(id);
    }

    public static int getMaxId(List<CcdValue<OtherParty>> otherParties, Party appellant) {
        List<Integer> currentIds = new ArrayList<>();
        if (nonNull(otherParties)) {
            for (CcdValue<OtherParty> ccdOtherParty : otherParties) {
                currentIds.addAll(getMaxId(ccdOtherParty.getValue()));
            }
        }

        currentIds.addAll(getMaxId(appellant));

        return currentIds.stream().max(Comparator.naturalOrder()).orElse(0);
    }

    public static List<Integer> getMaxId(Party party) {
        List<Integer> currentIds = new ArrayList<>();

        if (party.getId() != null) {
            currentIds.add(Integer.parseInt(party.getId()));
        }
        if (party.getAppointee() != null && party.getAppointee().getId() != null) {
            currentIds.add(Integer.parseInt(party.getAppointee().getId()));
        }
        if (party.getRep() != null && party.getRep().getId() != null) {
            currentIds.add(Integer.parseInt(party.getRep().getId()));
        }

        return currentIds;
    }

    public static void buildRelatedParties(HearingWrapper wrapper) {
        SscsCaseData caseData = wrapper.getUpdatedCaseData();
        Appeal appeal = caseData.getAppeal();
        Appellant appellant = appeal.getAppellant();
        List<String> allPartiesIds = new ArrayList<>();
        if (nonNull(caseData.getOtherParties())) {
            allPartiesIds.addAll(caseData.getOtherParties().stream()
                    .map(o -> o.getValue().getId())
                    .collect(Collectors.toList()));
        }

        allPartiesIds.add(appellant.getId());
        updateEntityRelatedParties(appellant, APPELLANT, allPartiesIds);
        if (isYes(appellant.getIsAppointee()) && nonNull(appellant.getAppointee())) {
            updateEntityRelatedParties(appellant.getAppointee(), APPOINTEE, List.of(appellant.getId()));
        }
        Representative rep = appeal.getRep();
        if (nonNull(rep) && isYes(rep.getHasRepresentative())) {
            updateEntityRelatedParties(appellant.getRep(), REPRESENTATIVE, List.of(appellant.getId()));
        }

        if (nonNull(caseData.getOtherParties())) {
            for (CcdValue<OtherParty> otherPartyCcdValue: caseData.getOtherParties()) {
                OtherParty otherParty = otherPartyCcdValue.getValue();
                updateEntityRelatedParties(otherParty, OTHER_PARTY, allPartiesIds);
                if (otherParty.hasAppointee() && nonNull(otherParty.getAppointee())) {
                    updateEntityRelatedParties(otherParty.getAppointee(), APPOINTEE, List.of(otherParty.getId()));
                }
                if (otherParty.hasRepresentative() && nonNull(otherParty.getRep())) {
                    updateEntityRelatedParties(otherParty.getRep(), REPRESENTATIVE, List.of(otherParty.getId()));
                }
            }
        }
    }

    public static void updateEntityRelatedParties(Entity entity, String partyRole,  List<String> ids) {
        List<RelatedParty> relatedParties = new ArrayList<>();
        // TODO ref data that hasn't been published yet

        for (String id : ids) {
            relatedParties.add(RelatedParty.builder()
                    .relatedPartyId(id)
                    .relationshipType(partyRole)
                    .build());
        }

        entity.setRelatedParties(relatedParties);
    }

    public static HearingRequestPayload buildCreateHearingPayload(HearingWrapper wrapper) {
        HearingRequestPayloadBuilder requestPayloadBuilder = HearingRequestPayload.builder();

        requestPayloadBuilder.requestDetails(createHmcRequestDetails(wrapper));
        requestPayloadBuilder.hearingDetails(createHearingRequestDetails(wrapper));
        requestPayloadBuilder.caseDetails(createHearingRequestCaseDetails(wrapper));
        requestPayloadBuilder.partiesDetails(createHearingPartiesDetails(wrapper));

        return requestPayloadBuilder.build();
    }

    public static RequestDetails createHmcRequestDetails(HearingWrapper wrapper) {
        RequestDetailsBuilder hmcRequestDetailsBuilder = RequestDetails.builder();
        hmcRequestDetailsBuilder.versionNumber(wrapper.getUpdatedCaseData().getSchedulingAndListingFields().getActiveHearingVersionNumber());
        return hmcRequestDetailsBuilder.build();
    }

    public static List<PartyDetails> createHearingPartiesDetails(HearingWrapper wrapper) {
        List<PartyDetails> partiesDetails = new ArrayList<>();

        Appeal appeal = wrapper.getUpdatedCaseData().getAppeal();
        Appellant appellant = appeal.getAppellant();

        partiesDetails.add(createHearingPartyDetails(appellant, appeal.getHearingOptions(), appeal.getHearingType(), appeal.getHearingSubtype()));
        if (isYes(appellant.getIsAppointee()) && nonNull(appellant.getAppointee())) {
            partiesDetails.add(createHearingPartyDetails(appellant.getAppointee(), appeal.getHearingOptions(), appeal.getHearingType(), appeal.getHearingSubtype()));
        }
        if (nonNull(appeal.getRep()) && isYes(appeal.getRep().getHasRepresentative()) && nonNull(appellant.getRep())) {
            partiesDetails.add(createHearingPartyDetails(appellant.getRep(),appeal.getHearingOptions(), appeal.getHearingType(), appeal.getHearingSubtype()));
        }

        List<CcdValue<OtherParty>> otherParties = wrapper.getUpdatedCaseData().getOtherParties();

        if (nonNull(otherParties)) {
            for (CcdValue<OtherParty> ccdOtherParty : otherParties) {
                OtherParty otherParty = ccdOtherParty.getValue();
                partiesDetails.add(createHearingPartyDetails(otherParty, otherParty.getHearingOptions(), null, otherParty.getHearingSubtype()));
                if (otherParty.hasAppointee() && nonNull(otherParty.getAppointee())) {
                    partiesDetails.add(createHearingPartyDetails(otherParty.getAppointee(), otherParty.getHearingOptions(), null, otherParty.getHearingSubtype()));
                }
                if (otherParty.hasRepresentative() && nonNull(otherParty.getRep())) {
                    partiesDetails.add(createHearingPartyDetails(otherParty.getRep(), otherParty.getHearingOptions(), null, otherParty.getHearingSubtype()));
                }
            }
        }

        return partiesDetails;
    }

    public static PartyDetails createHearingPartyDetails(Entity entity, HearingOptions hearingOptions, String hearingType, HearingSubtype hearingSubtype) {
        PartyDetailsBuilder partyDetails = PartyDetails.builder();

        partyDetails.partyID(getPartyId(entity));
        partyDetails.partyType(getPartyType(entity));
        partyDetails.partyRole(getPartyRole(entity));
        partyDetails.individualDetails(getPartyIndividualDetails(entity, hearingOptions, hearingType, hearingSubtype));
        partyDetails.organisationDetails(getPartyOrganisationDetails());
        partyDetails.unavailabilityDayOfWeek(getPartyUnavailabilityDayOfWeek());
        partyDetails.unavailabilityRanges(getPartyUnavailabilityRange(hearingOptions));

        return partyDetails.build();
    }

    public static String getPartyId(Entity entity) {
        return entity.getId();
    }


    public static String getPartyRole(Entity entity) {
        // TODO Lucas - Andrew unsure what this should be
        String role = "";
        if (nonNull(entity.getRole())) {
            role = entity.getRole().getName();
        } else {
            if (entity instanceof Appellant) {
                role = APPELLANT;
            } else if (entity instanceof Appointee) {
                role = APPOINTEE;
            } else if (entity instanceof Representative) {
                role = REPRESENTATIVE;
            } else if (entity instanceof OtherParty) {
                role = OTHER_PARTY;
            }
        }
        return role;
    }

    public static String getPartyType(Entity entity) {
        return isNotBlank(entity.getOrganisation()) ? PartyType.ORG.name() : PartyType.IND.name();
    }

    public static List<UnavailabilityRange> getPartyUnavailabilityRange(HearingOptions hearingOptions) {
        if (nonNull(hearingOptions.getExcludeDates())) {
            // TODO Lucas - Check this is correct
            List<UnavailabilityRange> unavailabilityRanges = new ArrayList<>();
            for (ExcludeDate excludeDate : hearingOptions.getExcludeDates()) {
                DateRange dateRange = excludeDate.getValue();
                UnavailabilityRangeBuilder unavailabilityRange = UnavailabilityRange.builder();
                unavailabilityRange.unavailableFromDate(LocalDate.parse(dateRange.getStart()));
                unavailabilityRange.unavailableToDate(LocalDate.parse(dateRange.getEnd()));
                unavailabilityRanges.add(unavailabilityRange.build());
            }
            return unavailabilityRanges;
        } else {
            return null;
        }
    }

    public static List<UnavailabilityDayOfWeek> getPartyUnavailabilityDayOfWeek() {
        // Not used as of now
        // TODO Lucas - Double Check
        return null;
    }

    public static IndividualDetails getPartyIndividualDetails(Entity entity, HearingOptions hearingOptions, String hearingType, HearingSubtype hearingSubtype) {
        IndividualDetailsBuilder individualDetails = IndividualDetails.builder();
        individualDetails.title(getIndividualTitle(entity));
        individualDetails.firstName(getIndividualFirstName(entity));
        individualDetails.lastName(getIndividualLastName(entity));
        individualDetails.preferredHearingChannel(getIndividualPreferredHearingChannel(hearingType, hearingSubtype));
        individualDetails.interpreterLanguage(getIndividualInterpreterLanguage(hearingOptions));
        individualDetails.reasonableAdjustments(getIndividualReasonableAdjustments(hearingOptions));
        individualDetails.vulnerableFlag(getIndividualVulnerableFlag());
        individualDetails.vulnerabilityDetails(getIndividualVulnerabilityDetails());
        individualDetails.hearingChannelEmail(getIndividualHearingChannelEmail(entity));
        individualDetails.hearingChannelPhone(getIndividualHearingChannelPhone(entity));
        individualDetails.relatedParties(getIndividualRelatedParties(entity));
        return individualDetails.build();
    }




    public static String getIndividualTitle(Entity entity) {
        return entity.getName().getTitle();
    }

    public static String getIndividualFirstName(Entity entity) {
        return entity.getName().getFirstName();
    }

    public static String getIndividualLastName(Entity entity) {
        return entity.getName().getLastName();
    }

    public static String getIndividualPreferredHearingChannel(String hearingType, HearingSubtype hearingSubtype) {
        // TODO Depends on SSCS-10273 - Needs to implement for Reference data of valid Hearing Channel codes
        return null;
    }

    public static String getIndividualInterpreterLanguage(HearingOptions hearingOptions) {
        // TODO Depends on SSCS-10273 - Needs to implement for Reference data to convert from SSCS Languages/Sign Languages to Reference languages
        if (isYes(hearingOptions.getLanguageInterpreter())) {
            String signLanguageType = hearingOptions.getSignLanguageType();
            String languages = hearingOptions.getLanguages();
        }
        return null;
    }

    public static List<String> getIndividualReasonableAdjustments(HearingOptions hearingOptions) {
        List<String> hmcArrangements = new ArrayList<>();
        List<String> sscsArrangements = hearingOptions.getArrangements();
        // TODO Needs new ticket - Needs to implement for Reference data to convert from SSCS Arrangements to Reference Arrangements
        return hmcArrangements;
    }

    public static boolean getIndividualVulnerableFlag() {
        return false;
    }

    public static String getIndividualVulnerabilityDetails() {
        return null;
    }

    public static String getIndividualHearingChannelEmail(Entity entity) {
        if (nonNull(entity.getContact())) {
            return entity.getContact().getEmail();
        }
        return null;
    }

    public static String getIndividualHearingChannelPhone(Entity entity) {
        String phoneNumber = null;
        if (nonNull(entity.getContact())) {
            phoneNumber = entity.getContact().getMobile();
            if (isNull(phoneNumber)) {
                phoneNumber = entity.getContact().getPhone();
            }
        }
        return isNotBlank(phoneNumber) ? phoneNumber : null;
    }

    public static List<uk.gov.hmcts.reform.sscs.model.single.hearing.RelatedParty> getIndividualRelatedParties(Entity entity) {
        return entity.getRelatedParties().stream()
                .map(o -> uk.gov.hmcts.reform.sscs.model.single.hearing.RelatedParty.builder()
                        .relatedPartyID(o.getRelatedPartyId())
                        .relationshipType(o.getRelationshipType())
                        .build())
                .collect(Collectors.toList());

    }

    public static OrganisationDetails getPartyOrganisationDetails() {
        // TODO Future Work
        return null;
    }

    public static HearingDetails createHearingRequestDetails(HearingWrapper wrapper) {
        SscsCaseData caseData = wrapper.getOriginalCaseData();

        var requestDetailsBuilder = HearingDetails.builder();

        boolean autoListed = shouldBeAutoListed();

        requestDetailsBuilder.autolistFlag(autoListed);
        requestDetailsBuilder.hearingInWelshFlag(shouldBeHearingsInWelshFlag());

        requestDetailsBuilder.hearingIsLinkedFlag(isCaseLinked());
        requestDetailsBuilder.hearingType(getHearingType(caseData));  // Assuming key is what is required.
        requestDetailsBuilder.hearingWindow(buildHearingWindow(caseData, autoListed));
        requestDetailsBuilder.duration(getHearingDuration(caseData));
        requestDetailsBuilder.hearingPriorityType(getHearingPriority(caseData.getAdjournCaseCanCaseBeListedRightAway(), //Confirm this
            caseData.getUrgentCase()));
        requestDetailsBuilder.hearingLocations(getHearingLocations(caseData.getCaseManagementLocation()));
        requestDetailsBuilder.facilitiesRequired(getFacilitiesRequired(caseData));
        requestDetailsBuilder.listingComments(getListingComments(caseData.getAppeal(), caseData.getOtherParties()));
        requestDetailsBuilder.leadJudgeContractType(getLeadJudgeContractType());
        requestDetailsBuilder.panelRequirements(getPanelRequirements(caseData));
        //requestDetailsBuilder.numberOfPhysicalAttendees(); ---- TODO Implementation to be done by SSCS-10260
        //requestDetailsBuilder.hearingRequester(); ---- TODO Implementation to be done by SSCS-10260. Optional?
        //requestDetailsBuilder.leadJudgeContractType(); ---- TODO Implementation to be done by SSCS-10260

        return requestDetailsBuilder.build();
    }

    public static CaseDetails createHearingRequestCaseDetails(HearingWrapper wrapper) {
        SscsCaseData caseData = wrapper.getOriginalCaseData();
        String caseId = caseData.getCcdCaseId();
        CaseDetailsBuilder caseDetailsBuilder = CaseDetails.builder();

        caseDetailsBuilder.hmctsServiceCode(sscsServiceCode);
        caseDetailsBuilder.caseRef(caseId);
        caseDetailsBuilder.caseDeepLink(getCaseDeepLink(caseId));
        caseDetailsBuilder.hmctsInternalCaseName(caseData.getWorkAllocationFields().getCaseNameHmctsInternal()); // TODO Lucas - Check these - should they be tied to WA?
        caseDetailsBuilder.publicCaseName(caseData.getWorkAllocationFields().getCaseNamePublic());
        caseDetailsBuilder.caseAdditionalSecurityFlag(shouldBeAdditionalSecurityFlag(caseData));
        caseDetailsBuilder.caseInterpreterRequiredFlag(isInterpreterRequired(caseData.getAdjournCaseInterpreterRequired()));
        caseDetailsBuilder.caseCategories(buildCaseCategories(caseData));
        caseDetailsBuilder.caseManagementLocationCode(getCaseManagementLocationCode(caseData.getCaseManagementLocation()));
        caseDetailsBuilder.caseRestrictedFlag(shouldBeSensitiveFlag());
        caseDetailsBuilder.caseSlaStartDate(caseData.getCaseCreated());

        return caseDetailsBuilder.build();
    }

    public static List<CaseCategory> buildCaseCategories(SscsCaseData caseData) {
        List<CaseCategory> categories = new ArrayList<>();

        categories.add(CaseCategory.builder()
            .categoryType(CASE_TYPE)
            .categoryValue(caseData.getBenefitCode())
            .build());

        categories.add(CaseCategory.builder()
            .categoryType(CASE_SUB_TYPE)
            .categoryValue(caseData.getIssueCode())
            .build());

        return categories;
    }

    public static HearingWindow buildHearingWindow(SscsCaseData caseData, boolean autoListed) {
        // TODO Del - check this is correct and if any additional logic is needed
        LocalDate dateRangeStart = null;
        LocalDate dateRangeEnd = null;

        if (autoListed && nonNull(caseData.getEvents())) {
            Event dwpResponded = caseData.getEvents().stream()
                .filter(c -> EventType.DWP_RESPOND.equals(c.getValue().getEventType()))
                .findFirst().orElse(null);
            if (nonNull(dwpResponded) && nonNull(dwpResponded.getValue())) {
                dateRangeStart = dwpResponded.getValue().getDateTime().plusMonths(1).toLocalDate();
            }
        }

        return HearingWindow.builder()
            .firstDateTimeMustBe(getFirstDateTimeMustBe())
            .dateRangeStart(dateRangeStart)
            .dateRangeEnd(dateRangeEnd)
            .build();
    }

    public static LocalDateTime getFirstDateTimeMustBe() {
        // TODO Adjournments - Find out how to use adjournCase data to work this out, possibly related variables:
        //      adjournCaseNextHearingDateType, adjournCaseNextHearingDateOrPeriod, adjournCaseNextHearingDateOrTime,
        //      adjournCaseNextHearingFirstAvailableDateAfterDate, adjournCaseNextHearingFirstAvailableDateAfterPeriod
        // TODO Future Work for override
        return null;
    }

    public static boolean shouldBeAutoListed() {
        // TODO Future Work
        return true;
    }

    public static boolean shouldBeHearingsInWelshFlag() {
        // TODO Future Work
        return false;
    }

    public static boolean shouldBeAdditionalSecurityFlag(SscsCaseData caseData) {
        boolean isYes = false;
        // TODO To be done
        //      Check unacceptableCustomerBehaviour for Appellant, their Appointee and their Representatives
        //      Check unacceptableCustomerBehaviour for each OtherParty, their Appointee and their Representatives

        return isYes;
    }

    public static boolean shouldBeSensitiveFlag() {
        // TODO Future Work
        return false;
    }

    public static String getCaseDeepLink(String ccdCaseId) {
        // TODO Lucas - Confirm this is correct and create automated tests
        return String.format("%s/cases/case-details/%s", exUiUrl, ccdCaseId);
    }

    public static String getCaseManagementLocationCode(CaseManagementLocation caseManagementLocation) {
        // caseManagementLocation's baseLocation == epimsId
        // TODO Lucas - map from caseManagementLocation to epims


        return null;
    }

    public static List<String> getFacilitiesRequired(SscsCaseData caseData) {
        List<String> facilitiesRequired = new ArrayList<>();
        // TODO Dependant on SSCS-10116 - find out how to work this out and implement
        //          caseData.getAppeal().getHearingOptions().getArrangements()
        //          for each otherParty otherParty.getHearingOptions().getArrangements()
        return facilitiesRequired;
    }

    public static PanelRequirements getPanelRequirements(SscsCaseData caseData) {
        var panelRequirementsBuilder = PanelRequirements.builder();

        List<String> roleTypes = new ArrayList<>();
        // TODO Dependant on SSCS-10116 - Will be linked to Session Category Reference Data,
        //      find out what role types there are and how these are determined

        panelRequirementsBuilder.roleTypes(roleTypes);

        List<String> authorisationSubType = new ArrayList<>();
        // TODO Dependant on SSCS-10116 - Will be linked to Session Category Reference Data,
        //      find out what subtypes there are and how these are determined
        panelRequirementsBuilder.authorisationSubTypes(authorisationSubType);

        panelRequirementsBuilder.panelPreferences(getPanelPreferences(caseData));

        List<String> panelSpecialisms = new ArrayList<>();
        // TODO Dependant on SSCS-10273 - Will be linked to PanelMemberSpecialism, need to find out how this is worked out
        panelRequirementsBuilder.panelSpecialisms(panelSpecialisms);

        return panelRequirementsBuilder.build();
    }

    public static List<PanelPreference> getPanelPreferences(SscsCaseData caseData) {
        List<PanelPreference> panelPreferences = new ArrayList<>();
        // TODO Adjournments - loop to go through Judicial members that are need to be included or excluded
        // TODO Waqas - Check no other reason to have panel preferences
        //      Will need Judicial Staff Reference Data
        return panelPreferences;
    }

    public static String getLeadJudgeContractType() {
        // TODO Implementation to be done by SSCS-10260
        return null;
    }

    public static String getListingComments(Appeal appeal, List<CcdValue<OtherParty>> otherParties) {
        List<String> listingComments = new ArrayList<>();
        // TODO Lucas - Check this is all that is needed in this
        if (nonNull(appeal.getHearingOptions()) && isNotBlank(appeal.getHearingOptions().getOther())) {
            listingComments.add(appeal.getHearingOptions().getOther());
        }
        if (nonNull(otherParties) && !otherParties.isEmpty()) {
            listingComments.addAll(otherParties.stream()
                    .map(o -> o.getValue().getHearingOptions().getOther())
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList()));
        }

        return listingComments.isEmpty() ? null : String.join("\n", listingComments);
    }

    public static List<HearingLocations> getHearingLocations(CaseManagementLocation caseManagementLocation) {
        return Arrays.asList(HearingLocations.builder()
                                 .locationId(caseManagementLocation.getBaseLocation())
                                 .locationType(LocationType.COURT.getLocationLabel())
                                 .build());
    }

    public static String getHearingPriority(String isAdjournCase, String isUrgentCase) {
        // urgentCase Should go to top of queue in LA - also consider case created date
        // Flag to Lauren - how  can this be captured in HMC queue?
        // If there's an adjournment - date shouldn't reset - should also go to top priority

        String hearingPriorityType = NORMAL;

        // TODO Adjournment - Check what should be used to check if there is adjournment
        // TODO Dependant on SSCS-10273 - Needed for enum values and logic
        if (isYes(isUrgentCase) || isYes(isAdjournCase)) {
            hearingPriorityType = HIGH;
        }

        return hearingPriorityType;
    }

    public static int getHearingDuration(SscsCaseData caseData) {
        if (nonNull(caseData.getAdjournCaseNextHearingListingDuration())
                && Integer.parseInt(caseData.getAdjournCaseNextHearingListingDuration()) > 0) {
            // TODO Adjournments - Check this is the correct logic for Adjournments
            if ("sessions".equalsIgnoreCase(caseData.getAdjournCaseNextHearingListingDurationUnits())) {
                return Integer.parseInt(caseData.getAdjournCaseNextHearingListingDuration()) * 165;
            }
            if ("hours".equalsIgnoreCase(caseData.getAdjournCaseNextHearingListingDurationUnits())) {
                // TODO Adjournments - check no other measurement than hours, sessions and null
                return Integer.parseInt(caseData.getAdjournCaseNextHearingListingDuration()) * 60;
            }
        }
        if (nonNull(caseData.getBenefitCode()) && nonNull(caseData.getIssueCode())) {
            // TODO Dependant on SSCS-10116 - Will use Session Category Reference Data
            //      depends on session category, logic to be built (manual override needed)
            return 60;
        }
        return 30;
    }

    public static String getHearingType(SscsCaseData caseData) {
        String hearingType = null;
        // TODO Dependant on SSCS-10273 - find out what logic is needed here
        return hearingType;
    }

    public static boolean isInterpreterRequired(String adjournCaseInterpreterRequired) {
        boolean isYes = isYes(adjournCaseInterpreterRequired);
        // TODO Adjournment - Check this is the correct logic for Adjournment
        // TODO To be done
        return isYes;
    }

    public static boolean isCaseLinked() {
        // TODO Future work
        // boolean isYes = nonNull(caseData.getLinkedCase()) && !caseData.getLinkedCase().isEmpty();
        // driven by benefit or issue, can't be auto listed
        return false;
    }
}
