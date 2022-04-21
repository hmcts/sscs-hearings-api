package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.*;
import uk.gov.hmcts.reform.sscs.model.single.hearing.IndividualDetails.IndividualDetailsBuilder;
import uk.gov.hmcts.reform.sscs.model.single.hearing.OrganisationDetails.OrganisationDetailsBuilder;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PartyDetails.PartyDetailsBuilder;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RelatedParty;
import uk.gov.hmcts.reform.sscs.model.single.hearing.UnavailabilityRange.UnavailabilityRangeBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMapping.*;
import static uk.gov.hmcts.reform.sscs.model.EntityRoleCode.RESPONDENT;
import static uk.gov.hmcts.reform.sscs.model.single.hearing.PartyType.IND;
import static uk.gov.hmcts.reform.sscs.model.single.hearing.PartyType.ORG;

@SuppressWarnings({"PMD.UnnecessaryLocalBeforeReturn","PMB.LawOfDemeter","PMD.ReturnEmptyCollectionRatherThanNull", "PMD.GodClass"})
// TODO Unsuppress in future
public final class HearingsPartiesMapping {

    private HearingsPartiesMapping() {

    }

    public static List<PartyDetails> buildHearingPartiesDetails(HearingWrapper wrapper) {

        SscsCaseData caseData = wrapper.getCaseData();
        Appeal appeal = caseData.getAppeal();
        Appellant appellant = appeal.getAppellant();

        List<PartyDetails> partiesDetails = new ArrayList<>(buildHearingPartiesPartyDetails(
                appellant, appeal.getRep(), appeal.getHearingOptions(), appeal.getHearingType(), appeal.getHearingSubtype()));

        List<CcdValue<OtherParty>> otherParties = caseData.getOtherParties();

        if (nonNull(otherParties)) {
            for (CcdValue<OtherParty> ccdOtherParty : otherParties) {
                OtherParty otherParty = ccdOtherParty.getValue();
                partiesDetails.addAll(buildHearingPartiesPartyDetails(
                        otherParty, otherParty.getRep(), otherParty.getHearingOptions(), null, otherParty.getHearingSubtype()));
            }
        }

        if (isYes(caseData.getDwpIsOfficerAttending())) { // TODO SSCS-10243 - Might need to change
            partiesDetails.add(createDwpPartyDetails());
        }

        if (isYes(caseData.getJointParty())) {
            partiesDetails.add(createJointPartyDetails(caseData));
        }

        return partiesDetails;
    }

    public static List<PartyDetails> buildHearingPartiesPartyDetails(Party party, Representative rep, HearingOptions hearingOptions, String hearingType, HearingSubtype hearingSubtype) {
        List<PartyDetails> partyDetails = new ArrayList<>();
        partyDetails.add(createHearingPartyDetails(party, hearingOptions, hearingType, hearingSubtype));
        if (nonNull(party.getAppointee()) && isYes(party.getIsAppointee())) {
            partyDetails.add(createHearingPartyDetails(party.getAppointee(), hearingOptions, hearingType, hearingSubtype));
        }
        if (nonNull(rep) && isYes(rep.getHasRepresentative())) {
            partyDetails.add(createHearingPartyDetails(rep, hearingOptions, hearingType, hearingSubtype));
        }
        return partyDetails;
    }

    public static PartyDetails createHearingPartyDetails(Entity entity, HearingOptions hearingOptions, String hearingType, HearingSubtype hearingSubtype) {
        PartyDetailsBuilder partyDetails = PartyDetails.builder();

        partyDetails.partyID(getPartyId(entity));
        partyDetails.partyType(getPartyType(entity));
        partyDetails.partyRole(getPartyRole(entity));
        partyDetails.individualDetails(getPartyIndividualDetails(entity, hearingOptions, hearingType, hearingSubtype));
        partyDetails.organisationDetails(getPartyOrganisationDetails());
        partyDetails.unavailabilityDayOfWeek(getPartyUnavailabilityDayOfWeek());
        partyDetails.unavailabilityRanges(getPartyUnavailabilityRange(hearingOptions));

        return partyDetails.build();
    }

