package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.*;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDetails;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingDuration;
import uk.gov.hmcts.reform.sscs.reference.data.model.SessionCategoryMap;
import uk.gov.hmcts.reform.sscs.service.ReferenceDataServiceHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.Valid;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.sscs.ccd.domain.HearingType.PAPER;
import static uk.gov.hmcts.reform.sscs.ccd.domain.PanelMember.MQPM1;
import static uk.gov.hmcts.reform.sscs.ccd.domain.PanelMember.MQPM2;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsCaseMapping.isInterpreterRequired;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsCaseMapping.shouldBeAdditionalSecurityFlag;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMapping.getSessionCaseCode;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingPriority.HIGH;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingPriority.NORMAL;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingTypeLov.SUBSTANTIVE;

@SuppressWarnings({"PMD.UnnecessaryLocalBeforeReturn","PMD.ReturnEmptyCollectionRatherThanNull", "PMD.GodClass", "PMD.ExcessiveImports"})
// TODO Unsuppress in future
public final class HearingsDetailsMapping {

    public static final int DURATION_SESSIONS_MULTIPLIER = 165;
    public static final int DURATION_HOURS_MULTIPLIER = 60;
    public static final int DURATION_DEFAULT = 30;
    public static final int DAYS_TO_ADD_HEARING_WINDOW_DWP_RESPONDED_URGENT_CASE = 14;
    public static final int DAYS_TO_ADD_HEARING_WINDOW_DWP_RESPONDED = 28;
    public static final int DAYS_TO_ADD_HEARING_WINDOW_TODAY = 1;

    private HearingsDetailsMapping() {

    }

    public static HearingDetails buildHearingDetails(HearingWrapper wrapper, ReferenceDataServiceHolder referenceData) {
        SscsCaseData caseData = wrapper.getCaseData();

        boolean autoListed = shouldBeAutoListed(caseData, referenceData);

        return HearingDetails.builder()
            .autolistFlag(autoListed)
            .hearingType(getHearingType())
            .hearingWindow(buildHearingWindow(caseData, autoListed))
            .duration(getHearingDuration(caseData, referenceData))
            .nonStandardHearingDurationReasons(getNonStandardHearingDurationReasons())
            .hearingPriorityType(getHearingPriority(caseData))
            .numberOfPhysicalAttendees(getNumberOfPhysicalAttendees(caseData))
            .hearingInWelshFlag(shouldBeHearingsInWelshFlag())
            .hearingLocations(getHearingLocations(caseData.getCaseManagementLocation()))
            .facilitiesRequired(getFacilitiesRequired(caseData))
            .listingComments(getListingComments(caseData))
            .hearingRequester(getHearingRequester())
            .privateHearingRequiredFlag(isPrivateHearingRequired())
            .leadJudgeContractType(getLeadJudgeContractType())
            .panelRequirements(getPanelRequirements(caseData, referenceData))
            .hearingIsLinkedFlag(isCaseLinked(caseData))
            .amendReasonCode(getAmendReasonCode())
            .build();
    }

    public static boolean shouldBeAutoListed(@Valid SscsCaseData caseData, ReferenceData referenceData) {
        return !(isCaseUrgent(caseData)
                || hasOrgRepresentative(caseData)
                || shouldBeAdditionalSecurityFlag(caseData)
                || isInterpreterRequired(caseData)
                || isCaseLinked(caseData)
                || isPaperCaseAndNoPO(caseData)
                || hasDqpmOrFqpm(caseData, referenceData)
                || isThereOtherComments(caseData)
            );
    }

    public static boolean isCaseUrgent(@Valid SscsCaseData caseData) {
        return isYes(caseData.getUrgentCase());
    }

    public static boolean hasOrgRepresentative(@Valid SscsCaseData caseData) {
        return !isRepresentativeOrg(caseData.getAppeal().getRep())
                && hasOrgOtherParties(caseData.getOtherParties());
    }

    public static boolean hasOrgOtherParties(Collection<CcdValue<OtherParty>> otherParties) {
        return otherParties.stream()
                .map(CcdValue::getValue)
                .map(OtherParty::getRep)
                .noneMatch(HearingsDetailsMapping::isRepresentativeOrg);
    }

    public static boolean isRepresentativeOrg(Representative rep) {
        return nonNull(rep) && isYes(rep.getHasRepresentative()) && isNotBlank(rep.getOrganisation());
    }

