package uk.gov.hmcts.reform.sscs.helper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.*;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HmcHearingDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HmcHearingRequestPayload.HmcHearingRequestPayloadBuilder;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HmcRequestDetails.HmcRequestDetailsBuilder;
import uk.gov.hmcts.reform.sscs.model.single.hearing.IndividualDetails.IndividualDetailsBuilder;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PanelPreference;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PanelRequirements;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PartyDetails.PartyDetailsBuilder;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RelatedParty;
import uk.gov.hmcts.reform.sscs.model.single.hearing.UnavailabilityRange.UnavailabilityRangeBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.*;

@SuppressWarnings({"PMD.LinguisticNaming","PMD.UnnecessaryLocalBeforeReturn"})
// TODO Unsuppress in future
public final class HearingsMapping {

    public static final String CASE_TYPE = "caseType";
    public static final String CASE_SUB_TYPE = "caseSubType";
    @Value("${exui.url}")
    private static String exUiUrl;

    @Value("${exui.url}")
    private static String sscsServiceCode;

    private HearingsMapping() {

    }

    public static void updateFlags(HearingWrapper wrapper) {
        SscsCaseData caseData = wrapper.getUpdatedCaseData();
        caseData.setAutoListFlag(shouldBeAutoListed(caseData.getAutoListFlag()));
        caseData.setHearingsInWelshFlag(shouldBeHearingsInWelshFlag(caseData.getHearingsInWelshFlag()));
        caseData.setAdditionalSecurityFlag(shouldBeAdditionalSecurityFlag(caseData.getAdditionalSecurityFlag()));
        caseData.setSensitiveFlag(shouldBeSensitiveFlag(caseData.getSensitiveFlag()));
    }

    public static void updateIds(HearingWrapper wrapper) {
        SscsCaseData caseData = wrapper.getUpdatedCaseData();
        Appeal appeal = caseData.getAppeal();
        Appellant appellant = appeal.getAppellant();
        Integer maxId = getMaxId(caseData.getOtherParties(), appellant);
        updateEntityId(appellant, maxId);
        if (nonNull(appellant.getAppointee())){
            updateEntityId(appellant.getAppointee(), maxId);
        }
        if (nonNull(appeal.getRep())){
            updateEntityId(appellant.getRep(), maxId);
        }
        for (CcdValue<OtherParty> otherPartyCcdValue: caseData.getOtherParties()) {
            OtherParty otherParty = otherPartyCcdValue.getValue();
            updateEntityId(otherParty, maxId);
            if (nonNull(otherParty.getAppointee())){
                updateEntityId(otherParty.getAppointee(), maxId);
            }
            if (nonNull(otherParty.getRep())){
                updateEntityId(otherParty.getRep(), maxId);
            }
        }
    }

    private static void updateEntityId(Entity entity, Integer maxId) {
        String id = entity.getId();
        if (isNotBlank(id)) {
            id = String.valueOf(++maxId);
        }
        entity.setId(id);
    }

    private static int getMaxId(List<CcdValue<OtherParty>> otherParties, Party appellant) {
        List<Integer> currentIds = new ArrayList<>();

        for (CcdValue<OtherParty> ccdOtherParty : otherParties) {
            currentIds.addAll(getMaxId(ccdOtherParty.getValue()));
        }
        currentIds.addAll(getMaxId(appellant));

        return currentIds.stream().max(Comparator.naturalOrder()).orElse(0);
    }