    public static PartyDetails createDwpPartyDetails() {
        PartyDetailsBuilder partyDetails = PartyDetails.builder();

        partyDetails.partyID(DWP_ID);
        partyDetails.partyType(ORG.name());
        partyDetails.partyRole(RESPONDENT.getKey()); // TODO Depends on SSCS-10273 - replace with common object
        partyDetails.organisationDetails(getDwpOrganisationDetails());
        partyDetails.unavailabilityDayOfWeek(getDwpUnavailabilityDayOfWeek());
        partyDetails.unavailabilityRanges(getDwpUnavailabilityRange());

        return partyDetails.build();
    }

    public static PartyDetails createJointPartyDetails(SscsCaseData caseData) {
        // TODO SSCS-10378 - Add joint party logic
        return PartyDetails.builder().build();
    }

    public static String getPartyId(Entity entity) {
        return entity.getId();
    }

    public static String getPartyType(Entity entity) {
        return isNotBlank(entity.getOrganisation()) ? ORG.name() : IND.name();
    }

    public static String getPartyRole(Entity entity) {
        return getEntityRoleCode(entity).getKey();
    }

    public static IndividualDetails getPartyIndividualDetails(Entity entity, HearingOptions hearingOptions, String hearingType, HearingSubtype hearingSubtype) {
        IndividualDetailsBuilder individualDetails = IndividualDetails.builder();
        individualDetails.title(getIndividualTitle(entity));
        individualDetails.firstName(getIndividualFirstName(entity));
        individualDetails.lastName(getIndividualLastName(entity));
        individualDetails.preferredHearingChannel(getIndividualPreferredHearingChannel(hearingType, hearingSubtype));
        individualDetails.interpreterLanguage(getIndividualInterpreterLanguage(hearingOptions));
        individualDetails.reasonableAdjustments(getIndividualReasonableAdjustments(hearingOptions));
        individualDetails.vulnerableFlag(isIndividualVulnerableFlag());
        individualDetails.vulnerabilityDetails(getIndividualVulnerabilityDetails());
        individualDetails.hearingChannelEmail(getIndividualHearingChannelEmail(entity));
        individualDetails.hearingChannelPhone(getIndividualHearingChannelPhone(entity));
        individualDetails.relatedParties(getIndividualRelatedParties(entity));
        return individualDetails.build();
    }

    public static String getIndividualTitle(Entity entity) {
        return entity.getName().getTitle();
    }

    public static String getIndividualFirstName(Entity entity) {
        return entity.getName().getFirstName();
    }

    public static String getIndividualLastName(Entity entity) {
        return entity.getName().getLastName();
    }

    public static String getIndividualPreferredHearingChannel(String hearingType, HearingSubtype hearingSubtype) {
            if (hearingType.equals("paper")) {
                return HearingChannel.NOT_ATTENDING.getKey();
            }

            if (isYes(hearingSubtype.getWantsHearingTypeFaceToFace())) {
                return HearingChannel.FACE_TO_FACE.getKey();
            } else if (isYes(hearingSubtype.getWantsHearingTypeVideo())) {
                return HearingChannel.VIDEO.getKey();
            } else if (isYes(hearingSubtype.getWantsHearingTypeTelephone())) {
                return HearingChannel.TELEPHONE.getKey();
            }

            return null;
    }

    public static String getIndividualInterpreterLanguage(HearingOptions hearingOptions) {
        if (hearingOptions.wantsSignLanguageInterpreter()) {
            String signLanguageType = hearingOptions.getSignLanguageType();
            return SignLanguage.getSignLanguageByLanguage(signLanguageType).getKey();
        }
        if (isYes(hearingOptions.getLanguageInterpreter())) {
            String languages = hearingOptions.getLanguages();
            return InterpreterLanguage.getLanguageAndConvert(languages).getKey();
        }
        return null;
    }

