package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.CcdValue;
import uk.gov.hmcts.reform.sscs.ccd.domain.Entity;
import uk.gov.hmcts.reform.sscs.ccd.domain.ExcludeDate;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingSubtype;
import uk.gov.hmcts.reform.sscs.ccd.domain.OtherParty;
import uk.gov.hmcts.reform.sscs.ccd.domain.Party;
import uk.gov.hmcts.reform.sscs.ccd.domain.Representative;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.PartyType;
import uk.gov.hmcts.reform.sscs.model.single.hearing.IndividualDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.OrganisationDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PartyDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RelatedParty;
import uk.gov.hmcts.reform.sscs.model.single.hearing.UnavailabilityDayOfWeek;
import uk.gov.hmcts.reform.sscs.model.single.hearing.UnavailabilityRange;
import uk.gov.hmcts.reform.sscs.reference.data.model.EntityRoleCode;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingChannelMapping.getIndividualPreferredHearingChannel;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMapping.DWP_ID;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMapping.DWP_ORGANISATION_TYPE;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMapping.getEntityRoleCode;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.DayOfWeekUnavailabilityType.ALL_DAY;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.PartyType.INDIVIDUAL;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.PartyType.ORGANISATION;
import static uk.gov.hmcts.reform.sscs.reference.data.model.EntityRoleCode.REPRESENTATIVE;
import static uk.gov.hmcts.reform.sscs.reference.data.model.EntityRoleCode.RESPONDENT;

@SuppressWarnings({"PMD.GodClass", "PMD.ExcessiveImports", "PMD.TooManyMethods"})
// TODO Unsuppress in future
public final class HearingsPartiesMapping {

    /**
     * According to task <a href ="https://tools.hmcts.net/jira/secure/RapidBoard.jspa?rapidView=2115&projectKey=SSCS&view=detail&selectedIssue=SSCS-10747">SSCS-10747</a>
     * for representatives it is asked to return solicitor code as the relationship type
     * which is 11 that is why this constant is defined.
     */
    public static final String REPRESENTATIVE_RELATION_TYPE_SOLICITOR_CODE = "11";

    private HearingsPartiesMapping() {

    }

    public static List<PartyDetails> buildHearingPartiesDetails(HearingWrapper wrapper,
                                                                ReferenceDataServiceHolder referenceDataServiceHolder)
            throws InvalidMappingException {
        return buildHearingPartiesDetails(wrapper.getCaseData(), referenceDataServiceHolder);
    }

    public static List<PartyDetails> buildHearingPartiesDetails(SscsCaseData caseData,
                                                                ReferenceDataServiceHolder referenceDataServiceHolder)
            throws InvalidMappingException {

        Appeal appeal = caseData.getAppeal();
        Appellant appellant = appeal.getAppellant();

        List<PartyDetails> partiesDetails = new ArrayList<>();

        if (isYes(caseData.getDwpIsOfficerAttending())) { // TODO SSCS-10243 - Might need to change
            partiesDetails.add(createDwpPartyDetails());
        }

        if (isYes(caseData.getJointParty().getHasJointParty())) {
            partiesDetails.addAll(
                buildHearingPartiesPartyDetails(
                    caseData.getJointParty(),
                    appellant.getId(),
                    referenceDataServiceHolder
                ));
        }

        partiesDetails.addAll(buildHearingPartiesPartyDetails(
                appellant, appeal.getRep(), appeal.getHearingOptions(),
                appeal.getHearingSubtype(), appellant.getId(), referenceDataServiceHolder));

        List<CcdValue<OtherParty>> otherParties = caseData.getOtherParties();

        if (nonNull(otherParties)) {
            for (CcdValue<OtherParty> ccdOtherParty : otherParties) {
                OtherParty otherParty = ccdOtherParty.getValue();
                partiesDetails.addAll(buildHearingPartiesPartyDetails(
                        otherParty, otherParty.getRep(), otherParty.getHearingOptions(),
                        otherParty.getHearingSubtype(), appellant.getId(), referenceDataServiceHolder));
            }
        }

        return partiesDetails;
    }

    public static List<PartyDetails> buildHearingPartiesPartyDetails(Party party, String appellantId, ReferenceDataServiceHolder referenceData) throws InvalidMappingException {
        return buildHearingPartiesPartyDetails(party, null, null, null, appellantId, referenceData);
    }

