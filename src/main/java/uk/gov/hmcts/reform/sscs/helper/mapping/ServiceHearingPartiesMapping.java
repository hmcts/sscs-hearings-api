package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.CcdValue;
import uk.gov.hmcts.reform.sscs.ccd.domain.Entity;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingSubtype;
import uk.gov.hmcts.reform.sscs.ccd.domain.OtherParty;
import uk.gov.hmcts.reform.sscs.ccd.domain.Party;
import uk.gov.hmcts.reform.sscs.ccd.domain.Representative;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PartyDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.IndividualDetails;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingChannelMapping.getIndividualPreferredHearingChannel;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMapping.DWP_ID;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.PartyType.ORGANISATION;
import static uk.gov.hmcts.reform.sscs.reference.data.model.EntityRoleCode.RESPONDENT;

@SuppressWarnings("PMD.ExcessiveImports")
public final class ServiceHearingPartiesMapping {

    private ServiceHearingPartiesMapping() {
        throw new IllegalStateException("Utility class");
    }

    public static List<PartyDetails> buildServiceHearingPartiesDetails(SscsCaseData caseData, ReferenceDataServiceHolder referenceData)
            throws InvalidMappingException {

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
                appellant, appeal.getRep(), appeal.getHearingOptions(), appeal.getHearingSubtype(), appellant.getId(), referenceData));

        List<CcdValue<OtherParty>> otherParties = caseData.getOtherParties();

        if (nonNull(otherParties)) {
            for (CcdValue<OtherParty> ccdOtherParty : otherParties) {
                OtherParty otherParty = ccdOtherParty.getValue();
                partiesDetails.addAll(buildServiceHearingPartiesPartyDetails(
                        otherParty, otherParty.getRep(), otherParty.getHearingOptions(), otherParty.getHearingSubtype(), appellant.getId(), referenceData));
            }
        }

        return partiesDetails;
    }

    public static List<PartyDetails> buildServiceHearingPartiesPartyDetails(Party party, Representative rep,
                                                                            HearingOptions hearingOptions,
                                                                            HearingSubtype hearingSubtype, String appellantId,
                                                                            ReferenceDataServiceHolder referenceData)
            throws InvalidMappingException {
        List<PartyDetails> partyDetails = new ArrayList<>();
        partyDetails.add(createHearingPartyDetails(party, hearingOptions, hearingSubtype, party.getId(), appellantId, referenceData));
        if (nonNull(party.getAppointee()) && isYes(party.getIsAppointee())) {
            partyDetails.add(createHearingPartyDetails(party.getAppointee(), hearingOptions, hearingSubtype, party.getId(), appellantId, referenceData));
        }
        if (nonNull(rep) && isYes(rep.getHasRepresentative())) {
            partyDetails.add(createHearingPartyDetails(rep, hearingOptions, hearingSubtype, party.getId(), appellantId, referenceData));
        }
        return partyDetails;
    }

    public static PartyDetails createHearingPartyDetails(Entity entity, HearingOptions hearingOptions,
                                                         HearingSubtype hearingSubtype,
                                                         String partyId, String appellantId,
                                                         ReferenceDataServiceHolder referenceData)
            throws InvalidMappingException {
        PartyDetails.PartyDetailsBuilder partyDetails = PartyDetails.builder();

        partyDetails.partyID(HearingsPartiesMapping.getPartyId(entity));
        partyDetails.partyType(HearingsPartiesMapping.getPartyType(entity));
        partyDetails.partyRole(HearingsPartiesMapping.getPartyRole(entity));
        partyDetails.partyName(HearingsPartiesMapping.getIndividualFullName(entity));
        partyDetails.individualDetails(getPartyIndividualDetails(entity, hearingOptions, hearingSubtype, partyId, appellantId, referenceData));
        partyDetails.partyChannel(getIndividualPreferredHearingChannel(hearingSubtype, hearingOptions).getHmcReference());
        partyDetails.organisationDetails(HearingsPartiesMapping.getPartyOrganisationDetails());
        partyDetails.unavailabilityDow(HearingsPartiesMapping.getPartyUnavailabilityDayOfWeek());
        partyDetails.unavailabilityRanges(HearingsPartiesMapping.getPartyUnavailabilityRange(hearingOptions));

        return partyDetails.build();
    }

    public static PartyDetails createDwpPartyDetails() {
        PartyDetails.PartyDetailsBuilder partyDetails = PartyDetails.builder();

        partyDetails.partyID(DWP_ID);
        partyDetails.partyType(ORGANISATION);
        partyDetails.partyRole(RESPONDENT.getHmcReference());
        partyDetails.organisationDetails(HearingsPartiesMapping.getDwpOrganisationDetails());
        partyDetails.unavailabilityDow(HearingsPartiesMapping.getDwpUnavailabilityDayOfWeek());
        partyDetails.unavailabilityRanges(null);

        return partyDetails.build();
    }

    public static PartyDetails createJointPartyDetails() {
        // TODO SSCS-10378 - Add joint party logic using caseData or referenceData
        return PartyDetails.builder().build();
    }

    public static IndividualDetails getPartyIndividualDetails(Entity entity, HearingOptions hearingOptions,
                                                              HearingSubtype hearingSubtype,
                                                              String partyId, String appellantId,
                                                              ReferenceDataServiceHolder referenceData)
            throws InvalidMappingException {
        return IndividualDetails.builder()
                .firstName(HearingsPartiesMapping.getIndividualFirstName(entity))
                .lastName(HearingsPartiesMapping.getIndividualLastName(entity))
                .preferredHearingChannel(getIndividualPreferredHearingChannel(hearingSubtype, hearingOptions).getHmcReference())
                .interpreterLanguage(HearingsPartiesMapping.getIndividualInterpreterLanguage(hearingOptions, referenceData))
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
}
