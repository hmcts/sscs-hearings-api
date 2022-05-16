package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.*;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.reference.data.mappings.HearingPriority.HIGH;
import static uk.gov.hmcts.reform.sscs.reference.data.mappings.HearingPriority.NORMAL;
import static uk.gov.hmcts.reform.sscs.reference.data.mappings.HearingTypeLov.SUBSTANTIVE;

@SuppressWarnings({"PMD.UnnecessaryLocalBeforeReturn","PMD.ReturnEmptyCollectionRatherThanNull"})
// TODO Unsuppress in future
public final class HearingsDetailsMapping {

    public static final int DURATION_SESSIONS_MULTIPLIER = 165;
    public static final int DURATION_HOURS_MULTIPLIER = 60;
    public static final int DURATION_DEFAULT = 30; // TODO find out default

    private HearingsDetailsMapping() {

    }

    public static HearingDetails buildHearingDetails(HearingWrapper wrapper) {
        SscsCaseData caseData = wrapper.getCaseData();

        HearingDetails.HearingDetailsBuilder hearingDetailsBuilder = HearingDetails.builder();

        boolean autoListed = shouldBeAutoListed();

        hearingDetailsBuilder.autolistFlag(autoListed);
        hearingDetailsBuilder.hearingType(getHearingType(caseData));
        hearingDetailsBuilder.hearingWindow(buildHearingWindow(caseData, autoListed));
        hearingDetailsBuilder.duration(getHearingDuration(caseData));
        hearingDetailsBuilder.nonStandardHearingDurationReasons(getNonStandardHearingDurationReasons());
        hearingDetailsBuilder.hearingPriorityType(getHearingPriority(caseData));
        hearingDetailsBuilder.numberOfPhysicalAttendees(getNumberOfPhysicalAttendees());
        hearingDetailsBuilder.hearingInWelshFlag(shouldBeHearingsInWelshFlag());
        hearingDetailsBuilder.hearingLocations(getHearingLocations(caseData.getCaseManagementLocation()));
        hearingDetailsBuilder.facilitiesRequired(getFacilitiesRequired(caseData));
        hearingDetailsBuilder.listingComments(getListingComments(caseData.getAppeal(), caseData.getOtherParties()));
        hearingDetailsBuilder.hearingRequester(getHearingRequester());
        hearingDetailsBuilder.privateHearingRequiredFlag(getPrivateHearingRequiredFlag());
        hearingDetailsBuilder.leadJudgeContractType(getLeadJudgeContractType());
        hearingDetailsBuilder.panelRequirements(getPanelRequirements(caseData));
        hearingDetailsBuilder.hearingIsLinkedFlag(isCaseLinked());
        hearingDetailsBuilder.amendReasonCode(getAmendReasonCode());

        return hearingDetailsBuilder.build();
    }

    public static boolean shouldBeAutoListed() {
        // TODO Future Work
        return true;
    }

    public static String getHearingType() {
        return SUBSTANTIVE.getHmcReference();
    }