    private static List<Integer> getMaxId(Party party) {
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

    public static HmcHearingRequestPayload buildCreateHearingPayload(HearingWrapper wrapper) {
        //Party details required-----------
        HmcHearingRequestPayloadBuilder requestPayloadBuilder = HmcHearingRequestPayload.builder();


        requestPayloadBuilder.hmcRequestDetails(createHmcRequestDetails(wrapper));
        requestPayloadBuilder.hmcHearingDetails(createHmcHearingRequestDetails(wrapper));
        requestPayloadBuilder.hmcHearingCaseDetails(createHmcHearingRequestCaseDetails(wrapper));
        requestPayloadBuilder.partiesDetails(createHmcHearingPartiesDetails(wrapper));
        return requestPayloadBuilder.build();
    }

    private static HmcRequestDetails createHmcRequestDetails(HearingWrapper wrapper) {
        HmcRequestDetailsBuilder hmcRequestDetailsBuilder = HmcRequestDetails.builder();
        hmcRequestDetailsBuilder.versionNumber(wrapper.getSchedulingAndListingFields.version());
        return hmcRequestDetailsBuilder.build();
    }

    private static List<PartyDetails> createHmcHearingPartiesDetails(HearingWrapper wrapper) {
        List<PartyDetails> partiesDetails = new ArrayList<>();

        List<CcdValue<OtherParty>> otherParties = wrapper.getUpdatedCaseData().getOtherParties();
        Appeal appeal = wrapper.getUpdatedCaseData().getAppeal();
        Appellant appellant = appeal.getAppellant();

        partiesDetails.add(createHmcHearingPartyDetails(appellant, appeal.getHearingOptions(), appeal.getHearingType(), appeal.getHearingSubtype()));
        if (isYes(appellant.getIsAppointee()) && nonNull(appellant.getAppointee())) {
            partiesDetails.add(createHmcHearingPartyDetails(appellant.getAppointee(), appeal.getHearingOptions(), appeal.getHearingType(), appeal.getHearingSubtype()));
        }
        if (nonNull(appeal.getRep()) && isYes(appeal.getRep().getHasRepresentative()) && nonNull(appellant.getRep())) {
            partiesDetails.add(createHmcHearingPartyDetails(appellant.getRep(),appeal.getHearingOptions(), appeal.getHearingType(), appeal.getHearingSubtype()));
        }

        for (CcdValue<OtherParty> ccdOtherParty : otherParties) {
            OtherParty otherParty = ccdOtherParty.getValue();
            partiesDetails.add(createHmcHearingPartyDetails(otherParty, otherParty.getHearingOptions(), null, otherParty.getHearingSubtype()));
            if (otherParty.hasAppointee() && nonNull(otherParty.getAppointee())) {
                partiesDetails.add(createHmcHearingPartyDetails(otherParty.getAppointee(), otherParty.getHearingOptions(), null, otherParty.getHearingSubtype()));
            }
            if (otherParty.hasRepresentative() && nonNull(otherParty.getRep())) {
                partiesDetails.add(createHmcHearingPartyDetails(otherParty.getRep(), otherParty.getHearingOptions(), null, otherParty.getHearingSubtype()));
            }
        }
        return partiesDetails;
    }

    private static PartyDetails createHmcHearingPartyDetails(Entity entity, HearingOptions hearingOptions, String hearingType, HearingSubtype hearingSubtype) {
        PartyDetailsBuilder partyDetails = PartyDetails.builder();

        PartyType partyType = getPartyType(entity);
        String partyRole = getPartyRole(entity);

        partyDetails.partyID(getPartyId(entity));
        partyDetails.partyType(partyType.getPartyLabel());
        partyDetails.partyRole(partyRole);
        if (PartyType.IND.equals(partyType)) {
            partyDetails.individualDetails(getPartyIndividualDetails(entity, partyRole, hearingOptions, hearingType, hearingSubtype));
        } else if (PartyType.ORG.equals(partyType)) {
            partyDetails.organisationDetails(getPartyOrganisationDetails(entity));
        }
        partyDetails.unavailabilityDayOfWeek(getPartyUnavailabilityDayOfWeek());
        partyDetails.unavailabilityRanges(getPartyUnavailabilityRange(hearingOptions));

        return partyDetails.build();
    }

    private static String getPartyId(Entity entity) {
        return entity.getId();
    }


    private static String getPartyRole(Entity entity) {
        String role = "";
        if (nonNull(entity.getRole())) {
            role = entity.getRole().getName();
        } else {
            if (entity instanceof Appellant) {
                role = "Appellant";
            } else if (entity instanceof Appointee) {
                role = "Appointee";
            } else if (entity instanceof Representative) {
                role = "Representative";
            } else if (entity instanceof OtherParty) {
                role = "OtherParty";
            }
        }
        return role;
    }

    private static PartyType getPartyType(Entity entity) {
        // TODO Talk to Andrew about this
        return null;
    }

    private static List<UnavailabilityRange> getPartyUnavailabilityRange(HearingOptions hearingOptions) {
        if (nonNull(hearingOptions.getExcludeDates())) {
            // TODO Check this is correct
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

    private static List<UnavailabilityDayOfWeek> getPartyUnavailabilityDayOfWeek() {
        // Not used as of now
        // TODO Double Check
        return null;
    }

    private static IndividualDetails getPartyIndividualDetails(Entity entity, String partyRole, HearingOptions hearingOptions, String hearingType, HearingSubtype hearingSubtype) {
        IndividualDetailsBuilder individualDetails = IndividualDetails.builder();
        individualDetails.title(getIndividualTitle(entity));
        individualDetails.firstName(getIndividualFirstName(entity));
        individualDetails.lastName(getIndividualLastName(entity));
        individualDetails.preferredHearingChannel(getIndividualPreferredHearingChannel(hearingSubtype));
        individualDetails.interpreterLanguage(getIndividualInterpreterLanguage(hearingOptions));
        individualDetails.reasonableAdjustments(getIndividualReasonableAdjustments(hearingOptions));
        individualDetails.vulnerableFlag(getIndividualVulnerableFlag(entity));
        individualDetails.vulnerabilityDetails(getIndividualVulnerabilityDetails(entity));
        individualDetails.hearingChannelEmail(getIndividualHearingChannelEmail(entity));
        individualDetails.hearingChannelPhone(getIndividualHearingChannelPhone(entity));
        individualDetails.relatedParties(getIndividualRelatedParties(entity, partyRole));
        return individualDetails.build();
    }


    private static String getIndividualTitle(Entity entity) {
        return entity.getName().getTitle();
    }

    private static String getIndividualFirstName(Entity entity) {
        return entity.getName().getFirstName();
    }

    private static String getIndividualLastName(Entity entity) {
        return entity.getName().getLastName();
    }

    private static String getIndividualPreferredHearingChannel(HearingSubtype hearingSubtype) {
        // TODO Needs to implement for Reference data of valid Hearing Channel codes
        return null;
    }

    private static String getIndividualInterpreterLanguage(HearingOptions hearingOptions) {
        // TODO Needs to implement for Reference data to convert from SSCS Languages/Sign Languages to Reference languages
        if (isYes(hearingOptions.getLanguageInterpreter())) {
            String signLanguageType = hearingOptions.getSignLanguageType();
            String languages = hearingOptions.getLanguages();
        }
        return null;
    }

    private static List<String> getIndividualReasonableAdjustments(HearingOptions hearingOptions) {
        List<String> hmcArrangements = new ArrayList<>();
        List<String> sscsArrangements = hearingOptions.getArrangements();
        // TODO Needs to implement for Reference data to convert from SSCS Arrangements to Reference Arrangements
        return hmcArrangements;
    }

    private static boolean getIndividualVulnerableFlag(Entity entity) {
        // TODO where to get this from?
        return isYes(entity.getVulnerableFlag());
    }

    private static String getIndividualVulnerabilityDetails(Entity entity) {
        // TODO where to get this from?
        return entity.getVulnerabilityDetails();
    }

    private static String getIndividualHearingChannelEmail(Entity entity) {
        if (nonNull(entity.getContact())) {
            return entity.getContact().getEmail();
        }
        return null;
    }

    private static String getIndividualHearingChannelPhone(Entity entity) {
        String phoneNumber = null;
        if (nonNull(entity.getContact())) {
            phoneNumber = entity.getContact().getMobile();
            if (isNull(phoneNumber)) {
                phoneNumber = entity.getContact().getPhone();
            }
        }
        return isNotBlank(phoneNumber) ? phoneNumber : null;
    }

    private static List<RelatedParty> getIndividualRelatedParties(Entity entity, String partyRole) {
        List<RelatedParty> RelatedParties = new ArrayList<>();
        // TODO ref data that hasn't been published yet

        RelatedParties.add(RelatedParty.builder()
                .relatedPartyID("")
                .relationshipType(partyRole)
                .build());

        return RelatedParties;
    }

    public static HmcCaseDetails createHmcCaseDetails(HearingWrapper wrapper) {
        SscsCaseData caseData = wrapper.getUpdatedCaseData();

        return HmcCaseDetails.builder()
                .caseDeepLink(getCaseDeepLink(caseData.getCcdCaseId()))
                .caseManagementLocationCode(getCaseManagementLocationCode(caseData.getCaseManagementLocation()))
                .build();
    }

    private static OrganisationDetails getPartyOrganisationDetails(Entity entity) {
        // TODO
        return null;
    }

    // TODO Lots todo in this mapping below -------------
    // TODO Check whether some of these fields need further mapping - SSCS-10273
    public static HmcHearingDetails createHmcHearingRequestDetails(HearingWrapper wrapper) {
        SscsCaseData caseData = wrapper.getOriginalCaseData();

        //Sensitivity flag? TODO Removed from prev implementation
        var requestDetailsBuilder = HmcHearingDetails.builder();

        requestDetailsBuilder.autolistFlag(isYes(caseData.getAutoListFlag()));
        requestDetailsBuilder.hearingInWelshFlag(isYes(caseData.getHearingsInWelshFlag()));
        requestDetailsBuilder.hearingIsLinkedFlag(isYes(isCaseLinked(caseData)));
        requestDetailsBuilder.hearingType(getHearingType(caseData));  // Assuming key is what is required.
        requestDetailsBuilder.hearingWindow(buildHearingWindow(caseData));
        requestDetailsBuilder.duration(getHearingDuration(caseData));
        requestDetailsBuilder.hearingPriorityType(getHearingPriority(caseData.getAdjournCaseCanCaseBeListedRightAway(), //Confirm this
            caseData.getUrgentCase()));
        requestDetailsBuilder.hmcHearingLocations(getHearingLocations(caseData.getCaseManagementLocation()));
        requestDetailsBuilder.facilitiesRequired(getFacilitiesRequired(caseData));
        requestDetailsBuilder.listingComments(getListingComments(caseData.getAppeal(), caseData.getOtherParties()));
        requestDetailsBuilder.leadJudgeContractType(getLeadJudgeContractType(caseData));
        requestDetailsBuilder.panelRequirements(getPanelRequirements(caseData));
        //requestDetailsBuilder.numberOfPhysicalAttendees(); ---- TODO Get from Gorkem's PR
        //requestDetailsBuilder.hearingRequester(); ---- TODO Get from Gorkem's PR. Optional?
        //requestDetailsBuilder.leadJudgeContractType(); ---- TODO Get from Gorkem's PR

        return requestDetailsBuilder.build();
    }

    public static HmcHearingCaseDetails createHmcHearingRequestCaseDetails(HearingWrapper wrapper) {
        SscsCaseData caseData = wrapper.getOriginalCaseData();
        String caseId = caseData.getCcdCaseId();
        var requestCaseDetailsBuilder = HmcHearingCaseDetails.builder();

        requestCaseDetailsBuilder.hmctsServiceCode(sscsServiceCode);
        requestCaseDetailsBuilder.caseRef(caseId);
        requestCaseDetailsBuilder.caseDeepLink(getCaseDeepLink(caseId));
        requestCaseDetailsBuilder.hmctsInternalCaseName(caseData.getWorkAllocationFields().getCaseNameHmctsInternal()); // TODO Check these - should they be tied to WA?
        requestCaseDetailsBuilder.publicCaseName(caseData.getWorkAllocationFields().getCaseNamePublic());
        requestCaseDetailsBuilder.caseAdditionalSecurityFlag(isYes(caseData.getAdditionalSecurityFlag()));
        requestCaseDetailsBuilder.caseInterpreterRequiredFlag(isYes(isInterpreterRequired(
            caseData.getAdjournCaseInterpreterRequired())));
        requestCaseDetailsBuilder.caseCategories(buildCaseCategories(caseData));
        requestCaseDetailsBuilder.caseManagementLocationCode(getCaseManagementLocationCode(caseData.getCaseManagementLocation()));
        requestCaseDetailsBuilder.caseRestrictedFlag(isYes(caseData.getSensitiveFlag())); // TODO Check, Spreadsheet seems to say this should go here.
        requestCaseDetailsBuilder.caseSlaStartDate(caseData.getCaseCreated());

        return requestCaseDetailsBuilder.build();
    }

    private static List<CaseCategory> buildCaseCategories(SscsCaseData caseData) {
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

    private static HearingWindow buildHearingWindow(SscsCaseData caseData) {
        // TODO check this is correct and if any additional logic is needed
        LocalDate dateRangeStart = null;
        LocalDate dateRangeEnd = null;;

        if (isYes(caseData.getAutoListFlag()) && nonNull(caseData.getEvents())) {
            Event dwpResponded = caseData.getEvents().stream()
                .filter(c -> EventType.DWP_RESPOND.equals(c.getValue().getEventType()))
                .findFirst().orElse(null);
            if (nonNull(dwpResponded) && nonNull(dwpResponded.getValue())) {
                dateRangeStart = dwpResponded.getValue().getDateTime().plusMonths(1).toLocalDate();
            }
        }

        return HearingWindow.builder()
            .firstDateTimeMustBe(getFirstDateTimeMustBe(caseData))
            .dateRangeStart(dateRangeStart)
            .dateRangeEnd(dateRangeEnd)
            .build();

    }



    public static YesNo shouldBeAutoListed(YesNo autoListFlag) {
        boolean isYes = isYes(autoListFlag);
        // TODO add auto listing reasons,
        //      YesNo.isNoOrNull(isCaseLinked)
        return isYes ? YES : NO;
    }

    public static YesNo shouldBeHearingsInWelshFlag(YesNo hearingsInWelshFlag) {
        boolean isYes = isYes(hearingsInWelshFlag);
        return isYes ? YES : NO;
    }

    public static YesNo shouldBeAdditionalSecurityFlag(YesNo additionalSecurityFlag) {
        boolean isYes = isYes(additionalSecurityFlag);
        // TODO Check unacceptableCustomerBehaviour for Appellant, their Appointee and their Representatives
        //      Check unacceptableCustomerBehaviour for each OtherParty, their Appointee and their Representatives
        //      Any YES then YES
        return isYes ? YES : NO;
    }

    public static YesNo shouldBeSensitiveFlag(YesNo sensitiveFlag) {
        boolean isYes = isYes(sensitiveFlag);
        // TODO check no other factors
        return isYes ? YES : NO;
    }

    public static String getCaseDeepLink(String ccdCaseId) {
        // TODO Confirm this is correct and create automated tests
        return String.format("%s/cases/case-details/%s", exUiUrl, ccdCaseId);
    }

    public static String getCaseManagementLocationCode(CaseManagementLocation caseManagementLocation) {
        // TODO find out how to get epims for this using caseManagementLocation
        return null;
    }

    public static List<String> getFacilitiesRequired(SscsCaseData caseData) {
        List<String> facilitiesRequired = new ArrayList<>();
        // TODO find out how to work this out and implement
        return facilitiesRequired;
    }

    public static LocalDateTime getFirstDateTimeMustBe(SscsCaseData caseData) {
        LocalDateTime firstDateTimeMustBe = null;
        // TODO Find out how to use adjournCase data to work this out, possibly related variables:
        //      adjournCaseNextHearingDateType, adjournCaseNextHearingDateOrPeriod, adjournCaseNextHearingDateOrTime,
        //      adjournCaseNextHearingFirstAvailableDateAfterDate, adjournCaseNextHearingFirstAvailableDateAfterPeriod
        return firstDateTimeMustBe;
    }

    public static PanelRequirements getPanelRequirements(SscsCaseData caseData) {
        var panelRequirementsBuilder = PanelRequirements.builder();

        List<String> roleTypes = new ArrayList<>();
        // TODO Will be linked to Session Category Reference Data,
        //      find out what role types there are and how these are determined

        panelRequirementsBuilder.roleTypes(roleTypes);

        List<String> authorisationSubType = new ArrayList<>();
        // TODO Will be linked to Session Category Reference Data,
        //      find out what subtypes there are and how these are determined
        panelRequirementsBuilder.authorisationSubTypes(authorisationSubType);

        panelRequirementsBuilder.panelPreferences(getPanelPreferences(caseData));

        List<String> panelSpecialisms = new ArrayList<>();
        // TODO find out what specialisms there are and how these are determined
        panelRequirementsBuilder.panelSpecialisms(panelSpecialisms);

        return panelRequirementsBuilder.build();
    }

    public static List<PanelPreference> getPanelPreferences(SscsCaseData caseData) {
        List<PanelPreference> panelPreferences = new ArrayList<>();
        // TODO loop to go through Judicial members that are need to be included or excluded
        //      Will need Judicial Staff Reference Data
        return panelPreferences;
    }

    public static String getLeadJudgeContractType(SscsCaseData caseData) {
        // TODO find out what types there are and how this is determined
        return null;
    }

    public static String getListingComments(Appeal appeal, List<CcdValue<OtherParty>> otherParties) {
        List<String> listingComments = new ArrayList<>();
        // TODO Check this is all that is needed in this
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

    public static List<HmcHearingLocation> getHearingLocations(CaseManagementLocation caseManagementLocation) {
        // locationType - from reference data - processing venue to venue type/epims
        // locationId - epims
        // manual over-ride e.g. if a judge wants to change venue
        // if paper case - display all venues in that region
        // locations where there is more than one venue
        // Normally one location, but can be two in some cities.
        // TODO work out what venues to choose and get epims/locationType info from Reference Data
        return new ArrayList<>();
    }

    public static String getHearingPriority(String isAdjournCase, String isUrgentCase) {
        // urgentCase Should go to top of queue in LA - also consider case created date
        // Flag to Lauren - how  can this be captured in HMC queue?
        // If there's an adjournment - date shouldn't reset - should also go to top priority

        String hearingPriorityType = "Normal";

        // TODO Adjournment - Check what should be used to check if there is adjournment
        if (isYes(isUrgentCase) || isYes(isAdjournCase)) {
            hearingPriorityType = "High";
        }

        return hearingPriorityType;
    }

    public static int getHearingDuration(SscsCaseData caseData) {
        int duration = 30;
        if (nonNull(caseData.getAdjournCaseNextHearingListingDuration())
                && Integer.parseInt(caseData.getAdjournCaseNextHearingListingDuration()) > 0) {
            // TODO Adjournments - Check this is the correct logic for Adjournments
            if ("hours".equalsIgnoreCase(caseData.getAdjournCaseNextHearingListingDurationUnits())) {
                duration = Integer.parseInt(caseData.getAdjournCaseNextHearingListingDuration()) * 60;
            } else {
                // TODO Adjournments - check no other measurement than hours, mins and null
                duration = Integer.parseInt(caseData.getAdjournCaseNextHearingListingDuration());
            }
        } else if (nonNull(caseData.getBenefitCode()) && nonNull(caseData.getIssueCode())) {
            // TODO Will use Session Category Reference Data
            //      depends on session category, logic to be built (manual override needed)
            duration = 60;
        }
        return duration;
    }

    public static String getHearingType(SscsCaseData caseData) {
        String hearingType = null;
        // TODO find out what logic is needed here
        return hearingType;
    }

    public static YesNo isInterpreterRequired(String adjournCaseInterpreterRequired) {
        boolean isYes = isYes(adjournCaseInterpreterRequired);
        // TODO Adjournment - Check this is the correct logic for Adjournment
        // TODO Implement checks for Appeal HearingOptions
        return isYes ? YES : NO;
    }

    public static YesNo isCaseLinked(SscsCaseData caseData) {
        boolean isYes = nonNull(caseData.getLinkedCase()) && !caseData.getLinkedCase().isEmpty();
        // driven by benefit or issue, can't be auto listed
        // TODO find out what is required and implement
        return isYes ? YES : NO;
    }
}
