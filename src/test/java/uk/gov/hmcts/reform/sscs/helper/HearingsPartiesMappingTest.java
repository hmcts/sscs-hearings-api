package uk.gov.hmcts.reform.sscs.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

    @DisplayName("When a valid hearing wrapper is given buildHearingPartiesDetails returns the correct Hearing Parties Details")
    @Test
    void buildHearingPartiesDetails() {
        SscsCaseData caseData = SscsCaseData.builder()
                .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder().build())
                        .appellant(Appellant.builder()
                                .id("1")
                                .name(Name.builder()
                                        .title("title")
                                        .firstName("first")
                                        .lastName("last")
                                        .build())
                                .relatedParties(new ArrayList<>())
                                .build())
                        .build())
                .build();
        HearingWrapper wrapper = HearingWrapper.builder()
                .originalCaseData(caseData)
                .updatedCaseData(caseData)
                .build();

        List<PartyDetails> partiesDetails = HearingsPartiesMapping.buildHearingPartiesDetails(wrapper);

        assertThat(partiesDetails)
                .isNotEmpty()
                .allSatisfy(o -> {
                    assertNotNull(o.getPartyID());
                    assertNotNull(o.getPartyType());
                    assertNotNull(o.getPartyRole());
                    assertNotNull(o.getIndividualDetails());
                    assertNull(o.getOrganisationDetails());
                    assertNull(o.getUnavailabilityDayOfWeek());
                    assertNull(o.getUnavailabilityRanges());
                });
    }

    @DisplayName("buildHearingPartiesPartyDetails Test")
    @Test
    void buildHearingPartiesPartyDetails() {
        // TODO Finish Test when method done
        Party party = Appellant.builder()
                .id("1")
                .name(Name.builder()
                        .title("title")
                        .firstName("first")
                        .lastName("last")
                        .build())
                .relatedParties(new ArrayList<>())
                .build();
        HearingOptions hearingOptions = HearingOptions.builder().build();
        List<PartyDetails> partiesDetails = HearingsPartiesMapping.buildHearingPartiesPartyDetails(party, false, false, null, hearingOptions, null, null);

        assertThat(partiesDetails)
                .isNotEmpty()
                .allSatisfy(o -> {
                    assertNotNull(o.getPartyID());
                    assertNotNull(o.getPartyType());
                    assertNotNull(o.getPartyRole());
                    assertNotNull(o.getIndividualDetails());
                    assertNull(o.getOrganisationDetails());
                    assertNull(o.getUnavailabilityDayOfWeek());
                    assertNull(o.getUnavailabilityRanges());
                });
    }

    @DisplayName("createHearingPartyDetails Test")
    @Test
    void createHearingPartyDetails() {
        // TODO Finish Test when method done
        Entity entity = Appellant.builder()
                .id("1")
                .name(Name.builder()
                        .title("title")
                        .firstName("first")
                        .lastName("last")
                        .build())
                .relatedParties(new ArrayList<>())
                .build();
        HearingOptions hearingOptions = HearingOptions.builder().build();
        PartyDetails partyDetails = HearingsPartiesMapping.createHearingPartyDetails(entity, hearingOptions, null, null);

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
        // TODO Finish Test when method done
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
        // TODO Finish Test when method done
        Entity entity = Appellant.builder().organisation(value).build();
        String result = HearingsPartiesMapping.getPartyType(entity);

        assertEquals(expected, result);
    }

    @DisplayName("getPartyRole Test")
    @Test
    void getPartyRole() {
        // TODO Finish Test when method done
    }

    @DisplayName("getPartyIndividualDetails Test")
    @Test
    void getPartyIndividualDetails() {
        // TODO Finish Test when method done
    }

    @DisplayName("getIndividualTitle Parameterised Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "test,test",
        "null,null",
    }, nullValues = {"null"})
    void getIndividualTitle(String value, String expected) {
        // TODO Finish Test when method done
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
        // TODO Finish Test when method done
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
        // TODO Finish Test when method done
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
        HearingsPartiesMapping.getIndividualReasonableAdjustments(hearingOptions);
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
        "test,test",
        "null,null",
    }, nullValues = {"null"})
    void getIndividualHearingChannelEmail(String value, String expected) {
        // TODO Finish Test when method done
        Contact contact = null;
        if (nonNull(value)) {
            contact = Contact.builder().email(value).build();
        }
        Entity entity = Appellant.builder().contact(contact).build();

        String result = HearingsPartiesMapping.getIndividualHearingChannelEmail(entity);

        assertEquals(expected, result);
    }

    @DisplayName("getIndividualHearingChannelPhone Parameterised Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "01000000000,02000000000,01000000000",
        "01000000000,null,01000000000",
        "null,02000000000,02000000000",
        "null,null,null",
    }, nullValues = {"null"})
    void getIndividualHearingChannelPhone(String mobile, String phone, String expected) {
        // TODO Finish Test when method done
        Contact contact = Contact.builder().mobile(mobile).phone(phone).build();

        Entity entity = Appellant.builder().contact(contact).build();

        String result = HearingsPartiesMapping.getIndividualHearingChannelPhone(entity);

        assertEquals(expected, result);
    }

    @DisplayName("getIndividualRelatedParties Test")
    @Test
    void getIndividualRelatedParties() {
        // TODO Finish Test when method done
        List<RelatedParty> relatedParties = new ArrayList<>();
        relatedParties.add(RelatedParty.builder().relatedPartyId("1").relationshipType("Appellant").build());
        Entity entity = Appellant.builder().relatedParties(relatedParties).build();

        List<uk.gov.hmcts.reform.sscs.model.single.hearing.RelatedParty> result = HearingsPartiesMapping.getIndividualRelatedParties(entity);

        assertThat(result)
                .extracting("relatedPartyID","relationshipType")
                .contains(tuple("1","Appellant"));
    }

    @DisplayName("getPartyOrganisationDetails Test")
    @Test
    void getPartyOrganisationDetails() {
        // TODO Finish Test when method done
        OrganisationDetails result = HearingsPartiesMapping.getPartyOrganisationDetails();

        assertNull(result);
    }

    @DisplayName("getPartyUnavailabilityDayOfWeek Test")
    @Test
    void getPartyUnavailabilityDayOfWeek() {
        // TODO Finish Test when method done
        List<UnavailabilityDayOfWeek> result = HearingsPartiesMapping.getPartyUnavailabilityDayOfWeek();

        assertNull(result);
    }

    @DisplayName("When Valid DateRanges are given getPartyUnavailabilityRange returns the correct list of Unavailability Ranges")
    @Test
    void getPartyUnavailabilityRange() {
        // TODO Finish Test when method done
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
        // TODO Finish Test when method done
        HearingOptions hearingOptions = HearingOptions.builder().build();
        List<UnavailabilityRange> result = HearingsPartiesMapping.getPartyUnavailabilityRange(hearingOptions);

        assertNull(result);
    }
}
