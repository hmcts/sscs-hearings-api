package uk.gov.hmcts.reform.sscs.helper;

import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.*;
import uk.gov.hmcts.reform.sscs.model.single.hearing.IndividualDetails.IndividualDetailsBuilder;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PartyDetails.PartyDetailsBuilder;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RelatedParty;
import uk.gov.hmcts.reform.sscs.model.single.hearing.UnavailabilityRange.UnavailabilityRangeBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.model.single.hearing.PartyType.IND;
import static uk.gov.hmcts.reform.sscs.model.single.hearing.PartyType.ORG;

@SuppressWarnings({"PMD.UnnecessaryLocalBeforeReturn","PMB.LawOfDemeter","PMD.ReturnEmptyCollectionRatherThanNull"})
// TODO Unsuppress in future
public final class HearingsPartiesMapping {

    public static final String OTHER_PARTY = "OtherParty";
    public static final String REPRESENTATIVE = "Representative";
    public static final String APPOINTEE = "Appointee";
    public static final String APPELLANT = "Appellant";

    private HearingsPartiesMapping() {

    }

    public static List<PartyDetails> buildHearingPartiesDetails(HearingWrapper wrapper) {
        List<PartyDetails> partiesDetails = new ArrayList<>();

        Appeal appeal = wrapper.getUpdatedCaseData().getAppeal();
        Appellant appellant = appeal.getAppellant();

        partiesDetails.addAll(buildHearingPartiesPartyDetails(appellant, appeal.getRep(), appeal.getHearingOptions(), appeal.getHearingType(), appeal.getHearingSubtype()));

        List<CcdValue<OtherParty>> otherParties = wrapper.getUpdatedCaseData().getOtherParties();

        if (nonNull(otherParties)) {
            for (CcdValue<OtherParty> ccdOtherParty : otherParties) {
                OtherParty otherParty = ccdOtherParty.getValue();
                partiesDetails.addAll(buildHearingPartiesPartyDetails(otherParty, otherParty.getRep(), otherParty.getHearingOptions(), null, otherParty.getHearingSubtype()));
            }
        }

        return partiesDetails;
    }

    public static List<PartyDetails> buildHearingPartiesPartyDetails(Party party, Representative rep, HearingOptions hearingOptions, String hearingType, HearingSubtype hearingSubtype) {
        List<PartyDetails> partyDetails = new ArrayList<>();
        partyDetails.add(createHearingPartyDetails(party, hearingOptions, hearingType, hearingSubtype));
        if (isYes(party.getIsAppointee()) && nonNull(party.getAppointee())) {
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

    public static String getPartyId(Entity entity) {
        return entity.getId();
    }

    public static String getPartyType(Entity entity) {
        return isNotBlank(entity.getOrganisation()) ? ORG.name() : IND.name();
    }

    public static String getPartyRole(Entity entity) {
        // TODO Lucas - Andrew unsure what this should be
        String role = "";
        if (nonNull(entity.getRole())) {
            role = entity.getRole().getName();
        } else {
            if (entity instanceof Appellant) {
                role = APPELLANT;
            } else if (entity instanceof Appointee) {
                role = APPOINTEE;
            } else if (entity instanceof Representative) {
                role = REPRESENTATIVE;
            } else if (entity instanceof OtherParty) {
                role = OTHER_PARTY;
            }
        }
        return role;
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
        // TODO Depends on SSCS-10273 - Needs to implement for Reference data of valid Hearing Channel codes
        return null;
    }

    public static String getIndividualInterpreterLanguage(HearingOptions hearingOptions) {
        // TODO Depends on SSCS-10273 - Needs to implement for Reference data to convert from SSCS Languages/Sign Languages to Reference languages
        // if (isYes(hearingOptions.getLanguageInterpreter())) {
        //     String signLanguageType = hearingOptions.getSignLanguageType();
        //     String languages = hearingOptions.getLanguages();
        // }
        return null;
    }

    public static List<String> getIndividualReasonableAdjustments(HearingOptions hearingOptions) {
        List<String> hmcArrangements = new ArrayList<>();
        // TODO Needs new ticket - Needs to implement for Reference data to convert from SSCS Arrangements to Reference Arrangements
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

    public static String getIndividualHearingChannelEmail(Entity entity) {
        // TODO Check correct
        if (nonNull(entity.getContact())) {
            return entity.getContact().getEmail();
        }
        return null;
    }

    public static String getIndividualHearingChannelPhone(Entity entity) {
        // TODO Check correct
        String phoneNumber = null;
        if (nonNull(entity.getContact())) {
            phoneNumber = entity.getContact().getMobile();
            if (isBlank(phoneNumber)) {
                phoneNumber = entity.getContact().getPhone();
            }
        }
        return isNotBlank(phoneNumber) ? phoneNumber : null;
    }

    public static List<RelatedParty> getIndividualRelatedParties(Entity entity) {
        // TODO Check correct
        return entity.getRelatedParties().stream()
                .map(o -> RelatedParty.builder()
                        .relatedPartyID(o.getRelatedPartyId())
                        .relationshipType(o.getRelationshipType())
                        .build())
                .collect(Collectors.toList());

    }

    public static OrganisationDetails getPartyOrganisationDetails() {
        // TODO Future Work
        return null;
    }

    public static List<UnavailabilityDayOfWeek> getPartyUnavailabilityDayOfWeek() {
        // Not used as of now
        // TODO Lucas - Check correct
        return null;
    }

    public static List<UnavailabilityRange> getPartyUnavailabilityRange(HearingOptions hearingOptions) {
        if (nonNull(hearingOptions.getExcludeDates())) {
            // TODO Lucas - Check this is correct
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
}


