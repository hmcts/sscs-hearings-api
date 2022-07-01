package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.DayOfWeekUnavailabilityType;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.PartyType;
import uk.gov.hmcts.reform.sscs.model.single.hearing.OrganisationDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PartyDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.UnavailabilityDayOfWeek;
import uk.gov.hmcts.reform.sscs.model.single.hearing.UnavailabilityRange;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel;
import uk.gov.hmcts.reform.sscs.reference.data.model.Language;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingChannelMapping.getIndividualPreferredHearingChannel;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsPartiesMapping.getIndividualInterpreterLanguage;
import static uk.gov.hmcts.reform.sscs.reference.data.model.EntityRoleCode.APPELLANT;
import static uk.gov.hmcts.reform.sscs.reference.data.model.EntityRoleCode.APPOINTEE;
import static uk.gov.hmcts.reform.sscs.reference.data.model.EntityRoleCode.OTHER_PARTY;
import static uk.gov.hmcts.reform.sscs.reference.data.model.EntityRoleCode.REPRESENTATIVE;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel.FACE_TO_FACE;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel.NOT_ATTENDING;

class HearingsPartiesMappingTest extends HearingsMappingBase {

    public static final String EMAIL_ADDRESS = "test@test.com";
    public static final String TELEPHONE_NUMBER = "01000000000";

