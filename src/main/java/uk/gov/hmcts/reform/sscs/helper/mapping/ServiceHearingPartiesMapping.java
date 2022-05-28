package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.CcdValue;
import uk.gov.hmcts.reform.sscs.ccd.domain.DateRange;
import uk.gov.hmcts.reform.sscs.ccd.domain.Entity;
import uk.gov.hmcts.reform.sscs.ccd.domain.ExcludeDate;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingSubtype;
import uk.gov.hmcts.reform.sscs.ccd.domain.OtherParty;
import uk.gov.hmcts.reform.sscs.ccd.domain.Party;
import uk.gov.hmcts.reform.sscs.ccd.domain.Representative;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PartyDetails;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.UnavailabilityRange;
import uk.gov.hmcts.reform.sscs.model.single.hearing.IndividualDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.OrganisationDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PartyType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMapping.DWP_ID;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMapping.getEntityRoleCode;
import static uk.gov.hmcts.reform.sscs.model.single.hearing.PartyType.IND;
import static uk.gov.hmcts.reform.sscs.model.single.hearing.PartyType.ORG;
import static uk.gov.hmcts.reform.sscs.reference.data.mappings.EntityRoleCode.RESPONDENT;

@SuppressWarnings("PMD.ExcessiveImports")
public final class ServiceHearingPartiesMapping {

    private ServiceHearingPartiesMapping() {
        throw new IllegalStateException("Utility class");
    }

    public static List<PartyDetails> buildServiceHearingPartiesDetails(SscsCaseData caseData) {

        Appeal appeal = caseData.getAppeal();
        Appellant appellant = appeal.getAppellant();

        List<PartyDetails> partiesDetails = new ArrayList<>();

        if (isYes(caseData.getDwpIsOfficerAttending())) { // TODO SSCS-10243 - Might need to change
            partiesDetails.add(createDwpPartyDetails());
        }

        if (isYes(caseData.getJointParty().getHasJointParty())) {
            partiesDetails.add(createJointPartyDetails());
        }

        partiesDetails.addAll(buildServiceHearingPartiesPartyDetails(
                appellant, appeal.getRep(), appeal.getHearingOptions(), appeal.getHearingType(), appeal.getHearingSubtype(), appellant.getId()));

        List<CcdValue<OtherParty>> otherParties = caseData.getOtherParties();

        if (nonNull(otherParties)) {
            for (CcdValue<OtherParty> ccdOtherParty : otherParties) {
                OtherParty otherParty = ccdOtherParty.getValue();
                partiesDetails.addAll(buildServiceHearingPartiesPartyDetails(
                        otherParty, otherParty.getRep(), otherParty.getHearingOptions(), null, otherParty.getHearingSubtype(), appellant.getId()));
            }
        }

        return partiesDetails;
    }

    public static List<PartyDetails> buildServiceHearingPartiesPartyDetails(Party party, Representative rep, HearingOptions hearingOptions, String hearingType, HearingSubtype hearingSubtype, String appellantId) {
        List<PartyDetails> partyDetails = new ArrayList<>();
        partyDetails.add(createHearingPartyDetails(party, hearingOptions, hearingType, hearingSubtype, party.getId(), appellantId));
        if (nonNull(party.getAppointee()) && isYes(party.getIsAppointee())) {
            partyDetails.add(createHearingPartyDetails(party.getAppointee(), hearingOptions, hearingType, hearingSubtype, party.getId(), appellantId));
        }
        if (nonNull(rep) && isYes(rep.getHasRepresentative())) {
            partyDetails.add(createHearingPartyDetails(rep, hearingOptions, hearingType, hearingSubtype, party.getId(), appellantId));
        }
        return partyDetails;
    }

    public static PartyDetails createHearingPartyDetails(Entity entity, HearingOptions hearingOptions, String hearingType, HearingSubtype hearingSubtype, String partyId, String appellantId) {
        PartyDetails.PartyDetailsBuilder partyDetails = PartyDetails.builder();

        partyDetails.partyID(getPartyId(entity));
        partyDetails.partyType(getPartyType(entity));
        partyDetails.partyRole(getPartyRole(entity));
        partyDetails.partyName(HearingsPartiesMapping.getIndividualFullName(entity));
        partyDetails.individualDetails(getPartyIndividualDetails(entity, hearingOptions, hearingType, hearingSubtype, partyId, appellantId));
        partyDetails.partyChannel(HearingsPartiesMapping.getIndividualPreferredHearingChannel(hearingType, hearingSubtype).orElse(null));
        partyDetails.organisationDetails(HearingsPartiesMapping.getPartyOrganisationDetails());
        partyDetails.unavailabilityDow(null); //TODO Implement later
        partyDetails.unavailabilityRanges(getPartyUnavailabilityRange(hearingOptions));

        return partyDetails.build();
    }

