package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.CcdValue;
import uk.gov.hmcts.reform.sscs.ccd.domain.DynamicListItem;
import uk.gov.hmcts.reform.sscs.ccd.domain.ElementDisputed;
import uk.gov.hmcts.reform.sscs.ccd.domain.ElementDisputedDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.Entity;
import uk.gov.hmcts.reform.sscs.ccd.domain.OtherParty;
import uk.gov.hmcts.reform.sscs.ccd.domain.OverrideFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.Party;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.model.HearingLocation;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingWindow;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PanelPreference;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PanelRequirements;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingDuration;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingChannelMapping.getHearingChannelsHmcReference;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsCaseMapping.isInterpreterRequired;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMapping.getSessionCaseCode;
import static uk.gov.hmcts.reform.sscs.helper.mapping.PanelMemberSpecialismsMapping.getPanelSpecialisms;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.LocationType.COURT;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.RequirementType.MUST_INCLUDE;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingPriority.STANDARD;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingPriority.URGENT;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingTypeLov.SUBSTANTIVE;

@SuppressWarnings({"PMD.UnnecessaryLocalBeforeReturn", "PMD.ReturnEmptyCollectionRatherThanNull", "PMD.GodClass", "PMD.ExcessiveImports"})
// TODO Unsuppress in future
public final class HearingsDetailsMapping {

    public static final int DURATION_SESSIONS_MULTIPLIER = 165;
    public static final int DURATION_HOURS_MULTIPLIER = 60;
    public static final int DURATION_DEFAULT = 30;
    public static final int DAYS_TO_ADD_HEARING_WINDOW_DWP_RESPONDED = 28;
    public static final int DAYS_TO_ADD_HEARING_WINDOW_TODAY = 1;

    private HearingsDetailsMapping() {

    }

    public static HearingDetails buildHearingDetails(HearingWrapper wrapper, ReferenceDataServiceHolder referenceDataServiceHolder) {
        SscsCaseData caseData = wrapper.getCaseData();

        boolean autoListed = HearingsAutoListMapping.shouldBeAutoListed(caseData, referenceDataServiceHolder);

        return HearingDetails.builder()
            .autolistFlag(autoListed)
            .hearingType(getHearingType())
            .hearingWindow(buildHearingWindow(caseData))
            .duration(getHearingDuration(caseData, referenceDataServiceHolder))
            .nonStandardHearingDurationReasons(getNonStandardHearingDurationReasons())
            .hearingPriorityType(getHearingPriority(caseData))
            .numberOfPhysicalAttendees(HearingsNumberAttendeesMapping.getNumberOfPhysicalAttendees(caseData))
            .hearingInWelshFlag(shouldBeHearingsInWelshFlag())
            .hearingLocations(getHearingLocations(caseData, referenceDataServiceHolder))
            .facilitiesRequired(getFacilitiesRequired())
            .listingComments(getListingComments(caseData))
            .hearingRequester(getHearingRequester())
            .privateHearingRequiredFlag(isPrivateHearingRequired())
            .leadJudgeContractType(getLeadJudgeContractType())
            .panelRequirements(getPanelRequirements(caseData, referenceDataServiceHolder))
            .hearingIsLinkedFlag(isCaseLinked(caseData))
            .amendReasonCode(getAmendReasonCode())
            .hearingChannels(getHearingChannelsHmcReference(caseData))
            .build();
    }

    public static String getHearingType() {
        return SUBSTANTIVE.getHmcReference();
    }