    public static boolean isPaperCaseAndNoPO(@Valid SscsCaseData caseData) {
        return isPaperCase(caseData) && !isPoAttending(caseData);
    }

    public static boolean isThereOtherComments(@Valid SscsCaseData caseData) {
        return isNotBlank(getListingComments(caseData));
    }

    public static boolean hasDqpmOrFqpm(@Valid SscsCaseData caseData, ReferenceData referenceData) {
        SessionCategoryMap sessionCategoryMap = getSessionCaseCode(caseData, referenceData);
        return sessionCategoryMap.getCategory().getPanelMembers().stream()
                .anyMatch(HearingsDetailsMapping::isDqpmOrFqpm);
    }

    public static boolean isDqpmOrFqpm(PanelMember panelMember) {
        // TODO Andrew needs to confirm if DQPM or MQPM
        switch (panelMember) {
            case DQPM:
            case FQPM:
                return true;
            default:
                return false;
        }
    }

    public static String getHearingType() {
        return SUBSTANTIVE.getHmcReference();
    }

    public static HearingWindow buildHearingWindow(@Valid SscsCaseData caseData, boolean autoListed) {
        return HearingWindow.builder()
                .firstDateTimeMustBe(getFirstDateTimeMustBe())
                .dateRangeStart(getHearingWindowStart(caseData, autoListed))
                .dateRangeEnd(null)
                .build();
    }

    public static LocalDate getHearingWindowStart(@Valid SscsCaseData caseData, boolean autoListed) {
        if (isNotBlank(caseData.getDwpResponseDate())) {
            LocalDate dwpResponded = LocalDate.parse(caseData.getDwpResponseDate());
            if (isCaseUrgent(caseData)) {
                return dwpResponded.plusDays(DAYS_TO_ADD_HEARING_WINDOW_DWP_RESPONDED_URGENT_CASE);
            } else if (autoListed) {
                return dwpResponded.plusDays(DAYS_TO_ADD_HEARING_WINDOW_DWP_RESPONDED);
            }
        }
        return LocalDate.now().plusDays(DAYS_TO_ADD_HEARING_WINDOW_TODAY);
    }

    public static LocalDateTime getFirstDateTimeMustBe() {
        // TODO Adjournments - Find out how to use adjournCase data to work this out, possibly related variables:
        //      adjournCaseNextHearingDateType, adjournCaseNextHearingDateOrPeriod, adjournCaseNextHearingDateOrTime,
        //      adjournCaseNextHearingFirstAvailableDateAfterDate, adjournCaseNextHearingFirstAvailableDateAfterPeriod
        // TODO Future Work - Manual Override
        return null;
    }

