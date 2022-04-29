package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.OrganisationDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PartyDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.UnavailabilityDayOfWeek;
import uk.gov.hmcts.reform.sscs.model.single.hearing.UnavailabilityRange;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class HearingsPartiesMappingTest extends HearingsMappingBase {

    @DisplayName("When a valid hearing wrapper without OtherParties or joint party is given buildHearingPartiesDetails returns the correct Hearing Parties Details")
    @Test
    void buildHearingPartiesDetails() {
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
        SscsCaseData caseData = SscsCaseData.builder()
                .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder().build())
                        .appellant(Appellant.builder()
                                .id(appellantId)
                                .name(Name.builder()
                                        .title("title")
                                        .firstName("first")
                                        .lastName("last")
                                        .build())
                                .appointee(appointee)
                                .build())
                        .build())
                .build();
        HearingWrapper wrapper = HearingWrapper.builder()
                .caseData(caseData)
                .caseData(caseData)
                .build();

        List<PartyDetails> partiesDetails = HearingsPartiesMapping.buildHearingPartiesDetails(wrapper);

        PartyDetails partyDetails = partiesDetails.stream().filter(o -> appellantId.equalsIgnoreCase(o.getPartyID())).findFirst().orElse(null);
        PartyDetails appointeeDetails = partiesDetails.stream().filter(o -> appointeeId.equalsIgnoreCase(o.getPartyID())).findFirst().orElse(null);
        appointeeAssertions(appointeeDetails);
        partyAssertions(partyDetails);

        assertThat(partiesDetails.stream().filter(o -> "DWP".equalsIgnoreCase(o.getPartyID())).findFirst()).isNotPresent();
    }

    private void partyAssertions(PartyDetails partyDetails) {
        assertThat(partyDetails).isNotNull();
        assertThat(partyDetails.getPartyType()).isNotNull();
        assertThat(partyDetails.getPartyRole()).isNotNull();
        assertThat(partyDetails.getIndividualDetails()).isNotNull();
        assertThat(partyDetails.getOrganisationDetails()).isNull();
        assertThat(partyDetails.getUnavailabilityDayOfWeek()).isNull();
        assertThat(partyDetails.getUnavailabilityRanges()).isNull();
    }

    private void appointeeAssertions(PartyDetails appointeeDetails) {
        assertThat(appointeeDetails).isNotNull();
        assertThat(appointeeDetails.getPartyType()).isNotNull();
        assertThat(appointeeDetails.getPartyRole()).isNotNull();
        assertThat(appointeeDetails.getIndividualDetails()).isNotNull();
        assertThat(appointeeDetails.getOrganisationDetails()).isNull();
        assertThat(appointeeDetails.getUnavailabilityDayOfWeek()).isNull();
        assertThat(appointeeDetails.getUnavailabilityRanges()).isNull();
    }

    @DisplayName("When a valid hearing wrapper when PO attending is given buildHearingPartiesDetails returns the correct Hearing Parties Details")
    @Test
    void buildHearingPartiesDetailsPoAttending() {
        String appellantId = "1";
        SscsCaseData caseData = SscsCaseData.builder()
                .dwpIsOfficerAttending("Yes")
                .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder().build())
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

        List<PartyDetails> partiesDetails = HearingsPartiesMapping.buildHearingPartiesDetails(wrapper);

        assertThat(partiesDetails.stream().filter(o -> appellantId.equalsIgnoreCase(o.getPartyID())).findFirst()).isPresent();

        PartyDetails dwpPartyDetails = partiesDetails.stream().filter(o -> "DWP".equalsIgnoreCase(o.getPartyID())).findFirst().orElse(null);
        assertThat(dwpPartyDetails).isNotNull();
        assertThat(dwpPartyDetails.getPartyType()).isNotNull();
        assertThat(dwpPartyDetails.getPartyRole()).isNotNull();
        assertThat(dwpPartyDetails.getIndividualDetails()).isNull();
        assertThat(dwpPartyDetails.getOrganisationDetails()).isNotNull();
        assertThat(dwpPartyDetails.getUnavailabilityDayOfWeek()).isNull();
        assertThat(dwpPartyDetails.getUnavailabilityRanges()).isNull();
    }

    @DisplayName("When a valid hearing wrapper when PO attending is not Yes given buildHearingPartiesDetails returns the correct Hearing Parties Details")
    @ParameterizedTest
    @ValueSource(strings = {"No"})
    @NullAndEmptySource
    void buildHearingPartiesDetailsPoAttending(String officerAttending) {
        String appellantId = "1";
        SscsCaseData caseData = SscsCaseData.builder()
                .dwpIsOfficerAttending(officerAttending)
                .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder().build())
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

        List<PartyDetails> partiesDetails = HearingsPartiesMapping.buildHearingPartiesDetails(wrapper);

        assertThat(partiesDetails.stream().filter(o -> appellantId.equalsIgnoreCase(o.getPartyID())).findFirst()).isPresent();

        assertThat(partiesDetails.stream().filter(o -> "DWP".equalsIgnoreCase(o.getPartyID())).findFirst()).isNotPresent();
    }

    @DisplayName("When a valid hearing wrapper is given with OtherParties buildHearingPartiesDetails returns the correct Hearing Parties Details")
    @Test
    void buildHearingPartiesDetailsOtherParties() {
        String appellantId = "1";
        String otherPartyId = "2";
        List<CcdValue<OtherParty>> otherParties = new ArrayList<>();
        otherParties.add(new CcdValue<>(OtherParty.builder()
                .hearingOptions(HearingOptions.builder().build())
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
                        .hearingOptions(HearingOptions.builder().build())
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

        List<PartyDetails> partiesDetails = HearingsPartiesMapping.buildHearingPartiesDetails(wrapper);

        assertThat(partiesDetails.stream().filter(o -> appellantId.equalsIgnoreCase(o.getPartyID())).findFirst()).isPresent();

        PartyDetails partyDetails = partiesDetails.stream().filter(o -> otherPartyId.equalsIgnoreCase(o.getPartyID())).findFirst().orElse(null);
        assertThat(partyDetails).isNotNull();
        assertThat(partyDetails.getPartyType()).isNotNull();
        assertThat(partyDetails.getPartyRole()).isNotNull();
        assertThat(partyDetails.getIndividualDetails()).isNotNull();
        assertThat(partyDetails.getOrganisationDetails()).isNull();
        assertThat(partyDetails.getUnavailabilityDayOfWeek()).isNull();
        assertThat(partyDetails.getUnavailabilityRanges()).isNull();

        assertThat(partiesDetails.stream().filter(o -> "DWP".equalsIgnoreCase(o.getPartyID())).findFirst()).isNotPresent();

    }

    @DisplayName("When a valid hearing wrapper with joint party given buildHearingPartiesDetails returns the correct Hearing Parties Details")
    @ParameterizedTest
    @EnumSource(value = YesNo.class)
    @NullSource
    void buildHearingPartiesDetailsJointParty(YesNo jointParty) {
        // TODO SSCS-10378 - Finish Test
        String appellantId = "1";
        SscsCaseData caseData = SscsCaseData.builder()
                .jointParty(JointParty.builder().hasJointParty(jointParty).build())
                .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder().build())
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

        List<PartyDetails> partiesDetails = HearingsPartiesMapping.buildHearingPartiesDetails(wrapper);

        assertThat(partiesDetails.stream().filter(o -> appellantId.equalsIgnoreCase(o.getPartyID())).findFirst()).isPresent();

        assertThat(partiesDetails.stream().filter(o -> "DWP".equalsIgnoreCase(o.getPartyID())).findFirst()).isNotPresent();
    }

    @DisplayName("buildHearingPartiesPartyDetails when Rep is not null Parameterised Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "Yes,true",
        "No,false",
        "null,false",
        ",false",
    }, nullValues = {"null"})
    void buildHearingPartiesPartyDetailsRep(String hasRepresentative, boolean expected) {
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
        HearingOptions hearingOptions = HearingOptions.builder().build();

        List<PartyDetails> partiesDetails = HearingsPartiesMapping.buildHearingPartiesPartyDetails(party, rep, hearingOptions, null, null, appellantId);

        assertThat(partiesDetails.stream().filter(o -> appellantId.equalsIgnoreCase(o.getPartyID())).findFirst()).isPresent();

        PartyDetails repDetails = partiesDetails.stream().filter(o -> repId.equalsIgnoreCase(o.getPartyID())).findFirst().orElse(null);
        if (expected) {
            assertThat(repDetails).isNotNull();
            assertThat(repDetails.getPartyType()).isNotNull();
            assertThat(repDetails.getPartyRole()).isNotNull();
            assertThat(repDetails.getIndividualDetails()).isNotNull();
            assertThat(repDetails.getOrganisationDetails()).isNull();
            assertThat(repDetails.getUnavailabilityDayOfWeek()).isNull();
            assertThat(repDetails.getUnavailabilityRanges()).isNull();
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
    void buildHearingPartiesPartyDetailsAppointeeRepNull() {
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

        List<PartyDetails> partiesDetails = HearingsPartiesMapping.buildHearingPartiesPartyDetails(party, null, hearingOptions, null, null, appellantId);

        PartyDetails partyDetails = partiesDetails.stream().filter(o -> appellantId.equalsIgnoreCase(o.getPartyID())).findFirst().orElse(null);
        assertThat(partyDetails).isNotNull();
        assertThat(partyDetails.getPartyType()).isNotNull();
        assertThat(partyDetails.getPartyRole()).isNotNull();
        assertThat(partyDetails.getIndividualDetails()).isNotNull();
        assertThat(partyDetails.getOrganisationDetails()).isNull();
        assertThat(partyDetails.getUnavailabilityDayOfWeek()).isNull();
        assertThat(partyDetails.getUnavailabilityRanges()).isNull();
    }

    @DisplayName("createHearingPartyDetails Test")
    @Test
    void createHearingPartyDetails() {
        Entity entity = Appellant.builder()
                .id("1")
                .name(Name.builder()
                        .title("title")
                        .firstName("first")
                        .lastName("last")
                        .build())
                .build();
        HearingOptions hearingOptions = HearingOptions.builder().build();
        PartyDetails partyDetails = HearingsPartiesMapping.createHearingPartyDetails(entity, hearingOptions, null, null, "1", "1");

        assertNotNull(partyDetails.getPartyID());
        assertNotNull(partyDetails.getPartyType());
        assertNotNull(partyDetails.getPartyRole());
        assertNotNull(partyDetails.getIndividualDetails());
        assertNull(partyDetails.getOrganisationDetails());
        assertNull(partyDetails.getUnavailabilityDayOfWeek());
        assertNull(partyDetails.getUnavailabilityRanges());
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
        "test,ORG",
        "null,IND",
    }, nullValues = {"null"})
    void getPartyType(String value, String expected) {
        Entity entity = Appellant.builder().organisation(value).build();
        String result = HearingsPartiesMapping.getPartyType(entity);

        assertEquals(expected, result);
    }

    @DisplayName("getPartyRole with OtherParty Test")
    @Test
    void getPartyRoleOtherParty() {
        String result = HearingsPartiesMapping.getPartyRole(OtherParty.builder().build());

        assertThat(result).isEqualTo("BBA3-otherParty");
    }

    @DisplayName("getPartyRole with Appellant Test")
    @Test
    void getPartyRoleAppellant() {
        String result = HearingsPartiesMapping.getPartyRole(Appellant.builder().build());

        assertThat(result).isEqualTo("BBA3-appellant");
    }

    @DisplayName("getPartyRole with Appointee Test")
    @Test
    void getPartyRoleAppointee() {
        String result = HearingsPartiesMapping.getPartyRole(Appointee.builder().build());

        assertThat(result).isEqualTo("BBA3-appointee");
    }

    @DisplayName("getPartyRole with Representative Test")
    @Test
    void getPartyRoleRepresentative() {
        String result = HearingsPartiesMapping.getPartyRole(Representative.builder().build());

        assertThat(result).isEqualTo("BBA3-representative");
    }

    @DisplayName("getIndividualTitle Parameterised Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "test,test",
        "null,null",
    }, nullValues = {"null"})
    void getIndividualTitle(String value, String expected) {
        Entity entity = Appellant.builder().name(Name.builder().title(value).build()).build();
        String result = HearingsPartiesMapping.getIndividualTitle(entity);

        assertEquals(expected, result);
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

    @DisplayName("getIndividualPreferredHearingChannel Test")
    @Test
    void getIndividualPreferredHearingChannel() {
        // TODO Finish Test when method done
        String hearingType = "HearingType";
        HearingSubtype hearingSubtype = HearingSubtype.builder().build();
        String result = HearingsPartiesMapping.getIndividualPreferredHearingChannel(hearingType, hearingSubtype);

        assertNull(result);
    }

    @DisplayName("getIndividualInterpreterLanguage Test")
    @Test
    void getIndividualInterpreterLanguage() {
        // TODO Finish Test when method done
        HearingOptions hearingOptions = HearingOptions.builder().build();
        String result = HearingsPartiesMapping.getIndividualInterpreterLanguage(hearingOptions);

        assertNull(result);
    }

    @DisplayName("getIndividualReasonableAdjustments Test")
    @Test
    void getIndividualReasonableAdjustments() {
        // TODO Finish Test when method done
        HearingOptions hearingOptions = HearingOptions.builder().build();
        List<String> individualReasonableAdjustments = HearingsPartiesMapping.getIndividualReasonableAdjustments(hearingOptions);

        assertThat(individualReasonableAdjustments).isEmpty();
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

    @DisplayName("getIndividualHearingChannelEmail Parameterised Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "test@test.com,test@test.com",
        "null,''",
        "'',''",
    }, nullValues = {"null"})
    void getIndividualHearingChannelEmail(String value, String expected) {
        Contact contact = null;
        if (nonNull(value)) {
            contact = Contact.builder().email(value).build();
        }
        Entity entity = Appellant.builder().contact(contact).build();

        List<String> result = HearingsPartiesMapping.getIndividualHearingChannelEmail(entity);

        assertThat(result).isEqualTo(splitCsvParamArray(expected));
    }

    @DisplayName("getIndividualHearingChannelPhone Parameterised Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "01000000000,02000000000,01000000000|02000000000",
        "01000000000,null,01000000000",
        "null,02000000000,02000000000",
        "null,null,''",
        "01000000000,,01000000000",
        "'',02000000000,02000000000",
        "'','',''",
    }, nullValues = {"null"})
    void getIndividualHearingChannelPhone(String mobile, String phone, String expected) {
        Contact contact = Contact.builder().mobile(mobile).phone(phone).build();

        Entity entity = Appellant.builder().contact(contact).build();

        List<String> result = HearingsPartiesMapping.getIndividualHearingChannelPhone(entity);

        assertThat(result).isEqualTo(splitCsvParamArray(expected));
    }

    @DisplayName("getIndividualRelatedParties Test")
    @Test
    void getIndividualRelatedParties() {
        Entity entity = Representative.builder().build();

        List<uk.gov.hmcts.reform.sscs.model.single.hearing.RelatedParty> result = HearingsPartiesMapping.getIndividualRelatedParties(entity, "1", "2");

        assertThat(result)
                .isNotEmpty()
                .extracting("relatedPartyId","relationshipType")
                .contains(tuple("1","Representative"));
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

        assertNull(result);
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
                .extracting("unavailableFromDate", "unavailableToDate")
                .contains(
                        tuple(LocalDate.of(2022,2,1),
                                LocalDate.of(2022,3,31)),
                        tuple(LocalDate.of(2022,6,1),
                                LocalDate.of(2022,6,2)));
    }

    @DisplayName("When null ExcludeDates is given getPartyUnavailabilityRange returns null")
    @Test
    void getPartyUnavailabilityRangeNullValue() {
        HearingOptions hearingOptions = HearingOptions.builder().build();
        List<UnavailabilityRange> result = HearingsPartiesMapping.getPartyUnavailabilityRange(hearingOptions);

        assertNull(result);
    }
}