    @DisplayName("When a valid hearing wrapper without OtherParties or joint party is given buildHearingPartiesDetails returns the correct Hearing Parties Details")
    @Test
    void buildHearingPartiesDetails() throws InvalidMappingException {
        String appellantId = "1";
        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder().wantsToAttend("yes").build())
                        .hearingType("test")
                        .hearingSubtype(HearingSubtype.builder().wantsHearingTypeFaceToFace("yes").build())
                        .appellant(Appellant.builder()
                                       .id(appellantId)
                                       .name(Name.builder()
                                                 .title("title")
                                                 .firstName("first")
                                                 .lastName("last")
                                                 .build())
                                       .build())
                        .build())
            .build();
        HearingWrapper wrapper = HearingWrapper.builder()
            .caseData(caseData)
            .caseData(caseData)
            .build();

        List<PartyDetails> partiesDetails = HearingsPartiesMapping.buildHearingPartiesDetails(wrapper, referenceData);

        PartyDetails partyDetails = partiesDetails.stream().filter(o -> appellantId.equalsIgnoreCase(o.getPartyID())).findFirst().orElse(
            null);
        assertThat(partyDetails).isNotNull();
        assertThat(partyDetails.getPartyType()).isNotNull();
        assertThat(partyDetails.getPartyRole()).isNotNull();
        assertThat(partyDetails.getIndividualDetails()).isNotNull();
        assertThat(partyDetails.getOrganisationDetails()).isNull();
        assertThat(partyDetails.getUnavailabilityDayOfWeek()).isEmpty();
        assertThat(partyDetails.getUnavailabilityRanges()).isEmpty();

        assertThat(partiesDetails.stream().filter(o -> "DWP".equalsIgnoreCase(o.getPartyID())).findFirst()).isNotPresent();
    }

    @DisplayName("When a valid hearing wrapper when PO attending is given buildHearingPartiesDetails returns the correct Hearing Parties Details")
    @Test
    void buildHearingPartiesDetailsPoAttending() throws InvalidMappingException {
        String appellantId = "1";
        SscsCaseData caseData = SscsCaseData.builder()
            .dwpIsOfficerAttending("Yes")
            .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder().wantsToAttend("yes").build())
                        .hearingType("test")
                        .hearingSubtype(HearingSubtype.builder().wantsHearingTypeFaceToFace("yes").build())
                        .appellant(Appellant.builder()
                                       .id(appellantId)
                                       .name(Name.builder()
                                                 .title("title")
                                                 .firstName("first")
                                                 .lastName("last")
                                                 .build())
                                       .build())
                        .build())
            .build();
        HearingWrapper wrapper = HearingWrapper.builder()
            .caseData(caseData)
            .caseData(caseData)
            .build();

        List<PartyDetails> partiesDetails = HearingsPartiesMapping.buildHearingPartiesDetails(wrapper, referenceData);

        assertThat(partiesDetails.stream().filter(o -> appellantId.equalsIgnoreCase(o.getPartyID())).findFirst()).isPresent();

        PartyDetails dwpPartyDetails = partiesDetails.stream().filter(o -> "DWP".equalsIgnoreCase(o.getPartyID())).findFirst().orElse(
            null);
        assertThat(dwpPartyDetails).isNotNull();
        assertThat(dwpPartyDetails.getPartyType()).isNotNull();
        assertThat(dwpPartyDetails.getPartyRole()).isNotNull();
        assertThat(dwpPartyDetails.getIndividualDetails()).isNull();
        assertThat(dwpPartyDetails.getOrganisationDetails()).isNotNull();
        assertThat(dwpPartyDetails.getUnavailabilityDayOfWeek()).isEmpty();
        assertThat(dwpPartyDetails.getUnavailabilityRanges()).isEmpty();
    }

    @DisplayName("When a valid hearing wrapper when PO attending is not Yes given buildHearingPartiesDetails returns the correct Hearing Parties Details")
    @ParameterizedTest
    @ValueSource(strings = {"No"})
    @NullAndEmptySource
    void buildHearingPartiesDetailsPoAttending(String officerAttending) throws InvalidMappingException {
        String appellantId = "1";
        SscsCaseData caseData = SscsCaseData.builder()
            .dwpIsOfficerAttending(officerAttending)
            .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder().wantsToAttend("yes").build())
                        .hearingType("test")
                        .hearingSubtype(HearingSubtype.builder().wantsHearingTypeFaceToFace("yes").build())
                        .appellant(Appellant.builder()
                                       .id(appellantId)
                                       .name(Name.builder()
                                                 .title("title")
                                                 .firstName("first")
                                                 .lastName("last")
                                                 .build())
                                       .build())
                        .build())
            .build();
        HearingWrapper wrapper = HearingWrapper.builder()
            .caseData(caseData)
            .caseData(caseData)
            .build();

        List<PartyDetails> partiesDetails = HearingsPartiesMapping.buildHearingPartiesDetails(wrapper, referenceData);

        assertThat(partiesDetails.stream().filter(o -> appellantId.equalsIgnoreCase(o.getPartyID())).findFirst()).isPresent();

        assertThat(partiesDetails.stream().filter(o -> "DWP".equalsIgnoreCase(o.getPartyID())).findFirst()).isNotPresent();
    }

    @DisplayName("When a valid hearing wrapper is given with OtherParties buildHearingPartiesDetails returns the correct Hearing Parties Details")
    @Test
    void buildHearingPartiesDetailsOtherParties() throws InvalidMappingException {
        String appellantId = "1";
        String otherPartyId = "2";
        List<CcdValue<OtherParty>> otherParties = new ArrayList<>();
        otherParties.add(new CcdValue<>(OtherParty.builder()
                                            .hearingOptions(HearingOptions.builder().wantsToAttend("yes").build())
                                            .hearingSubtype(HearingSubtype.builder().wantsHearingTypeFaceToFace("yes").build())
                                            .id(otherPartyId)
                                            .name(Name.builder()
                                                      .title("title")
                                                      .firstName("first")
                                                      .lastName("last")
                                                      .build())
                                            .build()));
        SscsCaseData caseData = SscsCaseData.builder()
            .otherParties(otherParties)
            .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder().wantsToAttend("yes").build())
                        .hearingType("test")
                        .hearingSubtype(HearingSubtype.builder().wantsHearingTypeFaceToFace("yes").build())
                        .appellant(Appellant.builder()
                                       .id(appellantId)
                                       .name(Name.builder()
                                                 .title("title")
                                                 .firstName("first")
                                                 .lastName("last")
                                                 .build())
                                       .build())
                        .build())
            .build();
        HearingWrapper wrapper = HearingWrapper.builder()
            .caseData(caseData)
            .caseData(caseData)
            .build();

        List<PartyDetails> partiesDetails = HearingsPartiesMapping.buildHearingPartiesDetails(wrapper, referenceData);

        assertThat(partiesDetails.stream().filter(o -> appellantId.equalsIgnoreCase(o.getPartyID())).findFirst()).isPresent();

        PartyDetails partyDetails = partiesDetails.stream().filter(o -> otherPartyId.equalsIgnoreCase(o.getPartyID())).findFirst().orElse(
            null);
        assertThat(partyDetails).isNotNull();
        assertThat(partyDetails.getPartyType()).isNotNull();
        assertThat(partyDetails.getPartyRole()).isNotNull();
        assertThat(partyDetails.getIndividualDetails()).isNotNull();
        assertThat(partyDetails.getOrganisationDetails()).isNull();
        assertThat(partyDetails.getUnavailabilityDayOfWeek()).isEmpty();
        assertThat(partyDetails.getUnavailabilityRanges()).isEmpty();

        assertThat(partiesDetails.stream().filter(o -> "DWP".equalsIgnoreCase(o.getPartyID())).findFirst()).isNotPresent();

    }

    @DisplayName("When a valid hearing wrapper with joint party given buildHearingPartiesDetails returns the correct Hearing Parties Details")
    @ParameterizedTest
    @EnumSource(value = YesNo.class)
    @NullSource
    void buildHearingPartiesDetailsJointParty(YesNo jointParty) throws InvalidMappingException {
        String appellantId = "1";
        String jointPartyId = "2";

        Name name = Name.builder()
            .title("title")
            .firstName("first")
            .lastName("last")
            .build();

        JointParty jointPartyDetails = JointParty.builder().id(jointPartyId)
            .hasJointParty(jointParty)
            .name(name)
            .build();

        SscsCaseData caseData = SscsCaseData.builder()
            .jointParty(jointPartyDetails)
            .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder().wantsToAttend("yes").build())
                        .hearingType("test")
                        .hearingSubtype(HearingSubtype.builder().wantsHearingTypeFaceToFace("yes").build())
                        .appellant(Appellant.builder()
                                       .id(appellantId)
                                       .name(name)
                                       .build())
                        .build())
            .build();

        HearingWrapper wrapper = HearingWrapper.builder()
            .caseData(caseData)
            .build();

        List<PartyDetails> partiesDetails = HearingsPartiesMapping.buildHearingPartiesDetails(wrapper, referenceData);

        assertThat(partiesDetails.stream().filter(o -> appellantId.equalsIgnoreCase(o.getPartyID())).findFirst()).isPresent();
        assertThat(partiesDetails.stream().anyMatch(o -> jointPartyId.equalsIgnoreCase(o.getPartyID())));
        assertThat(partiesDetails.stream().filter(o -> "DWP".equalsIgnoreCase(o.getPartyID())).findFirst()).isNotPresent();
    }

    @DisplayName("When HearingOption is  Null return empty string")
    @Test
    void getIndividualInterpreterLanguageWhenHearingOptionsNull() throws InvalidMappingException {

        String individualInterpreterLanguage = HearingsPartiesMapping.getIndividualInterpreterLanguage(
            null, null, referenceData
        );

        assertThat(individualInterpreterLanguage).isNull();
    }

    @DisplayName("buildHearingPartiesPartyDetails when Appointee is not null Parameterised Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "Yes,true",
        "No,false",
        "null,false",
        ",false",
    }, nullValues = {"null"})
    void buildHearingPartiesPartyDetailsAppointee(String isAppointee, boolean expected) throws InvalidMappingException {
        String appointeeId = "2";
        Appointee appointee = Appointee.builder()
            .id(appointeeId)
            .name(Name.builder()
                      .title("title")
                      .firstName("first")
                      .lastName("last")
                      .build())
            .build();
        String appellantId = "1";
        Party party = Appellant.builder()
            .id(appellantId)
            .isAppointee(isAppointee)
            .name(Name.builder()
                      .title("title")
                      .firstName("first")
                      .lastName("last")
                      .build())
            .appointee(appointee)
            .build();
        HearingOptions hearingOptions = HearingOptions.builder().build();

        List<PartyDetails> partiesDetails = HearingsPartiesMapping.buildHearingPartiesPartyDetails(
            party,
            null,
            hearingOptions,
            HearingSubtype.builder().build(),
            appellantId,
            null, referenceData
        );

        assertThat(partiesDetails.stream().filter(o -> appellantId.equalsIgnoreCase(o.getPartyID())).findFirst()).isPresent();

        PartyDetails appointeeDetails = partiesDetails.stream().filter(o -> appointeeId.equalsIgnoreCase(o.getPartyID())).findFirst().orElse(
            null);
        if (expected) {
            assertThat(appointeeDetails).isNotNull();
            assertThat(appointeeDetails.getPartyType()).isNotNull();
            assertThat(appointeeDetails.getPartyRole()).isNotNull();
            assertThat(appointeeDetails.getIndividualDetails()).isNotNull();
            assertThat(appointeeDetails.getOrganisationDetails()).isNull();
            assertThat(appointeeDetails.getUnavailabilityDayOfWeek()).isEmpty();
            assertThat(appointeeDetails.getUnavailabilityRanges()).isEmpty();
        } else {
            assertThat(appointeeDetails).isNull();
        }
    }


    @DisplayName("buildHearingPartiesPartyDetails when Rep is not null Parameterised Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "Yes,true",
        "No,false",
        "null,false",
        ",false",
    }, nullValues = {"null"})
    void buildHearingPartiesPartyDetailsRep(String hasRepresentative, boolean expected) throws InvalidMappingException {
        String repId = "3";
        Representative rep = Representative.builder()
            .id(repId)
            .hasRepresentative(hasRepresentative)
            .name(Name.builder()
                      .title("title")
                      .firstName("first")
                      .lastName("last")
                      .build())
            .build();

        String appellantId = "1";
        Party party = Appellant.builder()
            .id(appellantId)
            .name(Name.builder()
                      .title("title")
                      .firstName("first")
                      .lastName("last")
                      .build())
            .build();
        HearingOptions hearingOptions = HearingOptions.builder().wantsToAttend("yes").build();
        HearingSubtype hearingSubtype = HearingSubtype.builder().wantsHearingTypeFaceToFace("yes").build();

        List<PartyDetails> partiesDetails = HearingsPartiesMapping.buildHearingPartiesPartyDetails(
            party,
            rep,
            hearingOptions,
            hearingSubtype,
            appellantId,
            null, referenceData
        );

        assertThat(partiesDetails.stream().filter(o -> appellantId.equalsIgnoreCase(o.getPartyID())).findFirst()).isPresent();

        PartyDetails repDetails = partiesDetails.stream().filter(o -> repId.equalsIgnoreCase(o.getPartyID())).findFirst().orElse(
            null);
        if (expected) {
            assertThat(repDetails).isNotNull();
            assertThat(repDetails.getPartyType()).isNotNull();
            assertThat(repDetails.getPartyRole()).isNotNull();
            assertThat(repDetails.getIndividualDetails()).isNotNull();
            assertThat(repDetails.getOrganisationDetails()).isNull();
            assertThat(repDetails.getUnavailabilityDayOfWeek()).isEmpty();
            assertThat(repDetails.getUnavailabilityRanges()).isEmpty();
        } else {
            assertThat(repDetails).isNull();
        }
    }

    @DisplayName("buildHearingPartiesPartyDetails when Appointee and Rep are both null Parameterised Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "Yes,true",
        "No,false",
        "null,false",
        ",false",
    }, nullValues = {"null"})
    void buildHearingPartiesPartyDetailsAppointeeRepNull() throws InvalidMappingException {
        String appellantId = "1";
        Party party = Appellant.builder()
            .id(appellantId)
            .name(Name.builder()
                      .title("title")
                      .firstName("first")
                      .lastName("last")
                      .build())
            .build();
        HearingOptions hearingOptions = HearingOptions.builder().build();

        List<PartyDetails> partiesDetails = HearingsPartiesMapping.buildHearingPartiesPartyDetails(
            party,
            null,
            hearingOptions,
            HearingSubtype.builder().build(),
            appellantId,
            null, referenceData
        );

        PartyDetails partyDetails = partiesDetails.stream().filter(o -> appellantId.equalsIgnoreCase(o.getPartyID())).findFirst().orElse(
            null);
        assertThat(partyDetails).isNotNull();
        assertThat(partyDetails.getPartyType()).isNotNull();
        assertThat(partyDetails.getPartyRole()).isNotNull();
        assertThat(partyDetails.getIndividualDetails()).isNotNull();
        assertThat(partyDetails.getOrganisationDetails()).isNull();
        assertThat(partyDetails.getUnavailabilityDayOfWeek()).isEmpty();
        assertThat(partyDetails.getUnavailabilityRanges()).isEmpty();
    }

    @DisplayName("createHearingPartyDetails Test")
    @Test
    void createHearingPartyDetails() throws InvalidMappingException {
        Entity entity = Appellant.builder()
            .id("1")
            .name(Name.builder()
                      .title("title")
                      .firstName("first")
                      .lastName("last")
                      .build())
            .build();
        HearingOptions hearingOptions = HearingOptions.builder().build();
        PartyDetails partyDetails = HearingsPartiesMapping.createHearingPartyDetails(
            entity,
            hearingOptions,
            HearingSubtype.builder().build(),
            "1",
            "1",
            null, referenceData
        );

        assertThat(partyDetails.getPartyID()).isNotNull();
        assertThat(partyDetails.getPartyType()).isNotNull();
        assertThat(partyDetails.getPartyRole()).isNotNull();
        assertThat(partyDetails.getIndividualDetails()).isNotNull();
        assertThat(partyDetails.getOrganisationDetails()).isNull();
        assertThat(partyDetails.getUnavailabilityDayOfWeek()).isEmpty();
        assertThat(partyDetails.getUnavailabilityRanges()).isEmpty();
    }

    @DisplayName("getPartyId Parameterised Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "1,1",
        "null,null",
    }, nullValues = {"null"})
    void getPartyId(String value, String expected) {
        Entity entity = Appellant.builder().id(value).build();
        String result = HearingsPartiesMapping.getPartyId(entity);

        assertEquals(expected, result);
    }

    @DisplayName("getPartyType Parameterised Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "test,ORGANISATION",
        "null,INDIVIDUAL",
    }, nullValues = {"null"})
    void getPartyType(String value, PartyType expected) {
        Entity entity = Appellant.builder().organisation(value).build();
        PartyType result = HearingsPartiesMapping.getPartyType(entity);

        assertEquals(expected, result);
    }

    @ParameterizedTest
    @MethodSource("getPartyReferenceArgements")
    void testGetPartyRole(Entity entity, String reference) {
        String result = HearingsPartiesMapping.getPartyRole(entity);

        assertThat(result).isEqualTo(reference);
    }

    @DisplayName("getIndividualFirstName Parameterised Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "test,test",
        "null,null",
    }, nullValues = {"null"})
    void getIndividualFirstName(String value, String expected) {
        Entity entity = Appellant.builder().name(Name.builder().firstName(value).build()).build();
        String result = HearingsPartiesMapping.getIndividualFirstName(entity);

        assertEquals(expected, result);
    }

    @DisplayName("getIndividualLastName Parameterised Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "test,test",
        "null,null",
    }, nullValues = {"null"})
    void getIndividualLastName(String value, String expected) {
        Entity entity = Appellant.builder().name(Name.builder().lastName(value).build()).build();
        String result = HearingsPartiesMapping.getIndividualLastName(entity);

        assertEquals(expected, result);
    }

    @DisplayName("When language passed in should return correct LOV format")
    @ParameterizedTest
    @CsvSource({"Acholi,ach", "Afrikaans,afr", "Akan,aka", "Albanian,alb", "Zaza,zza", "Zulu,zul"})
    void testGetIndividualInterpreterLanguage(String lang, String expected) throws InvalidMappingException {
        given(verbalLanguages.getVerbalLanguage(lang))
            .willReturn(new Language(expected,"Test",null,null,List.of(lang)));

        given(referenceData.getVerbalLanguages()).willReturn(verbalLanguages);

        HearingOptions hearingOptions = HearingOptions.builder()
            .languageInterpreter("Yes")
            .languages(lang)
            .build();

        String result = getIndividualInterpreterLanguage(hearingOptions, null, referenceData);
        assertThat(result).isEqualTo(expected);
    }

    @DisplayName("When a invalid verbal language is given getIndividualInterpreterLanguage should throw the correct error and message")
    @ParameterizedTest
    @ValueSource(strings = {"Test"})
    @NullAndEmptySource
    void testGetIndividualInterpreterLanguage(String value) throws InvalidMappingException {
        given(verbalLanguages.getVerbalLanguage(value))
            .willReturn(null);

        given(referenceData.getVerbalLanguages()).willReturn(verbalLanguages);

        HearingOptions hearingOptions = HearingOptions.builder()
            .languageInterpreter("Yes")
            .languages(value)
            .build();

        assertThatExceptionOfType(InvalidMappingException.class)
            .isThrownBy(() -> getIndividualInterpreterLanguage(hearingOptions, null, referenceData))
            .withMessageContaining("The language %s cannot be mapped", value);
    }

    @DisplayName("When a valid sign language is given getIndividualInterpreterLanguage should return correct hmcReference")
    @ParameterizedTest
    @CsvSource({"American Sign Language (ASL),americanSignLanguage",
        "Hands on signing,handsOnSigning",
        "Deaf Relay,deafRelay",
        "Palantypist / Speech to text,palantypist"})
    void testGetIndividualInterpreterSignLanguage(String signLang, String expected) throws InvalidMappingException {

        given(signLanguages.getSignLanguage(signLang))
            .willReturn(new Language(expected,"Test",null,null,List.of(signLang)));

        given(referenceData.getSignLanguages()).willReturn(signLanguages);

        HearingOptions hearingOptions = HearingOptions.builder()
            .arrangements(List.of("signLanguageInterpreter"))
            .signLanguageType(signLang)
            .build();

        String result = getIndividualInterpreterLanguage(hearingOptions, null, referenceData);
        assertThat(result).isEqualTo(expected);
    }

    @DisplayName("When a invalid sign language is given getIndividualInterpreterLanguage should throw the correct error and message")
    @ParameterizedTest
    @ValueSource(strings = {"Test"})
    @NullAndEmptySource
    void testGetIndividualInterpreterSignLanguage(String value) throws InvalidMappingException {
        given(signLanguages.getSignLanguage(value))
            .willReturn(null);

        given(referenceData.getSignLanguages()).willReturn(signLanguages);

        HearingOptions hearingOptions = HearingOptions.builder()
            .arrangements(List.of("signLanguageInterpreter"))
            .signLanguageType(value)
            .build();

        assertThatExceptionOfType(InvalidMappingException.class)
            .isThrownBy(() -> getIndividualInterpreterLanguage(hearingOptions, null, referenceData))
            .withMessageContaining("The language %s cannot be mapped", value);
    }


    @DisplayName("When hearing type paper then return LOV not attending")
    @Test
    void getIndividualPreferredHearingChannelPaperTest() {
        HearingSubtype hearingSubtype = HearingSubtype.builder().build();
        HearingOptions hearingOptions = HearingOptions.builder().build();

        HearingChannel result = getIndividualPreferredHearingChannel(hearingSubtype, hearingOptions, null);

        assertThat(result).isNotNull();
        assertThat(result.getHmcReference()).isEqualTo(NOT_ATTENDING.getHmcReference());
    }

    @DisplayName("When hearing Subtype and Hearing Options is null return null")
    @Test
    void whenHearingSubtypeAndHearingOptionsIsNull_returnNull() {
        HearingChannel result = getIndividualPreferredHearingChannel(null, null, null);

        assertThat(result).isNull();
    }

    @DisplayName("When hearing type oral and video then return LOV VIDEO")
    @Test
    void getIndividualPreferredHearingChannelOralVideoTest() {
        HearingSubtype hearingSubtype = HearingSubtype.builder().wantsHearingTypeVideo("Yes").hearingVideoEmail(
            "test@email.com").build();
        HearingOptions hearingOptions = HearingOptions.builder().wantsToAttend("yes").build();
        HearingChannel result = getIndividualPreferredHearingChannel(hearingSubtype, hearingOptions, null);
        assertThat(result.getHmcReference()).isEqualTo(HearingChannel.VIDEO.getHmcReference());
    }

    @DisplayName("When hearing type oral and telephone then return LOV TELEPHONE")
    @Test
    void getIndividualPreferredHearingChannelOralTelephoneTest() {
        HearingSubtype hearingSubtype = HearingSubtype.builder().wantsHearingTypeTelephone("Yes").hearingTelephoneNumber(
            "01111234567").build();
        HearingOptions hearingOptions = HearingOptions.builder().wantsToAttend("yes").build();
        HearingChannel result = getIndividualPreferredHearingChannel(hearingSubtype, hearingOptions, null);
        assertThat(result.getHmcReference()).isEqualTo(HearingChannel.TELEPHONE.getHmcReference());
    }

    @DisplayName("When wantsToAttend is yes, and wantsHearingType telephone but hearingTelephoneNumber is not set throw IllegalStateException")
    @Test
    void getIndividualPreferredHearingChannelNullWhenMissingPartialRequirementsTelephoneExample() {
        HearingSubtype hearingSubtype = HearingSubtype.builder().wantsHearingTypeTelephone("Yes").build();
        HearingOptions hearingOptions = HearingOptions.builder().wantsToAttend("yes").build();

        assertThatExceptionOfType(
            IllegalStateException.class).isThrownBy(() ->
                                                        getIndividualPreferredHearingChannel(
                                                            hearingSubtype,
                                                            hearingOptions,
                                                            null));
    }

    @DisplayName("When hearing type oral and face to face then return LOV FACE TO FACE")
    @Test
    void getIndividualPreferredHearingChannelOralFaceToFaceTest() {
        HearingSubtype hearingSubtype = HearingSubtype.builder().wantsHearingTypeFaceToFace("Yes").build();
        HearingOptions hearingOptions = HearingOptions.builder().wantsToAttend("yes").build();
        HearingChannel result = getIndividualPreferredHearingChannel(hearingSubtype, hearingOptions, null);
        assertThat(result).isEqualTo(FACE_TO_FACE);
    }

    @DisplayName("When hearing type is blank and face to face then return LOV not attending")
    @Test
    void getIndividualPreferredHearingChannelBlankFaceToFaceTest() {
        HearingSubtype hearingSubtype = HearingSubtype.builder().wantsHearingTypeFaceToFace("Yes").build();
        HearingOptions hearingOptions = HearingOptions.builder().wantsToAttend("yes").build();
        HearingChannel result = getIndividualPreferredHearingChannel(hearingSubtype, hearingOptions, null);
        assertThat(result).isEqualTo(FACE_TO_FACE);
    }

    @DisplayName("When wantsToAttend is yes, and wantsHearingType video but hearingVideoEmail is not set throw IllegalStateException")
    @Test
    void getIndividualPreferredHearingChannelNullWhenMissingPartialRequirementsVideoExample() {
        HearingSubtype hearingSubtype = HearingSubtype.builder().wantsHearingTypeVideo("Yes").build();
        HearingOptions hearingOptions = HearingOptions.builder().wantsToAttend("yes").build();

        assertThatExceptionOfType(
            IllegalStateException.class).isThrownBy(() ->
                                                        getIndividualPreferredHearingChannel(
                                                            hearingSubtype,
                                                            hearingOptions,
                                                            null));
    }

    @DisplayName("isIndividualVulnerableFlag Test")
    @Test
    void isIndividualVulnerableFlag() {
        // TODO Finish Test when method done
        boolean result = HearingsPartiesMapping.isIndividualVulnerableFlag();

        assertFalse(result);
    }

    @DisplayName("getIndividualVulnerabilityDetails Test")
    @Test
    void getIndividualVulnerabilityDetails() {
        // TODO Finish Test when method done
        String result = HearingsPartiesMapping.getIndividualVulnerabilityDetails();

        assertNull(result);
    }

    @DisplayName("When a hearingVideoEmail has a email, getIndividualHearingChannelEmail "
        + "returns a list with only that email ")
    @Test
    void testGetIndividualHearingChannelEmail() {

        HearingSubtype subtype = HearingSubtype.builder()
            .hearingVideoEmail(EMAIL_ADDRESS)
            .build();

        List<String> result = HearingsPartiesMapping.getIndividualHearingChannelEmail(subtype);

        assertThat(result)
            .hasSize(1)
            .containsOnly(EMAIL_ADDRESS);
    }

    @DisplayName("When a hearingVideoEmail is empty or blank, getIndividualHearingChannelEmail "
        + "returns an empty list")
    @ParameterizedTest
    @NullAndEmptySource
    void testGetIndividualHearingChannelEmail(String value) {

        HearingSubtype subtype = HearingSubtype.builder()
            .hearingVideoEmail(value)
            .build();

        List<String> result = HearingsPartiesMapping.getIndividualHearingChannelEmail(subtype);

        assertThat(result)
            .isEmpty();
    }

    @DisplayName("When a HearingSubtype is null, getIndividualHearingChannelEmail "
        + "returns an empty list")
    @Test
    void testGetIndividualHearingChannelEmailNull() {
        List<String> result = HearingsPartiesMapping.getIndividualHearingChannelEmail(null);

        assertThat(result)
            .isEmpty();
    }

    @DisplayName("When a hearingTelephoneNumber is empty or blank, getIndividualHearingChannelPhone "
        + "returns an empty list")
    @Test
    void testGetIndividualHearingChannelPhone() {
        HearingSubtype subtype = HearingSubtype.builder()
            .hearingTelephoneNumber(TELEPHONE_NUMBER)
            .build();

        List<String> result = HearingsPartiesMapping.getIndividualHearingChannelPhone(subtype);

        assertThat(result)
            .hasSize(1)
            .containsOnly(TELEPHONE_NUMBER);
    }

    @DisplayName("When a hearingTelephoneNumber is empty or blank, getIndividualHearingChannelPhone "
        + "returns an empty list")
    @ParameterizedTest
    @NullAndEmptySource
    void testGetIndividualHearingChannelPhone(String value) {
        HearingSubtype subtype = HearingSubtype.builder()
            .hearingTelephoneNumber(value)
            .build();

        List<String> result = HearingsPartiesMapping.getIndividualHearingChannelPhone(subtype);

        assertThat(result)
            .isEmpty();
    }

    @DisplayName("When a HearingSubtype is null, getIndividualHearingChannelPhone "
        + "returns an empty list")
    @Test
    void testGetIndividualHearingChannelPhoneNull() {
        List<String> result = HearingsPartiesMapping.getIndividualHearingChannelPhone(null);

        assertThat(result)
            .isEmpty();
    }

    @DisplayName("getIndividualRelatedParties Test")
    @Test
    void getIndividualRelatedParties() {
        Entity entity = Representative.builder().build();

        List<uk.gov.hmcts.reform.sscs.model.single.hearing.RelatedParty> result = HearingsPartiesMapping.getIndividualRelatedParties(
            entity,
            "1",
            "2"
        );

        assertThat(result)
            .isNotEmpty()
            .extracting("relatedPartyId", "relationshipType")
            .contains(tuple("1", REPRESENTATIVE.getHmcReference()));
    }

    @DisplayName("getPartyOrganisationDetails Test")
    @Test
    void getPartyOrganisationDetails() {
        OrganisationDetails result = HearingsPartiesMapping.getPartyOrganisationDetails();

        assertNull(result);
    }

    @DisplayName("getPartyUnavailabilityDayOfWeek Test")
    @Test
    void getPartyUnavailabilityDayOfWeek() {
        List<UnavailabilityDayOfWeek> result = HearingsPartiesMapping.getPartyUnavailabilityDayOfWeek();

        assertThat(result).isEmpty();
    }

    @DisplayName("When Valid DateRanges are given getPartyUnavailabilityRange returns the correct list of Unavailability Ranges")
    @Test
    void getPartyUnavailabilityRange() {
        List<ExcludeDate> excludeDates = new ArrayList<>();
        excludeDates.add(ExcludeDate.builder().value(DateRange.builder()
                                                         .start("2022-02-01")
                                                         .end("2022-03-31")
                                                         .build())
                             .build());
        excludeDates.add(ExcludeDate.builder().value(DateRange.builder()
                                                         .start("2022-06-01")
                                                         .end("2022-06-02")
                                                         .build())
                             .build());
        HearingOptions hearingOptions = HearingOptions.builder().excludeDates(excludeDates).build();
        List<UnavailabilityRange> result = HearingsPartiesMapping.getPartyUnavailabilityRange(hearingOptions);

        assertThat(result)
            .extracting("unavailableFromDate", "unavailableToDate", "unavailabilityType")
            .contains(
                tuple(
                    LocalDate.of(2022, 2, 1),
                    LocalDate.of(2022, 3, 31),
                    DayOfWeekUnavailabilityType.ALL_DAY.getLabel()
                ),
                tuple(
                    LocalDate.of(2022, 6, 1),
                    LocalDate.of(2022, 6, 2),
                    DayOfWeekUnavailabilityType.ALL_DAY.getLabel()
                )
            );
    }

    @DisplayName("When null ExcludeDates is given getPartyUnavailabilityRange returns an empty list")
    @Test
    void getPartyUnavailabilityRangeNullValue() {
        HearingOptions hearingOptions = HearingOptions.builder().build();
        List<UnavailabilityRange> result = HearingsPartiesMapping.getPartyUnavailabilityRange(hearingOptions);

        assertThat(result).isEmpty();
    }

    @DisplayName("Unavailability ranges must be set from the excluded dates. Each range must have an UnavailabilityType.")
    @Test
    void buildHearingPartiesDetails_unavailabilityRanges() throws InvalidMappingException {

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(1);
        String appellantId = "1";
        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder()
                                            .excludeDates(List.of(ExcludeDate.builder()
                                                                      .value(DateRange.builder().start(start.toString())
                                                                                 .end(end.toString()).build()).build()))
                                            .wantsToAttend("yes").build())
                        .hearingType("test")
                        .hearingSubtype(HearingSubtype.builder().wantsHearingTypeFaceToFace("yes").build())
                        .appellant(Appellant.builder()
                                       .id(appellantId)
                                       .name(Name.builder()
                                                 .title("title")
                                                 .firstName("first")
                                                 .lastName("last")
                                                 .build())
                                       .build())
                        .build())
            .build();
        HearingWrapper wrapper = HearingWrapper.builder()
            .caseData(caseData)
            .caseData(caseData)
            .build();

        List<PartyDetails> partiesDetails = HearingsPartiesMapping.buildHearingPartiesDetails(wrapper, referenceData);

        PartyDetails partyDetails = partiesDetails.stream().filter(o -> appellantId.equalsIgnoreCase(o.getPartyID())).findFirst().orElse(
            null);
        assertThat(partyDetails).isNotNull();
        assertThat(partyDetails.getUnavailabilityRanges()).hasSize(1);

        UnavailabilityRange unavailabilityRange = partyDetails.getUnavailabilityRanges().get(0);

        assertThat(unavailabilityRange.getUnavailabilityType()).isEqualTo(DayOfWeekUnavailabilityType.ALL_DAY.getLabel());
        assertThat(unavailabilityRange.getUnavailableFromDate()).isEqualTo(start);
        assertThat(unavailabilityRange.getUnavailableToDate()).isEqualTo(end);
    }

    private static Stream<Arguments> getPartyReferenceArgements() {
        return Stream.of(
            Arguments.of(Representative.builder().build(), REPRESENTATIVE.getHmcReference()),
            Arguments.of(Appellant.builder().build(), APPELLANT.getHmcReference()),
            Arguments.of(Appointee.builder().build(), APPOINTEE.getHmcReference()),
            Arguments.of(OtherParty.builder().build(), OTHER_PARTY.getHmcReference())
        );
    }
}
