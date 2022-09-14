package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.CcdValue;
import uk.gov.hmcts.reform.sscs.ccd.domain.ElementDisputed;
import uk.gov.hmcts.reform.sscs.ccd.domain.ElementDisputedDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.Entity;
import uk.gov.hmcts.reform.sscs.ccd.domain.OtherParty;
import uk.gov.hmcts.reform.sscs.ccd.domain.OverrideFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.Party;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.model.HearingLocation;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.HearingType;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDetails;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingDuration;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsCaseMapping.isInterpreterRequired;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HearingType.SUBSTANTIVE;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.LocationType.COURT;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingPriority.STANDARD;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingPriority.URGENT;


@SuppressWarnings({"PMD.GodClass"})
// TODO Unsuppress in future
public final class HearingsDetailsMapping {

    public static final int DURATION_SESSIONS_MULTIPLIER = 165;
    public static final int DURATION_HOURS_MULTIPLIER = 60;
    public static final int DURATION_DEFAULT = 30;
    public static final int MIN_HEARING_DURATION = 1;

    private HearingsDetailsMapping() {

    }

    public static HearingDetails buildHearingDetails(HearingWrapper wrapper, ReferenceDataServiceHolder referenceDataServiceHolder) {
        SscsCaseData caseData = wrapper.getCaseData();

        boolean autoListed = HearingsAutoListMapping.shouldBeAutoListed(caseData, referenceDataServiceHolder);

        return HearingDetails.builder()
            .autolistFlag(autoListed)
            .hearingType(getHearingType())
            .hearingWindow(HearingsWindowMapping.buildHearingWindow(caseData))
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
            .panelRequirements(HearingsPanelMapping.getPanelRequirements(caseData, referenceDataServiceHolder))
            .hearingIsLinkedFlag(isCaseLinked(caseData))
            .amendReasonCodes(OverridesMapping.getAmendReasonCodes(caseData))
            .hearingChannels(HearingsChannelMapping.getHearingChannels(caseData))
            .build();
    }

    public static HearingType getHearingType() {
        return SUBSTANTIVE;
    }

    public static boolean isCaseUrgent(@Valid SscsCaseData caseData) {
        return isYes(caseData.getUrgentCase());
    }

    public static int getHearingDuration(SscsCaseData caseData, ReferenceDataServiceHolder referenceDataServiceHolder) {
        OverrideFields overrideFields = OverridesMapping.getOverrideFields(caseData);
        if (nonNull(overrideFields.getDuration()) && overrideFields.getDuration().intValue() >= MIN_HEARING_DURATION) {
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
            && Integer.parseInt(caseData.getAdjournCaseNextHearingListingDuration()) >= MIN_HEARING_DURATION) {

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
        } else if (HearingsChannelMapping.isPaperCase(caseData)) {
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
        return Collections.emptyList();
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

        if (isNotEmpty(overrideFields.getHearingVenueEpimsIds())) {
            return overrideFields.getHearingVenueEpimsIds().stream()
                .map(CcdValue::getValue)
                .map(CcdValue::getValue)
                .map(epimsId -> HearingLocation.builder()
                    .locationId(epimsId)
                    .locationType(COURT)
                    .build())
                .collect(Collectors.toList());
        }

        String epimsId = referenceDataServiceHolder
            .getVenueService()
            .getEpimsIdForVenue(caseData.getProcessingVenue())
            .orElse(null);

        Map<String,List<String>> multipleHearingLocations = referenceDataServiceHolder.getMultipleHearingLocations();

        return multipleHearingLocations.values().stream()
            .filter(listValues ->  listValues.contains(epimsId))
            .findFirst()
            .orElseGet(() -> Collections.singletonList(epimsId))
            .stream().map(epims -> HearingLocation.builder().locationId(epims).locationType(COURT).build())
            .collect(Collectors.toCollection(ArrayList::new));
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


    public static boolean isCaseLinked(@Valid SscsCaseData caseData) {
        return isNotEmpty(caseData.getLinkedCase());
    }

    public static boolean isPoOfficerAttending(@Valid SscsCaseData caseData) {
        OverrideFields overrideFields = OverridesMapping.getOverrideFields(caseData);
        if (nonNull(overrideFields.getPoToAttend())) {
            return isYes(overrideFields.getPoToAttend());
        }

        return isYes(caseData.getDwpIsOfficerAttending());
    }
}