    public static List<PartyDetails> buildHearingPartiesPartyDetails(Party party, Representative rep, HearingOptions hearingOptions,
                                                                     HearingSubtype hearingSubtype,
                                                                     String appellantId, ReferenceDataServiceHolder referenceData)
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
                                                         String partyId, String appellantId, ReferenceDataServiceHolder referenceData)
            throws InvalidMappingException {
        PartyDetails.PartyDetailsBuilder partyDetails = PartyDetails.builder();

        partyDetails.partyID(getPartyId(entity));
        partyDetails.partyType(getPartyType(entity));
        partyDetails.partyRole(getPartyRole(entity));
        partyDetails.individualDetails(getPartyIndividualDetails(entity, hearingOptions, hearingSubtype, partyId, appellantId, referenceData));
        partyDetails.partyChannelSubType(getPartyChannelSubType());
        partyDetails.unavailabilityDayOfWeek(getPartyUnavailabilityDayOfWeek());
        partyDetails.unavailabilityRanges(getPartyUnavailabilityRange(hearingOptions));

        return partyDetails.build();
    }

    public static PartyDetails createDwpPartyDetails() {
        PartyDetails.PartyDetailsBuilder partyDetails = PartyDetails.builder();

        partyDetails.partyID(DWP_ID);
        partyDetails.partyType(ORGANISATION);
        partyDetails.partyRole(RESPONDENT.getHmcReference());
        partyDetails.organisationDetails(getDwpOrganisationDetails());
        partyDetails.unavailabilityDayOfWeek(getDwpUnavailabilityDayOfWeek());
        partyDetails.unavailabilityRanges(getPartyUnavailabilityRange(null));

        return partyDetails.build();
    }

    public static PartyDetails createJointPartyDetails(SscsCaseData caseData) {
        // TODO SSCS-10378 - Add joint party logic
        return PartyDetails.builder().build();
    }

    public static String getPartyId(Entity entity) {
        return entity.getId();
    }

    public static PartyType getPartyType(Entity entity) {
        return isBlank(entity.getOrganisation()) || (entity instanceof Representative) ? INDIVIDUAL : ORGANISATION;
    }

    public static String getPartyRole(Entity entity) {
        return getEntityRoleCode(entity).getHmcReference();
    }

    public static IndividualDetails getPartyIndividualDetails(Entity entity, HearingOptions hearingOptions,
                                                              HearingSubtype hearingSubtype,
                                                              String partyId, String appellantId, ReferenceDataServiceHolder referenceData)
            throws InvalidMappingException {

        HearingChannel hearingChannel = getIndividualPreferredHearingChannel(hearingSubtype,
                                                                                       hearingOptions);

        return IndividualDetails.builder()
                .firstName(getIndividualFirstName(entity))
                .lastName(getIndividualLastName(entity))
                .preferredHearingChannel(hearingChannel == null ?  null : hearingChannel.getHmcReference())
                .interpreterLanguage(getIndividualInterpreterLanguage(hearingOptions, referenceData))
                .reasonableAdjustments(HearingsAdjustmentMapping.getIndividualsAdjustments(hearingOptions))
                .vulnerableFlag(isIndividualVulnerableFlag())
                .vulnerabilityDetails(getIndividualVulnerabilityDetails())
                .hearingChannelEmail(getIndividualHearingChannelEmail(hearingSubtype))
                .hearingChannelPhone(getIndividualHearingChannelPhone(hearingSubtype))
                .relatedParties(getIndividualRelatedParties(entity, partyId, appellantId))
                .custodyStatus(getIndividualCustodyStatus())
                .otherReasonableAdjustmentDetails(getIndividualOtherReasonableAdjustmentDetails())
                .build();
    }

    public static String getIndividualFirstName(Entity entity) {
        return entity.getName().getFirstName();
    }

    public static String getIndividualLastName(Entity entity) {
        return entity.getName().getLastName();
    }

    public static String getIndividualFullName(Entity entity) {
        return entity.getName().getFullNameNoTitle();
    }

    public static String getIndividualInterpreterLanguage(HearingOptions hearingOptions, ReferenceDataServiceHolder referenceData) throws InvalidMappingException {

        if (isNull(hearingOptions)) {
            return EMPTY;
        }

        if (isTrue(hearingOptions.wantsSignLanguageInterpreter())) {
            String signLanguage = hearingOptions.getSignLanguageType();
            String signLanguageReference = referenceData.getSignLanguages().getSignLanguageReference(signLanguage);
            if (isNull(signLanguageReference)) {
                throw new InvalidMappingException(String.format("The language %s cannot be mapped", signLanguage));
            }
            return signLanguageReference;
        }
        if (isYes(hearingOptions.getLanguageInterpreter())) {
            String verbalLanguage = hearingOptions.getLanguages();
            String verbalLanguageReference = referenceData.getVerbalLanguages().getVerbalLanguageReference(verbalLanguage);
            if (isNull(verbalLanguageReference)) {
                throw new InvalidMappingException(String.format("The language %s cannot be mapped", verbalLanguage));
            }
            return verbalLanguageReference;
        }
        return null;
    }

    public static boolean isIndividualVulnerableFlag() {
        // TODO Future Work
        return false;
    }

    public static String getIndividualVulnerabilityDetails() {
        // TODO Future Work
        return null;
    }

    public static List<String> getIndividualHearingChannelEmail(HearingSubtype hearingSubtype) {
        List<String> emails = new ArrayList<>();
        if (nonNull(hearingSubtype) && isNotBlank(hearingSubtype.getHearingVideoEmail())) {
            emails.add(hearingSubtype.getHearingVideoEmail());
        }
        return emails;
    }

    public static List<String> getIndividualHearingChannelPhone(HearingSubtype hearingSubtype) {
        List<String> phoneNumbers = new ArrayList<>();
        if (nonNull(hearingSubtype) && isNotBlank(hearingSubtype.getHearingTelephoneNumber())) {
            phoneNumbers.add(hearingSubtype.getHearingTelephoneNumber());
        }
        return phoneNumbers;
    }

    public static List<RelatedParty> getIndividualRelatedParties(Entity entity, String partyId, String appellantId) {
        List<RelatedParty> relatedParties = new ArrayList<>();
        EntityRoleCode roleCode = getEntityRoleCode(entity);
        switch (roleCode) {
            case APPOINTEE:
            case REPRESENTATIVE:
                relatedParties.add(getRelatedParty(partyId, roleCode.getHmcReference()));
                break;
            case OTHER_PARTY:
            case JOINT_PARTY:
                relatedParties.add(getRelatedParty(appellantId, roleCode.getHmcReference()));
                break;
            default:
                break;
        }
        return relatedParties;
    }

    /**
     * Returns related party according to given id and relationship type.
     * According to task <a href ="https://tools.hmcts.net/jira/secure/RapidBoard.jspa?rapidView=2115&projectKey=SSCS&view=detail&selectedIssue=SSCS-10747">SSCS-10747</a>
     * for representatives it is asked to return solicitor code as the relationship type
     * which is 11, REPRESENTATIVE_RELATION_TYPE_SOLICITOR_CODE
     *
     * @param id id of the related party
     * @param relationshipType relationship type of the related party
     * @return RelatedParty details
     */
    public static RelatedParty getRelatedParty(String id, String relationshipType) {
        return RelatedParty.builder()
                .relatedPartyId(id)
                .relationshipType(REPRESENTATIVE.getHmcReference().equals(relationshipType) ? REPRESENTATIVE_RELATION_TYPE_SOLICITOR_CODE : relationshipType)
                .build();
    }

    public static String getIndividualCustodyStatus() {
        // TODO Future work
        return null;
    }

    public static String getIndividualOtherReasonableAdjustmentDetails() {
        // TODO Future work
        return null;
    }

    public static String getPartyChannelSubType() {
        // TODO Future work
        return null;
    }

    public static OrganisationDetails getDwpOrganisationDetails() {
        return getOrganisationDetails(DWP_ID, DWP_ORGANISATION_TYPE, null);
    }

    public static OrganisationDetails getPartyOrganisationDetails() {
        // Not used as of now
        return null;
    }

    public static OrganisationDetails getOrganisationDetails(String name, String type, String id) {
        OrganisationDetails.OrganisationDetailsBuilder organisationDetails = OrganisationDetails.builder();
        organisationDetails.name(name);
        organisationDetails.organisationType(type);
        organisationDetails.cftOrganisationID(id);
        return organisationDetails.build();
    }

    public static List<UnavailabilityDayOfWeek> getPartyUnavailabilityDayOfWeek() {
        // Not used as of now
        return Collections.emptyList();
    }

    public static List<UnavailabilityDayOfWeek> getDwpUnavailabilityDayOfWeek() {
        // Not used as of now
        return getPartyUnavailabilityDayOfWeek();
    }

    public static List<UnavailabilityRange> getPartyUnavailabilityRange(HearingOptions hearingOptions) {
        if (isNull(hearingOptions) || isNull(hearingOptions.getExcludeDates())) {
            return Collections.emptyList();
        }

        return hearingOptions.getExcludeDates().stream()
                .map(ExcludeDate::getValue)
                .map(dateRange -> UnavailabilityRange.builder()
                        .unavailableFromDate(LocalDate.parse(dateRange.getStart()))
                        .unavailableToDate(LocalDate.parse(dateRange.getEnd()))
                        .unavailabilityType(ALL_DAY.getLabel())
                        .build())
                .collect(Collectors.toList());
    }
}