    public static HearingWindow buildHearingWindow(SscsCaseData caseData, boolean autoListed) {
        LocalDate dateRangeStart = null;
        LocalDate dateRangeEnd = null;

        if (autoListed && nonNull(caseData.getEvents())) {
            Event dwpResponded = caseData.getEvents().stream()
                    .filter(c -> EventType.DWP_RESPOND.equals(c.getValue().getEventType()))
                    .findFirst().orElse(null);
            if (nonNull(dwpResponded) && isNotBlank(dwpResponded.getValue().getDate())) {
                dateRangeStart = isYes(caseData.getUrgentCase())
                        ? dwpResponded.getValue().getDateTime().plusDays(14).toLocalDate()
                        : dwpResponded.getValue().getDateTime().plusMonths(28).toLocalDate();
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
        // TODO Future Work - Manual Override
        return null;
    }

    public static int getHearingDuration(SscsCaseData caseData) {
        // TODO Adjournments - Check this is the correct logic for Adjournments
        // TODO Future Work - Manual Override
        // TODO Dependant on SSCS-10116 - Will use Session Category Reference Data

        if (isNotBlank(caseData.getAdjournCaseNextHearingListingDuration())
                && caseData.getAdjournCaseNextHearingListingDuration().matches("\\d+")
                && Integer.parseInt(caseData.getAdjournCaseNextHearingListingDuration()) > 0) {

            if ("sessions".equalsIgnoreCase(caseData.getAdjournCaseNextHearingListingDurationUnits())) {
                return Integer.parseInt(caseData.getAdjournCaseNextHearingListingDuration()) * DURATION_SESSIONS_MULTIPLIER;
            }
            if ("hours".equalsIgnoreCase(caseData.getAdjournCaseNextHearingListingDurationUnits())) {
                // TODO Adjournments - check no other measurement than hours, sessions and null
                return Integer.parseInt(caseData.getAdjournCaseNextHearingListingDuration()) * DURATION_HOURS_MULTIPLIER;
            }
        }
        if (nonNull(caseData.getBenefitCode()) && nonNull(caseData.getIssueCode())) {
            // TODO Dependant on SSCS-10116 - Will use Session Category Reference Data
            return 45;
        }
        return DURATION_DEFAULT;
    }

    public static List<String> getNonStandardHearingDurationReasons() {
        // TODO Future Work
        return null;
    }

    public static String getHearingPriority(SscsCaseData caseData) {
        // urgentCase Should go to top of queue in LA - also consider case created date
        // Flag to Lauren - how  can this be captured in HMC queue?
        // If there's an adjournment - date shouldn't reset - should also go to top priority

        // TODO Adjournment - Check what should be used to check if there is adjournment
        if (isYes(caseData.getUrgentCase()) || isYes(caseData.getAdjournCasePanelMembersExcluded())) {
            return HIGH.getHmcReference();
        }
        return NORMAL.getHmcReference();
    }

    public static Number getNumberOfPhysicalAttendees() {
        // TODO Implementation to be done by SSCS-10260
        return null;
    }

    public static boolean shouldBeHearingsInWelshFlag() {
        // TODO Future Work
        return false;
    }

    public static List<HearingLocations> getHearingLocations(CaseManagementLocation caseManagementLocation) {
        // locationType - from reference data - processing venue to venue type/epims
        // locationId - epims
        // manual over-ride e.g. if a judge wants to change venue
        // if paper case - display all venues in that region
        // locations where there is more than one venue
        // Normally one location, but can be two in some cities.
        // TODO Implementation to be done by SSCS-10245 - work out what venues to choose and get epims/locationType info from Reference Data
        return new ArrayList<>();
    }

    public static List<String> getFacilitiesRequired(SscsCaseData caseData) {
        List<String> facilitiesRequired = new ArrayList<>();
        // TODO Dependant on SSCS-10116 - find out how to work this out and implement
        //          caseData.getAppeal().getHearingOptions().getArrangements()
        //          for each otherParty otherParty.getHearingOptions().getArrangements()
        return facilitiesRequired;
    }

    public static String getListingComments(Appeal appeal, List<CcdValue<OtherParty>> otherParties) {
        List<String> listingComments = new ArrayList<>();
        if (nonNull(appeal.getHearingOptions()) && isNotBlank(appeal.getHearingOptions().getOther())) {
            listingComments.add(getComment(appeal.getAppellant(), appeal.getHearingOptions().getOther()));
        }
        if (nonNull(otherParties) && !otherParties.isEmpty()) {
            listingComments.addAll(otherParties.stream()
                    .map(CcdValue::getValue)
                    .filter(o -> isNotBlank(o.getHearingOptions().getOther()))
                    .map(o -> getComment(o, o.getHearingOptions().getOther()))
                    .collect(Collectors.toList()));
        }

        return listingComments.isEmpty() ? null : String.join(String.format("%n%n"), listingComments);
    }

    public static String getComment(Party party, String comment) {
        return String.format("%s%n%s", getCommentSubheader(party), comment);
    }

    public static String getCommentSubheader(Party party) {
        return String.format("%s - %s:", getPartyRole(party), getEntityName(party));
    }

    public static String getPartyRole(Party party) {
        return nonNull(party.getRole()) && isNotBlank(party.getRole().getName())
                ? party.getRole().getName()
                : HearingsMapping.getEntityRoleCode(party).getValueEn();
    }

    public static String getEntityName(Entity entity) {
        return  entity.getName().getFullName();
    }

    public static String getHearingRequester() {
        // TODO Implementation to be done by SSCS-10260. Optional?
        return null;
    }

    private static Boolean getPrivateHearingRequiredFlag() {
        // TODO Future Work
        return null;
    }

    public static String getLeadJudgeContractType() {
        // TODO Implementation to be done by SSCS-10260
        return null;
    }

    public static PanelRequirements getPanelRequirements(SscsCaseData caseData) {
        var panelRequirementsBuilder = PanelRequirements.builder();

        List<String> roleTypes = new ArrayList<>();
        // TODO Dependant on SSCS-10116 - Will be linked to Session Category Reference Data,
        //      find out what role types there are and how these are determined

        panelRequirementsBuilder.roleTypes(roleTypes);

        List<String> authorisationTypes = new ArrayList<>();
        // TODO Dependant on SSCS-10116 - Will be linked to Session Category Reference Data,
        //      find out what types there are and how these are determined
        panelRequirementsBuilder.authorisationTypes(authorisationTypes);


        List<String> authorisationSubTypes = new ArrayList<>();
        // TODO Dependant on SSCS-10116 - Will be linked to Session Category Reference Data,
        //      find out what subtypes there are and how these are determined
        panelRequirementsBuilder.authorisationSubTypes(authorisationSubTypes);

        panelRequirementsBuilder.panelPreferences(getPanelPreferences(caseData));

        List<String> panelSpecialisms = new ArrayList<>();
        // TODO Dependant on SSCS-10116 - Will be linked to PanelMemberSpecialism, need to find out how this is worked out
        panelRequirementsBuilder.panelSpecialisms(panelSpecialisms);

        return panelRequirementsBuilder.build();
    }

    public static List<PanelPreference> getPanelPreferences(SscsCaseData caseData) {
        List<PanelPreference> panelPreferences = new ArrayList<>();
        // TODO Adjournments - loop to go through Judicial members that are need to be included or excluded
        // TODO Potentially used with Manual overrides
        //      Will need Judicial Staff Reference Data
        return panelPreferences;
    }

    public static boolean isCaseLinked() {
        // TODO Future work
        // boolean isYes = nonNull(caseData.getLinkedCase()) && !caseData.getLinkedCase().isEmpty();
        // driven by benefit or issue, can't be auto listed
        return false;
    }

    private static String getAmendReasonCode() {
        // TODO Future Work
        return null;
    }
}
