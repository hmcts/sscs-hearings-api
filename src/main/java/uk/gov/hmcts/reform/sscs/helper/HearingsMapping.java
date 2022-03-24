package uk.gov.hmcts.reform.sscs.helper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseManagementLocation;
import uk.gov.hmcts.reform.sscs.ccd.domain.CcdValue;
import uk.gov.hmcts.reform.sscs.ccd.domain.Event;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingPriorityType;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingWindowRange;
import uk.gov.hmcts.reform.sscs.ccd.domain.HmcCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.OtherParty;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsHearingType;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.CaseCategory;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingWindow;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HmcHearingLocation;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HmcHearingRequestCaseDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HmcHearingRequestDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PanelPreference;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PanelRequirements;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.NO;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.YES;

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

    public static HmcCaseDetails createHmcCaseDetails(HearingWrapper wrapper) {
        SscsCaseData caseData = wrapper.getUpdatedCaseData();

        return HmcCaseDetails.builder()
                .caseDeepLink(getCaseDeepLink(caseData.getCcdCaseId()))
                .caseManagementLocationCode(getCaseManagementLocationCode(caseData.getCaseManagementLocation()))
                .build();
    }

    //Lots todo in this mapping below -------------
    //Check whether some of these fields need further mapping - SSCS-10273
    public static HmcHearingRequestDetails createHmcHearingRequestDetails(HearingWrapper wrapper) {
        SscsCaseData caseData = wrapper.getOriginalCaseData();

        //Sensitivity flag? Removed from prev implementation
        var requestDetailsBuilder = HmcHearingRequestDetails.builder();

        requestDetailsBuilder.autolistFlag(caseData.getAutoListFlag().toBoolean());
        requestDetailsBuilder.hearingInWelshFlag(caseData.getHearingsInWelshFlag().toBoolean());
        requestDetailsBuilder.hearingIsLinkedFlag(isCaseLinked(caseData).toBoolean());
        requestDetailsBuilder.hearingType(getHearingType(caseData).getKey());  // Assuming key is what is required.
        requestDetailsBuilder.hearingWindow(buildHearingWindow(caseData));
        requestDetailsBuilder.duration(getHearingDuration(caseData));
        requestDetailsBuilder.hearingPriorityType(getHearingPriority(caseData.getAdjournCaseCanCaseBeListedRightAway(), //Confirm this
            caseData.getUrgentCase()).getType());
        requestDetailsBuilder.hmcHearingLocations(getHearingLocations(caseData.getCaseManagementLocation()));
        requestDetailsBuilder.facilitiesRequired(getFacilitiesRequired(caseData));
        requestDetailsBuilder.listingComments(getListingComments(caseData.getAppeal(), caseData.getOtherParties()));
        requestDetailsBuilder.leadJudgeContractType(getLeadJudgeContractType(caseData));
        requestDetailsBuilder.panelRequirements(getPanelRequirements(caseData));
        //requestDetailsBuilder.numberOfPhysicalAttendees(); ----Get from Gorkem's PR
        //requestDetailsBuilder.hearingRequester(); ----Get from Gorkem's PR. Optional?
        //requestDetailsBuilder.leadJudgeContractType(); ----Get from Gorkem's PR

        return requestDetailsBuilder.build();
    }

    public static HmcHearingRequestCaseDetails createHmcHearingRequestCaseDetails(HearingWrapper wrapper) {
        SscsCaseData caseData = wrapper.getOriginalCaseData();
        String caseId = caseData.getCcdCaseId();
        var requestCaseDetailsBuilder = HmcHearingRequestCaseDetails.builder();

        requestCaseDetailsBuilder.hmctsServiceCode(sscsServiceCode);
        requestCaseDetailsBuilder.caseRef(caseId);
        requestCaseDetailsBuilder.caseDeepLink(getCaseDeepLink(caseId));
        requestCaseDetailsBuilder.hmctsInternalCaseName(caseData.getWorkAllocationFields().getCaseNameHmctsInternal()); //Check these - should they be tied to WA?
        requestCaseDetailsBuilder.publicCaseName(caseData.getWorkAllocationFields().getCaseNamePublic());
        requestCaseDetailsBuilder.caseAdditionalSecurityFlag(caseData.getAdditionalSecurityFlag().toBoolean());
        requestCaseDetailsBuilder.caseInterpreterRequiredFlag(isInterpreterRequired(
            caseData.getAdjournCaseInterpreterRequired()).toBoolean());
        requestCaseDetailsBuilder.caseCategories(buildCaseCategories(caseData));
        requestCaseDetailsBuilder.caseManagementLocationCode(getCaseManagementLocationCode(caseData.getCaseManagementLocation()));
        requestCaseDetailsBuilder.caseRestrictedFlag(caseData.getSensitiveFlag().toBoolean()); //Spreadsheet seems to say this should go here. Check
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

    private static HearingWindow buildHearingWindow(SscsCaseData caseData) {  // TODO check this is correct and if any additional logic is needed
        LocalDate dateRangeStart = null;
        LocalDate dateRangeEnd;

        if (YesNo.isYes(caseData.getAutoListFlag()) && nonNull(caseData.getEvents())) {
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
            .build();

    }



    public static YesNo shouldBeAutoListed(YesNo autoListFlag) {
        boolean isYes = YesNo.isYes(autoListFlag);
        // TODO add auto listing reasons,
        //      YesNo.isNoOrNull(isCaseLinked)
        return isYes ? YES : NO;
    }

    public static YesNo shouldBeHearingsInWelshFlag(YesNo hearingsInWelshFlag) {
        boolean isYes = YesNo.isYes(hearingsInWelshFlag);
        return isYes ? YES : NO;
    }

    public static YesNo shouldBeAdditionalSecurityFlag(YesNo additionalSecurityFlag) {
        boolean isYes = YesNo.isYes(additionalSecurityFlag);
        // TODO Check unacceptableCustomerBehaviour for Appellant, their Appointee and their Representatives
        //      Check unacceptableCustomerBehaviour for each OtherParty, their Appointee and their Representatives
        //      Any YES then YES
        return isYes ? YES : NO;
    }

    public static YesNo shouldBeSensitiveFlag(YesNo sensitiveFlag) {
        boolean isYes = YesNo.isYes(sensitiveFlag);
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

    public static HearingWindowRange getHearingWindowRange(SscsCaseData caseData) {
        // TODO check this is correct and if any additional logic is needed
        if (YesNo.isYes(caseData.getAutoListFlag()) && nonNull(caseData.getEvents())) {
            Event dwpResponded = caseData.getEvents().stream()
                    .filter(c -> EventType.DWP_RESPOND.equals(c.getValue().getEventType()))
                    .findFirst().orElse(null);
            if (nonNull(dwpResponded) && nonNull(dwpResponded.getValue())) {
                LocalDate hearingWindowStart = dwpResponded.getValue().getDateTime().plusMonths(1).toLocalDate();
                return HearingWindowRange.builder().dateRangeStart(hearingWindowStart).build();
            }
        }
        return null;
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

    public static SscsHearingType getHearingType(SscsCaseData caseData) {
        SscsHearingType hearingType = null;
        // TODO find out what logic is needed here
        return hearingType;
    }

    public static YesNo isInterpreterRequired(String adjournCaseInterpreterRequired) {
        boolean isYes = YesNo.isYes(adjournCaseInterpreterRequired);
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
