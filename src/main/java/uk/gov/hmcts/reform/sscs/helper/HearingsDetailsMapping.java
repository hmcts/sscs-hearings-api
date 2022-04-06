package uk.gov.hmcts.reform.sscs.helper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.*;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDetails;
import uk.gov.hmcts.reform.sscs.service.SessionLookupService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;

@Component
public class HearingsDetailsMapping {

    public static final String NORMAL = "Normal";
    public static final String HIGH = "High";

    private final SessionLookupService sessionLookupService;
    public static final int DEFAULT_DURATION = 30;

    @Autowired
    public HearingsDetailsMapping(SessionLookupService sessionLookupService) {
        this.sessionLookupService = sessionLookupService;

    }

    public HearingDetails buildHearingDetails(HearingWrapper wrapper) {
        SscsCaseData caseData = wrapper.getOriginalCaseData();

        HearingDetails.HearingDetailsBuilder hearingDetailsBuilder = HearingDetails.builder();

        boolean autoListed = shouldBeAutoListed();

        hearingDetailsBuilder.autolistFlag(autoListed);
        hearingDetailsBuilder.hearingInWelshFlag(shouldBeHearingsInWelshFlag());

        hearingDetailsBuilder.hearingIsLinkedFlag(isCaseLinked());
        hearingDetailsBuilder.hearingType(getHearingType(caseData));  // Assuming key is what is required.
        hearingDetailsBuilder.hearingWindow(buildHearingWindow(caseData, autoListed));
        hearingDetailsBuilder.duration(getHearingDuration(caseData));
        hearingDetailsBuilder.hearingPriorityType(getHearingPriority(caseData.getAdjournCaseCanCaseBeListedRightAway(), //Confirm this
            caseData.getUrgentCase()));
        hearingDetailsBuilder.numberOfPhysicalAttendees(getNumberOfPhysicalAttendees());
        hearingDetailsBuilder.hearingLocations(getHearingLocations(caseData.getCaseManagementLocation()));
        hearingDetailsBuilder.facilitiesRequired(getFacilitiesRequired(caseData));
        hearingDetailsBuilder.listingComments(getListingComments(caseData.getAppeal(), caseData.getOtherParties()));
        hearingDetailsBuilder.hearingRequester(getHearingRequester());
        hearingDetailsBuilder.leadJudgeContractType(getLeadJudgeContractType());
        hearingDetailsBuilder.panelRequirements(getPanelRequirements(caseData));

        return hearingDetailsBuilder.build();
    }

    public boolean shouldBeAutoListed() {
        // TODO Future Work
        return true;
    }

    public boolean shouldBeHearingsInWelshFlag() {
        // TODO Future Work
        return false;
    }

    public boolean isCaseLinked() {
        // TODO Future work
        // boolean isYes = nonNull(caseData.getLinkedCase()) && !caseData.getLinkedCase().isEmpty();
        // driven by benefit or issue, can't be auto listed
        return false;
    }

    public String getHearingType(SscsCaseData caseData) {
        String hearingType = null;
        // TODO Dependant on SSCS-10273 - find out what logic is needed here
        return hearingType;
    }

    public HearingWindow buildHearingWindow(SscsCaseData caseData, boolean autoListed) {
        LocalDate dateRangeStart = null;
        LocalDate dateRangeEnd = null;

        if (autoListed && nonNull(caseData.getEvents())) {
            if (isYes(caseData.getUrgentCase()) && isNotBlank(caseData.getCaseCreated())) {
                dateRangeStart = LocalDate.parse(caseData.getCaseCreated()).plusDays(14);
            } else {
                Event dwpResponded = caseData.getEvents().stream()
                        .filter(c -> EventType.DWP_RESPOND.equals(c.getValue().getEventType()))
                        .findFirst().orElse(null);
                if (nonNull(dwpResponded) && nonNull(dwpResponded.getValue()) && isNotBlank(dwpResponded.getValue().getDate())) {
                    dateRangeStart = dwpResponded.getValue().getDateTime().plusMonths(1).toLocalDate();
                }
            }

        }

        return HearingWindow.builder()
                .firstDateTimeMustBe(getFirstDateTimeMustBe())
                .dateRangeStart(dateRangeStart)
                .dateRangeEnd(dateRangeEnd)
                .build();
    }

    public LocalDateTime getFirstDateTimeMustBe() {
        // TODO Adjournments - Find out how to use adjournCase data to work this out, possibly related variables:
        //      adjournCaseNextHearingDateType, adjournCaseNextHearingDateOrPeriod, adjournCaseNextHearingDateOrTime,
        //      adjournCaseNextHearingFirstAvailableDateAfterDate, adjournCaseNextHearingFirstAvailableDateAfterPeriod
        // TODO Future Work for override
        return null;
    }

    public int getHearingDuration(SscsCaseData caseData) {
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
            String ccdKey = caseData.getBenefitCode() + caseData.getIssueCode();
            int result = sessionLookupService.getDuration(ccdKey);
            if (result != 0) {
                return result;
            }
            return DEFAULT_DURATION;
        }
        return DEFAULT_DURATION;
    }

    public String getHearingPriority(String isAdjournCase, String isUrgentCase) {
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

    public Number getNumberOfPhysicalAttendees() {
        // TODO Implementation to be done by SSCS-10260
        return null;
    }

    public List<HearingLocations> getHearingLocations(CaseManagementLocation caseManagementLocation) {
        // locationType - from reference data - processing venue to venue type/epims
        // locationId - epims
        // manual over-ride e.g. if a judge wants to change venue
        // if paper case - display all venues in that region
        // locations where there is more than one venue
        // Normally one location, but can be two in some cities.
        // TODO Implementation to be done by SSCS-10245 - work out what venues to choose and get epims/locationType info from Reference Data
        return new ArrayList<>();
    }

    public List<String> getFacilitiesRequired(SscsCaseData caseData) {
        List<String> facilitiesRequired = new ArrayList<>();
        // TODO Dependant on SSCS-10116 - find out how to work this out and implement
        //          caseData.getAppeal().getHearingOptions().getArrangements()
        //          for each otherParty otherParty.getHearingOptions().getArrangements()
        return facilitiesRequired;
    }

    public String getListingComments(Appeal appeal, List<CcdValue<OtherParty>> otherParties) {
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

    public String getHearingRequester() {
        // TODO Implementation to be done by SSCS-10260. Optional?
        return null;
    }

    public String getLeadJudgeContractType() {
        // TODO Implementation to be done by SSCS-10260
        return null;
    }

    public PanelRequirements getPanelRequirements(SscsCaseData caseData) {
        var panelRequirementsBuilder = PanelRequirements.builder();

        List<String> roleTypes = new ArrayList<>();
        // TODO Dependant on SSCS-10116 - Will be linked to Session Category Reference Data,
        //      find out what role types there are and how these are determined
        if (nonNull(caseData.getBenefitCode()) && nonNull(caseData.getIssueCode())) {
            roleTypes.addAll(sessionLookupService.getPanelMembers(caseData.getBenefitCode() + caseData.getIssueCode()));
        }

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

    public List<PanelPreference> getPanelPreferences(SscsCaseData caseData) {
        List<PanelPreference> panelPreferences = new ArrayList<>();
        // TODO Adjournments - loop to go through Judicial members that are need to be included or excluded
        // TODO Waqas - Check no other reason to have panel preferences
        //      Will need Judicial Staff Reference Data
        return panelPreferences;
    }
}