    public static HearingWindow buildHearingWindow(@Valid SscsCaseData caseData) {

        OverrideFields overrideFields = OverridesMapping.getOverrideFields(caseData);

        if (nonNull(overrideFields.getHearingWindow())
            && (nonNull(overrideFields.getHearingWindow().getFirstDateTimeMustBe())
            || nonNull(overrideFields.getHearingWindow().getDateRangeStart())
            || nonNull(overrideFields.getHearingWindow().getDateRangeEnd()))) {
            return HearingWindow.builder()
                .firstDateTimeMustBe(overrideFields.getHearingWindow().getFirstDateTimeMustBe())
                .dateRangeStart(overrideFields.getHearingWindow().getDateRangeStart())
                .dateRangeEnd(overrideFields.getHearingWindow().getDateRangeEnd())
                .build();
        }

        return HearingWindow.builder()
            .firstDateTimeMustBe(getFirstDateTimeMustBe())
            .dateRangeStart(getHearingWindowStart(caseData))
            .dateRangeEnd(null)
            .build();
    }

    /**
     * This method sets hearing window start date according to the jira task
     * <a href="https://tools.hmcts.net/jira/browse/SSCS-10666">SSCS-10666</a>
     * 1. When case not autolisted and not an urgent case, buildHearingWindow should still be +28 days
     * 2. If the case is urgent and the hearing channel is either paper or oral case we should list +1 day
     * 3. if it is not urgent and paper or case it returns to  +28 days.(this is the same as number 1)
     * Those three items means that when the case is urgent hearing window start date is 1 day after the dwpResponded
     * day. If not urgent it is always 28 days after the dwpResponded date.
     * @param caseData SscsCaseData
     * @return LocalDate value that is calculated as hearing window start date.
     */
    public static LocalDate getHearingWindowStart(@Valid SscsCaseData caseData) {
        if (isNotBlank(caseData.getDwpResponseDate())) {
            LocalDate dwpResponded = LocalDate.parse(caseData.getDwpResponseDate());
            if (isCaseUrgent(caseData)) {
                return dwpResponded.plusDays(DAYS_TO_ADD_HEARING_WINDOW_TODAY);
            } else {
                return dwpResponded.plusDays(DAYS_TO_ADD_HEARING_WINDOW_DWP_RESPONDED);
            }
        } else {
            return LocalDate.now().plusDays(DAYS_TO_ADD_HEARING_WINDOW_TODAY);
        }
    }

    public static boolean isCaseUrgent(@Valid SscsCaseData caseData) {
        return isYes(caseData.getUrgentCase());
    }

    public static LocalDateTime getFirstDateTimeMustBe() {
        // TODO Adjournments - Find out how to use adjournCase data to work this out, possibly related variables:
        //      adjournCaseNextHearingDateType, adjournCaseNextHearingDateOrPeriod, adjournCaseNextHearingDateOrTime,
        //      adjournCaseNextHearingFirstAvailableDateAfterDate, adjournCaseNextHearingFirstAvailableDateAfterPeriod
        // TODO Future Work - Manual Override
        return null;
    }

    public static int getHearingDuration(SscsCaseData caseData, ReferenceDataServiceHolder referenceDataServiceHolder) {
        OverrideFields overrideFields = OverridesMapping.getOverrideFields(caseData);
        if (nonNull(overrideFields.getDuration()) && overrideFields.getDuration().intValue() > 0) {
            return overrideFields.getDuration().intValue();
        }

        Integer duration = getHearingDurationAdjournment(caseData);
        if (isNull(duration)) {
            duration = getHearingDurationBenefitIssueCodes(caseData, referenceDataServiceHolder);
        }

        return nonNull(duration) ? duration : DURATION_DEFAULT;
    }

    public static Integer getHearingDurationAdjournment(SscsCaseData caseData) {
        if (isNotBlank(caseData.getAdjournCaseNextHearingListingDuration())
            && Integer.parseInt(caseData.getAdjournCaseNextHearingListingDuration()) > 0) {

            if ("sessions".equalsIgnoreCase(caseData.getAdjournCaseNextHearingListingDurationUnits())) {
                return Integer.parseInt(caseData.getAdjournCaseNextHearingListingDuration()) * DURATION_SESSIONS_MULTIPLIER;
            }
            if ("hours".equalsIgnoreCase(caseData.getAdjournCaseNextHearingListingDurationUnits())) {
                // TODO Adjournments - check no other measurement than hours, sessions and null
                return Integer.parseInt(caseData.getAdjournCaseNextHearingListingDuration()) * DURATION_HOURS_MULTIPLIER;
            }
        }

        return null;
    }