    public static List<String> getIndividualReasonableAdjustments(HearingOptions hearingOptions) {
        List<String> hmcArrangements = new ArrayList<>();
        // TODO Andrew Looking into - Needs to implement for Reference data to convert from SSCS Arrangements to Reference Arrangements
        // List<String> sscsArrangements = hearingOptions.getArrangements();
        return hmcArrangements;
    }

    public static boolean isIndividualVulnerableFlag() {
        // TODO Future Work
        return false;
    }

    public static String getIndividualVulnerabilityDetails() {
        // TODO Future Work
        return null;
    }

    public static List<String> getIndividualHearingChannelEmail(Entity entity) {
        List<String> emails = new ArrayList<>();
        if (nonNull(entity.getContact()) && isNotBlank(entity.getContact().getEmail())) {
            emails.add(entity.getContact().getEmail());
        }
        return emails;
    }

    public static List<String> getIndividualHearingChannelPhone(Entity entity) {
        List<String> phoneNumbers = new ArrayList<>();
        if (nonNull(entity.getContact())) {
            if (isNotBlank(entity.getContact().getMobile())) {
                phoneNumbers.add(entity.getContact().getMobile());
            }
            if (isNotBlank(entity.getContact().getPhone())) {
                phoneNumbers.add(entity.getContact().getPhone());
            }
        }
        return phoneNumbers;
    }

    public static List<RelatedParty> getIndividualRelatedParties(Entity entity) {
        return entity.getRelatedParties().stream()
                .map(o -> RelatedParty.builder()
                        .relatedPartyID(o.getRelatedPartyId())
                        .relationshipType(o.getRelationshipType())
                        .build())
                .collect(Collectors.toList());

    }

    public static OrganisationDetails getDwpOrganisationDetails() {
        return getOrganisationDetails(DWP_ID, DWP_ORGANISATION_TYPE, null);
    }

    public static OrganisationDetails getPartyOrganisationDetails() {
        // Not used as of now
        return null;
    }

    public static OrganisationDetails getOrganisationDetails(String name, String type, String id) {
        OrganisationDetailsBuilder organisationDetails = OrganisationDetails.builder();
        organisationDetails.name(name);
        organisationDetails.organisationType(type);
        organisationDetails.cftOrganisationID(id);
        return organisationDetails.build();
    }

    public static List<UnavailabilityDayOfWeek> getPartyUnavailabilityDayOfWeek() {
        // Not used as of now
        return null;
    }

    public static List<UnavailabilityDayOfWeek> getDwpUnavailabilityDayOfWeek() {
        // Not used as of now
        return getPartyUnavailabilityDayOfWeek();
    }

    public static List<UnavailabilityRange> getPartyUnavailabilityRange(HearingOptions hearingOptions) {
        if (nonNull(hearingOptions) && nonNull(hearingOptions.getExcludeDates())) {
            List<UnavailabilityRange> unavailabilityRanges = new ArrayList<>();
            for (ExcludeDate excludeDate : hearingOptions.getExcludeDates()) {
                DateRange dateRange = excludeDate.getValue();
                UnavailabilityRangeBuilder unavailabilityRange = UnavailabilityRange.builder();
                unavailabilityRange.unavailableFromDate(LocalDate.parse(dateRange.getStart()));
                unavailabilityRange.unavailableToDate(LocalDate.parse(dateRange.getEnd()));
                unavailabilityRanges.add(unavailabilityRange.build());
            }
            return unavailabilityRanges;
        } else {
            return null;
        }
    }

    public static List<UnavailabilityRange> getDwpUnavailabilityRange() {
        // Not used as of now
        return getPartyUnavailabilityRange(null);
    }
}


