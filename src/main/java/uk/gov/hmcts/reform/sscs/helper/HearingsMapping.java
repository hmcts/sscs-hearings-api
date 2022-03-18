package uk.gov.hmcts.reform.sscs.helper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingRequest.HearingRequestBuilder;
import uk.gov.hmcts.reform.sscs.ccd.domain.PanelRequirements.PanelRequirementsBuilder;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;

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

    @Value("${exui.url}")
    private static String exUiUrl;

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

    public static HearingRequest createHearingRequest(HearingWrapper wrapper) {
        SscsCaseData caseData = wrapper.getUpdatedCaseData();

        HearingRequestBuilder request = HearingRequest.builder();

        request.initialRequestTimestamp(LocalDateTime.now());
        request.autoListFlag(caseData.getAutoListFlag());
        request.inWelshFlag(caseData.getHearingsInWelshFlag());
        request.isLinkedFlag(isCaseLinked(caseData));
        request.additionalSecurityFlag(caseData.getAdditionalSecurityFlag());
        request.sensitiveFlag(caseData.getSensitiveFlag());
        request.interpreterRequiredFlag(isInterpreterRequired(caseData.getAdjournCaseInterpreterRequired()));
        request.hearingType(getHearingType(caseData));
        request.firstDateTimeMustBe(getFirstDateTimeMustBe(caseData));
        request.hearingWindowRange(getHearingWindowRange(caseData));
        request.duration(getHearingDuration(caseData));
        request.hearingPriorityType(getHearingPriority(caseData.getAdjournCaseCanCaseBeListedRightAway(),
                caseData.getUrgentCase()));
        request.hearingLocations(getHearingLocations(caseData.getCaseManagementLocation()));
        request.facilitiesRequired(getFacilitiesRequired(caseData));
        request.listingComments(getListingComments(caseData.getAppeal(), caseData.getOtherParties()));
        request.leadJudgeContractType(getLeadJudgeContractType(caseData));
        request.panelRequirements(getPanelRequirements(caseData));

        return request.build();
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

    public static List<CcdValue<String>> getFacilitiesRequired(SscsCaseData caseData) {
        List<CcdValue<String>> facilitiesRequired = new ArrayList<>();
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
        PanelRequirementsBuilder panelRequirements = PanelRequirements.builder();

        List<CcdValue<String>> roleTypes = new ArrayList<>();
        // TODO Will be linked to Session Category Reference Data,
        //      find out what role types there are and how these are determined

        panelRequirements.roleTypes(roleTypes);

        List<CcdValue<String>> authorisationSubType = new ArrayList<>();
        // TODO Will be linked to Session Category Reference Data,
        //      find out what subtypes there are and how these are determined
        panelRequirements.authorisationSubType(authorisationSubType);

        panelRequirements.panelPreferences(getPanelPreferences(caseData));

        List<CcdValue<String>> panelSpecialisms = new ArrayList<>();
        // TODO find out what specialisms there are and how these are determined
        panelRequirements.panelSpecialisms(panelSpecialisms);

        return panelRequirements.build();
    }

    public static List<CcdValue<PanelPreference>> getPanelPreferences(SscsCaseData caseData) {
        List<CcdValue<PanelPreference>> panelPreferences = new ArrayList<>();
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

    public static List<CcdValue<HearingLocation>> getHearingLocations(CaseManagementLocation caseManagementLocation) {
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