    public static PartyDetails createDwpPartyDetails() {
        PartyDetails.PartyDetailsBuilder partyDetails = PartyDetails.builder();

        partyDetails.partyID(DWP_ID);
        partyDetails.partyType(ORG);
        partyDetails.partyRole(RESPONDENT.getHmcReference());
        partyDetails.organisationDetails(HearingsPartiesMapping.getDwpOrganisationDetails());
        partyDetails.unavailabilityDow(null);
        partyDetails.unavailabilityRanges(null);

        return partyDetails.build();
    }

    public static PartyDetails createJointPartyDetails() {
        // TODO SSCS-10378 - Add joint party logic using caseData or referenceData
        return PartyDetails.builder().build();
    }

    public static IndividualDetails getPartyIndividualDetails(Entity entity, HearingOptions hearingOptions, String hearingType, HearingSubtype hearingSubtype, String partyId, String appellantId) {
        return IndividualDetails.builder()
                .firstName(HearingsPartiesMapping.getIndividualFirstName(entity))
                .lastName(HearingsPartiesMapping.getIndividualLastName(entity))
                .preferredHearingChannel(HearingsPartiesMapping.getIndividualPreferredHearingChannel(hearingType, hearingSubtype).orElse(null))
                .interpreterLanguage(HearingsPartiesMapping.getIndividualInterpreterLanguage(hearingOptions).orElse(null))
                .reasonableAdjustments(HearingsPartiesMapping.getIndividualReasonableAdjustments(hearingOptions))
                .vulnerableFlag(HearingsPartiesMapping.isIndividualVulnerableFlag())
                .vulnerabilityDetails(HearingsPartiesMapping.getIndividualVulnerabilityDetails())
                .hearingChannelEmail(HearingsPartiesMapping.getIndividualHearingChannelEmail(hearingSubtype))
                .hearingChannelPhone(HearingsPartiesMapping.getIndividualHearingChannelPhone(hearingSubtype))
                .relatedParties(HearingsPartiesMapping.getIndividualRelatedParties(entity, partyId, appellantId))
                .custodyStatus(HearingsPartiesMapping.getIndividualCustodyStatus())
                .otherReasonableAdjustmentDetails(HearingsPartiesMapping.getIndividualOtherReasonableAdjustmentDetails())
                .build();
    }

    public static OrganisationDetails getOrganisationDetails(String name, String type, String id) {
        OrganisationDetails.OrganisationDetailsBuilder organisationDetails = OrganisationDetails.builder();
        organisationDetails.name(name);
        organisationDetails.organisationType(type);
        organisationDetails.cftOrganisationID(id);
        return organisationDetails.build();
    }

    public static List<UnavailabilityRange> getPartyUnavailabilityRange(HearingOptions hearingOptions) {
        if (nonNull(hearingOptions) && nonNull(hearingOptions.getExcludeDates())) {
            List<UnavailabilityRange> unavailabilityRanges = new ArrayList<>();
            for (ExcludeDate excludeDate : hearingOptions.getExcludeDates()) {
                DateRange dateRange = excludeDate.getValue();
                UnavailabilityRange.UnavailabilityRangeBuilder unavailabilityRange = UnavailabilityRange.builder();
                unavailabilityRange.unavailableFromDate(dateRange.getStart());
                unavailabilityRange.unavailableToDate(dateRange.getEnd());
                unavailabilityRanges.add(unavailabilityRange.build());
            }
            return unavailabilityRanges;
        }
        return Collections.emptyList();
    }

    public static String getPartyId(Entity entity) {
        return entity.getId();
    }

    public static PartyType getPartyType(Entity entity) {
        return isNotBlank(entity.getOrganisation()) ? ORG : IND;
    }

    public static String getPartyRole(Entity entity) {
        return getEntityRoleCode(entity).getHmcReference();
    }


}
