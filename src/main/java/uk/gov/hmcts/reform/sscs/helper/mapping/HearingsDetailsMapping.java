package uk.gov.hmcts.reform.sscs.helper.mapping;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.sscs.ccd.domain.AdjournCasePanelMembersExcluded;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.CcdValue;
import uk.gov.hmcts.reform.sscs.ccd.domain.Entity;
import uk.gov.hmcts.reform.sscs.ccd.domain.OtherParty;
import uk.gov.hmcts.reform.sscs.ccd.domain.Party;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.exception.ListingException;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.HearingType;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDetails;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HearingType.SUBSTANTIVE;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingPriority.STANDARD;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingPriority.URGENT;

@SuppressWarnings({"PMD.GodClass"})
@Slf4j
// TODO Unsuppress in future
public final class HearingsDetailsMapping {

    private HearingsDetailsMapping() {

    }

    public static HearingDetails buildHearingDetails(HearingWrapper wrapper,
                                                     ReferenceDataServiceHolder referenceDataServiceHolder)
        throws ListingException {
        SscsCaseData caseData = wrapper.getCaseData();

        boolean autoListed = HearingsAutoListMapping.shouldBeAutoListed(caseData, referenceDataServiceHolder);

        return HearingDetails.builder()
            .autolistFlag(autoListed)
            .hearingType(getHearingType())
            .hearingWindow(HearingsWindowMapping.buildHearingWindow(caseData))
            .duration(HearingsDurationMapping.getHearingDuration(caseData, referenceDataServiceHolder))
            .nonStandardHearingDurationReasons(HearingsDurationMapping.getNonStandardHearingDurationReasons())
            .hearingPriorityType(getHearingPriority(caseData))
            .numberOfPhysicalAttendees(HearingsNumberAttendeesMapping.getNumberOfPhysicalAttendees(caseData, referenceDataServiceHolder))
            .hearingInWelshFlag(shouldBeHearingsInWelshFlag())
            .hearingLocations(HearingsLocationMapping.getHearingLocations(caseData, referenceDataServiceHolder))
            .facilitiesRequired(getFacilitiesRequired())
            .listingComments(getListingComments(caseData))
            .hearingRequester(getHearingRequester())
            .privateHearingRequiredFlag(isPrivateHearingRequired())
            .leadJudgeContractType(getLeadJudgeContractType())
            .panelRequirements(HearingsPanelMapping.getPanelRequirements(caseData, referenceDataServiceHolder))
            .hearingIsLinkedFlag(isCaseLinked(caseData))
            .amendReasonCodes(OverridesMapping.getAmendReasonCodes(caseData))
            .hearingChannels(HearingsChannelMapping.getHearingChannels(caseData, referenceDataServiceHolder))
            .build();
    }

    public static HearingType getHearingType() {
        return SUBSTANTIVE;
    }

    public static boolean isCaseUrgent(@Valid SscsCaseData caseData) {
        return isYes(caseData.getUrgentCase());
    }

    public static String getHearingPriority(SscsCaseData caseData) {
        // urgentCase Should go to top of queue in LA - also consider case created date
        // Flag to Lauren - how  can this be captured in HMC queue?
        // If there's an adjournment - date shouldn't reset - should also go to top priority

        // TODO Adjournment - Check what should be used to check if there is adjournment
        if (isCaseUrgent(caseData) || caseData.getAdjournment().getPanelMembersExcluded() == AdjournCasePanelMembersExcluded.YES) {
            return URGENT.getHmcReference();
        }
        return STANDARD.getHmcReference();
    }

    public static boolean shouldBeHearingsInWelshFlag() {
        // TODO Future Work
        return false;
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
        return isYes(caseData.getDwpIsOfficerAttending());
    }
}