    public static Integer getHearingDurationBenefitIssueCodes(SscsCaseData caseData, ReferenceDataServiceHolder referenceDataServiceHolder) {
        HearingDuration hearingDuration = referenceDataServiceHolder.getHearingDurations().getHearingDuration(
            caseData.getBenefitCode(), caseData.getIssueCode());

        if (isNull(hearingDuration)) {
            return null;
        }

        if (isYes(caseData.getAppeal().getHearingOptions().getWantsToAttend())) {
            Integer duration = isInterpreterRequired(caseData)
                ? hearingDuration.getDurationInterpreter()
                : hearingDuration.getDurationFaceToFace();
            return referenceDataServiceHolder.getHearingDurations()
                .addExtraTimeIfNeeded(duration, hearingDuration.getBenefitCode(), hearingDuration.getIssue(),
                                      getElementsDisputed(caseData)
                );
        } else if (HearingChannelMapping.isPaperCase(caseData)) {
            return hearingDuration.getDurationPaper();
        } else {
            return null;
        }
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public static List<String> getElementsDisputed(SscsCaseData caseData) {
        List<ElementDisputed> elementDisputed = new ArrayList<>();
        if (isNotEmpty(caseData.getElementsDisputedGeneral())) {
            elementDisputed.addAll(caseData.getElementsDisputedGeneral());
        }
        if (isNotEmpty(caseData.getElementsDisputedSanctions())) {
            elementDisputed.addAll(caseData.getElementsDisputedSanctions());
        }
        if (isNotEmpty(caseData.getElementsDisputedOverpayment())) {
            elementDisputed.addAll(caseData.getElementsDisputedOverpayment());
        }
        if (isNotEmpty(caseData.getElementsDisputedHousing())) {
            elementDisputed.addAll(caseData.getElementsDisputedHousing());
        }
        if (isNotEmpty(caseData.getElementsDisputedChildCare())) {
            elementDisputed.addAll(caseData.getElementsDisputedChildCare());
        }
        if (isNotEmpty(caseData.getElementsDisputedCare())) {
            elementDisputed.addAll(caseData.getElementsDisputedCare());
        }
        if (isNotEmpty(caseData.getElementsDisputedChildElement())) {
            elementDisputed.addAll(caseData.getElementsDisputedChildElement());
        }
        if (isNotEmpty(caseData.getElementsDisputedChildDisabled())) {
            elementDisputed.addAll(caseData.getElementsDisputedChildDisabled());
        }
        if (isNotEmpty(caseData.getElementsDisputedLimitedWork())) {
            elementDisputed.addAll(caseData.getElementsDisputedLimitedWork());
        }
        return elementDisputed.stream()
            .map(ElementDisputed::getValue)
            .map(ElementDisputedDetails::getIssueCode)
            .collect(Collectors.toList());
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
        if (isCaseUrgent(caseData) || isYes(caseData.getAdjournCasePanelMembersExcluded())) {
            return URGENT.getHmcReference();
        }
        return STANDARD.getHmcReference();
    }

    public static boolean shouldBeHearingsInWelshFlag() {
        // TODO Future Work
        return false;
    }

    public static List<HearingLocation> getHearingLocations(SscsCaseData caseData,
                                                            ReferenceDataServiceHolder referenceDataServiceHolder) {

        OverrideFields overrideFields = OverridesMapping.getOverrideFields(caseData);

        if (ObjectUtils.isNotEmpty(overrideFields.getHearingVenueEpimsId())) {
            return overrideFields.getHearingVenueEpimsId().stream()
                .map(CcdValue::getValue)
                .map(x -> HearingLocation.builder()
                    .locationId(x)
                    .locationType(COURT)
                    .build())
                .collect(Collectors.toList());
        }

        String epimsId = referenceDataServiceHolder
            .getVenueService()
            .getEpimsIdForVenue(caseData.getProcessingVenue())
            .orElse(null);

        HearingLocation hearingLocation = new HearingLocation();
        hearingLocation.setLocationId(epimsId);
        hearingLocation.setLocationType(COURT);

        List<HearingLocation> hearingLocations = new ArrayList<>();
        hearingLocations.add(hearingLocation);

        return hearingLocations;
    }

    public static List<String> getFacilitiesRequired() {
        return Collections.emptyList();
    }

    public static String getListingComments(SscsCaseData caseData) {
        Appeal appeal = caseData.getAppeal();
        List<CcdValue<OtherParty>> otherParties = caseData.getOtherParties();

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

        if (listingComments.isEmpty()) {
            return null;
        }

        return String.join(String.format("%n%n"), listingComments);
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
        return entity.getName().getFullName();
    }

    public static String getHearingRequester() {
        // TODO Implementation to be done by SSCS-10260. Optional?
        return null;
    }

    public static boolean isPrivateHearingRequired() {
        // TODO Future Work
        return false;
    }

    public static String getLeadJudgeContractType() {
        // TODO Implementation to be done by SSCS-10260
        return null;
    }

    public static PanelRequirements getPanelRequirements(SscsCaseData caseData,
                                                         ReferenceDataServiceHolder referenceDataServiceHolder) {
        return PanelRequirements.builder()
            .roleTypes(getRoleTypes())
            .authorisationTypes(getAuthorisationTypes())
            .authorisationSubTypes(getAuthorisationSubTypes())
            .panelPreferences(getPanelPreferences(caseData))
            .panelSpecialisms(getPanelSpecialisms(caseData, getSessionCaseCode(caseData, referenceDataServiceHolder)))
            .build();
    }

    public static List<String> getRoleTypes() {
        //TODO Need to retrieve RoleTypes from caseData and/or ReferenceData
        return Collections.emptyList();
    }

    public static List<String> getAuthorisationTypes() {
        //TODO Need to retrieve AuthorisationTypes from caseData and/or ReferenceData
        return Collections.emptyList();
    }

    public static List<String> getAuthorisationSubTypes() {
        //TODO Need to retrieve AuthorisationSubTypes from caseData and/or ReferenceData
        return Collections.emptyList();
    }


    public static List<PanelPreference> getPanelPreferences(SscsCaseData caseData) {

        List<PanelPreference> panelPreferences = new ArrayList<>();
        // TODO Adjournments - loop to go through Judicial members that are need to be included or excluded
        // TODO Potentially used with Manual overrides
        //      Will need Judicial Staff Reference Data

        OverrideFields overrideFields = OverridesMapping.getOverrideFields(caseData);

        if (nonNull(overrideFields.getReservedToJudge()) && isYes(overrideFields.getReservedToJudge().getIsReservedToMember())) {
            DynamicListItem reservedMemberListItem = overrideFields.getReservedToJudge().getReservedMember().getValue();
            panelPreferences.add(PanelPreference.builder()
                .memberID(reservedMemberListItem.getCode())
                .memberType("?") // TODO What should this be?
                .requirementType(MUST_INCLUDE)
                .build());
        }

        return panelPreferences;
    }

    public static boolean isCaseLinked(@Valid SscsCaseData caseData) {
        return isNotEmpty(LinkedCasesMapping.getLinkedCases(caseData));
    }

    private static List<String> getAmendReasonCode() {
        // TODO Future Work
        return new ArrayList<>();
    }

    public static boolean isPoOfficerAttending(@Valid SscsCaseData caseData) {
        OverrideFields overrideFields = OverridesMapping.getOverrideFields(caseData);
        if (nonNull(overrideFields.getPoToAttend())) {
            return isYes(overrideFields.getPoToAttend());
        }

        return isYes(caseData.getDwpIsOfficerAttending());
    }
}