    public static int getHearingDuration(SscsCaseData caseData, ReferenceDataServiceHolder referenceData) {
        // TODO Adjournments - Check this is the correct logic for Adjournments
        // TODO Future Work - Manual Override

        Integer duration = getHearingDurationAdjournment(caseData);
        if (isNull(duration)) {
            duration = getHearingDurationBenefitIssueCodes(caseData, referenceData);
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

    public static Integer getHearingDurationBenefitIssueCodes(SscsCaseData caseData, ReferenceDataServiceHolder referenceData) {
        HearingDuration hearingDuration = referenceData.getHearingDurations().getHearingDuration(
            caseData.getBenefitCode(), caseData.getIssueCode());

        if (isNull(hearingDuration)) {
            return null;
        }

        if (isYes(caseData.getAppeal().getHearingOptions().getWantsToAttend())) {
            Integer duration = isInterpreterRequired(caseData)
                    ? hearingDuration.getDurationInterpreter()
                    : hearingDuration.getDurationFaceToFace();
            return referenceData.getHearingDurations()
                    .addExtraTimeIfNeeded(duration, hearingDuration.getBenefitCode(), hearingDuration.getIssue(),
                            getElementsDisputed(caseData));
        } else if (isPaperCase(caseData)) {
            return hearingDuration.getDurationPaper();
        } else {
            return null;
        }
    }

    public static boolean isPaperCase(SscsCaseData caseData) {
        return PAPER.getValue().equalsIgnoreCase(caseData.getAppeal().getHearingType());
    }

    public static boolean isPoAttending(SscsCaseData caseData) {
        return isYes(caseData.getDwpIsOfficerAttending());
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
            return HIGH.getHmcReference();
        }
        return NORMAL.getHmcReference();
    }

    public static int getNumberOfPhysicalAttendees(SscsCaseData caseData) {
        int numberOfAttendees = 0;
        // get a value if it is facetoface from hearingSubType -> wantsHearingTypeFaceToFace
        if (nonNull(caseData.getAppeal())
                && nonNull(caseData.getAppeal().getHearingSubtype())
                && nonNull(caseData.getAppeal().getHearingSubtype().isWantsHearingTypeFaceToFace())
                && caseData.getAppeal().getHearingSubtype().isWantsHearingTypeFaceToFace()) {
            //appellants + dwp attendee (1) + judge (1) + panel members + representative (1)
            numberOfAttendees = 1;
            if (isYes(caseData.getAppeal().getHearingOptions().getWantsToAttend())) {
                numberOfAttendees++;
            }

            if (isYes(caseData.getAppeal().getRep().getHasRepresentative())) {
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

    public static boolean shouldBeHearingsInWelshFlag() {
        // TODO Future Work
        return false;
    }

    public static List<HearingLocations> getHearingLocations(CaseManagementLocation caseManagementLocation) {
        HearingLocations hearingLocations = new HearingLocations();
        hearingLocations.setLocationId(caseManagementLocation.getBaseLocation());
        hearingLocations.setLocationType("court");

        List<HearingLocations> hearingLocationsList = new ArrayList<>();
        hearingLocationsList.add(hearingLocations);

        return hearingLocationsList;
    }

    public static List<String> getFacilitiesRequired(SscsCaseData caseData) {
        // TODO Dependant on SSCS-10116 - find out how to work this out and implement
        //          caseData.getAppeal().getHearingOptions().getArrangements()
        //          for each otherParty otherParty.getHearingOptions().getArrangements()
        return Optional.ofNullable(caseData.getAppeal())
                .map(Appeal::getHearingOptions)
                .map(HearingOptions::getArrangements)
                .orElse(new ArrayList<>());
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
        return  entity.getName().getFullName();
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

    public static PanelRequirements getPanelRequirements(SscsCaseData caseData, ReferenceDataServiceHolder referenceData) {
        return PanelRequirements.builder()
                .roleTypes(getRoleTypes())
                .authorisationTypes(getAuthorisationTypes())
                .authorisationSubTypes(getAuthorisationSubTypes())
                .panelPreferences(getPanelPreferences(caseData))
                .panelSpecialisms(getPanelSpecialisms(caseData, getSessionCaseCode(caseData, referenceData)))
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

    public static List<String> getPanelSpecialisms(SscsCaseData caseData, SessionCategoryMap sessionCategoryMap) {
        List<String> panelSpecialisms = new ArrayList<>();
        if (isNull(sessionCategoryMap)) {
            return panelSpecialisms;
        }

        String doctorSpecialism = caseData.getSscsIndustrialInjuriesData().getPanelDoctorSpecialism();
        String doctorSpecialismSecond = caseData.getSscsIndustrialInjuriesData().getSecondPanelDoctorSpecialism();
        panelSpecialisms = sessionCategoryMap.getCategory().getPanelMembers().stream()
                .map(panelMember -> getPanelMemberSpecialism(panelMember, doctorSpecialism, doctorSpecialismSecond))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return panelSpecialisms;
    }

    public static String getPanelMemberSpecialism(PanelMember panelMember,
                                                    String doctorSpecialism, String doctorSpecialismSecond) {
        switch (panelMember) {
            case MQPM1:
                return panelMember.getReference(doctorSpecialism);
            case MQPM2:
                return panelMember.getReference(doctorSpecialismSecond);
            default:
                return panelMember.getReference();
        }
    }

    public static List<PanelPreference> getPanelPreferences(SscsCaseData caseData) {
        List<PanelPreference> panelPreferences = new ArrayList<>();
        // TODO Adjournments - loop to go through Judicial members that are need to be included or excluded
        // TODO Potentially used with Manual overrides
        //      Will need Judicial Staff Reference Data
        return panelPreferences;
    }

    public static boolean isCaseLinked(@Valid SscsCaseData caseData) {
        return isNotEmpty(LinkedCasesMapping.getLinkedCases(caseData));
    }

    private static String getAmendReasonCode() {
        // TODO Future Work
        return null;
    }
}
