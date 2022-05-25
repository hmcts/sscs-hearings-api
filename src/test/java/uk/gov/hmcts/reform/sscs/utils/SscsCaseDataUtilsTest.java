package uk.gov.hmcts.reform.sscs.utils;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.CcdValue;
import uk.gov.hmcts.reform.sscs.ccd.domain.DateRange;
import uk.gov.hmcts.reform.sscs.ccd.domain.Event;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.ExcludeDate;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingSubtype;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.OtherParty;
import uk.gov.hmcts.reform.sscs.ccd.domain.Representative;
import uk.gov.hmcts.reform.sscs.ccd.domain.Role;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingsPartiesMapping;
import uk.gov.hmcts.reform.sscs.helper.mapping.PartyFlagsMapping;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.CaseFlags;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.HearingWindow;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PartyDetails;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PartyFlags;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.UnavailabilityRange;
import uk.gov.hmcts.reform.sscs.model.single.hearing.OrganisationDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PartyType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PartyFlagsMap.ADJOURN_CASE_INTERPRETER_LANGUAGE;
import static uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PartyFlagsMap.DISABLED_ACCESS;
import static uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PartyFlagsMap.DWP_PHME;
import static uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PartyFlagsMap.DWP_UCB;
import static uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PartyFlagsMap.HEARING_LOOP;
import static uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PartyFlagsMap.IS_CONFIDENTIAL_CASE;
import static uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PartyFlagsMap.SIGN_LANGUAGE_TYPE;
import static uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PartyFlagsMap.URGENT_CASE;
import static uk.gov.hmcts.reform.sscs.reference.data.mappings.HearingChannel.FACE_TO_FACE;

class SscsCaseDataUtilsTest {

    static MockedStatic<HearingsPartiesMapping> hearingsPartiesMapping;

    @BeforeAll
    public static void init() {
        hearingsPartiesMapping = Mockito.mockStatic(HearingsPartiesMapping.class);
    }

    @AfterAll
    public static void close() {
        hearingsPartiesMapping.close();
    }

    @Test
    void shouldGetHearingTypeWhenExists() {
        // given
        SscsCaseData sscsCaseData = Mockito.mock(SscsCaseData.class);
        Appeal appeal = Mockito.mock(Appeal.class);
        // when
        Mockito.when(appeal.getHearingType()).thenReturn("final");
        Mockito.when(sscsCaseData.getAppeal()).thenReturn(appeal);
        //then
        assertEquals("final", SscsCaseDataUtils.getHearingType(sscsCaseData));
    }

    @Test
    void shouldGetEmptyHearingTypeWhenAppealNull() {
        // given
        SscsCaseData sscsCaseData = Mockito.mock(SscsCaseData.class);
        // when
        Mockito.when(sscsCaseData.getAppeal()).thenReturn(null);
        //then
        assertEquals("", SscsCaseDataUtils.getHearingType(sscsCaseData));
    }

    @Test
    void shouldGetIssueCode() {
        // given
        SscsCaseData sscsCaseData = Mockito.mock(SscsCaseData.class);
        // when
        Mockito.when(sscsCaseData.getIssueCode()).thenReturn("DD");
        //then
        assertEquals(1, SscsCaseDataUtils.getIssueCode(sscsCaseData).size());
        assertEquals("DD", SscsCaseDataUtils.getIssueCode(sscsCaseData).stream().findFirst().orElseThrow());
    }

    @Test
    void shouldGetEmptyListWhenIssueCodeNull() {
        // given
        SscsCaseData sscsCaseData = Mockito.mock(SscsCaseData.class);
        // when
        Mockito.when(sscsCaseData.getIssueCode()).thenReturn(null);
        //then
        assertTrue(SscsCaseDataUtils.getIssueCode(sscsCaseData).isEmpty());
    }

    @Test
    void shouldGetFacilitiesRequired() {
        // given
        SscsCaseData sscsCaseData = Mockito.mock(SscsCaseData.class);
        Appeal appeal = Mockito.mock(Appeal.class);
        HearingOptions hearingOptions = Mockito.mock(HearingOptions.class);
        // when
        Mockito.when(hearingOptions.getArrangements()).thenReturn(List.of("signLanguageInterpreter",
                "hearingLoop", "disabledAccess"));
        Mockito.when(appeal.getHearingOptions()).thenReturn(hearingOptions);
        Mockito.when(sscsCaseData.getAppeal()).thenReturn(appeal);
        //then
        List<String> facilitiesRequired = SscsCaseDataUtils.getFacilitiesRequired(sscsCaseData);
        assertEquals(3, facilitiesRequired.size());
        assertEquals("signLanguageInterpreter", facilitiesRequired.stream().findFirst().orElseThrow());
        assertEquals("disabledAccess", facilitiesRequired.stream().skip(2).findFirst().orElseThrow());
    }

    @Test
    void shouldGetEmptyFacilitiesRequiredWhenAppealNull() {
        // given
        SscsCaseData sscsCaseData = Mockito.mock(SscsCaseData.class);
        // when
        Mockito.when(sscsCaseData.getAppeal()).thenReturn(null);
        //then
        assertTrue(SscsCaseDataUtils.getFacilitiesRequired(sscsCaseData).isEmpty());
    }

    @Test
    void shouldGetCaseName() {
        // given
        SscsCaseData sscsCaseData = Mockito.mock(SscsCaseData.class);
        Appeal appeal = Mockito.mock(Appeal.class);
        Appellant appellant = Mockito.mock(Appellant.class);
        Name name = Mockito.mock(Name.class);
        // when
        Mockito.when(name.getFullName()).thenReturn("Mr Harry Potter");
        Mockito.when(appellant.getName()).thenReturn(name);
        Mockito.when(appeal.getAppellant()).thenReturn(appellant);
        Mockito.when(sscsCaseData.getAppeal()).thenReturn(appeal);
        //then
        assertEquals("Mr Harry Potter", SscsCaseDataUtils.getCaseName(sscsCaseData));
    }

    @Test
    void shouldGetEmptyCaseNameWhenAppealNull() {
        // given
        SscsCaseData sscsCaseData = Mockito.mock(SscsCaseData.class);
        // when
        Mockito.when(sscsCaseData.getAppeal()).thenReturn(null);
        //then
        assertEquals("",SscsCaseDataUtils.getCaseName(sscsCaseData));
    }

    @Test
    void shouldGetNumberOfAttendees() {
        // given
        SscsCaseData sscsCaseData = Mockito.mock(SscsCaseData.class);
        Appeal appeal = Mockito.mock(Appeal.class);
        HearingSubtype hearingSubtype = Mockito.mock(HearingSubtype.class);
        HearingOptions hearingOptions = Mockito.mock(HearingOptions.class);
        Representative representative = Mockito.mock(Representative.class);
        // when
        Mockito.when(representative.getHasRepresentative()).thenReturn(YesNo.YES.getValue());
        Mockito.when(hearingOptions.getWantsToAttend()).thenReturn(YesNo.YES.getValue());
        Mockito.when(hearingSubtype.isWantsHearingTypeFaceToFace()).thenReturn(true);
        Mockito.when(appeal.getRep()).thenReturn(representative);
        Mockito.when(appeal.getHearingOptions()).thenReturn(hearingOptions);
        Mockito.when(appeal.getHearingSubtype()).thenReturn(hearingSubtype);
        Mockito.when(sscsCaseData.getAppeal()).thenReturn(appeal);
        //then
        assertEquals(3, SscsCaseDataUtils.getNumberOfPhysicalAttendees(sscsCaseData));
    }

    @Test
    void shouldGetParties() {
        // given
        final HearingSubtype hearingSubtype = Mockito.mock(HearingSubtype.class);
        final HearingOptions hearingOptions = Mockito.mock(HearingOptions.class);
        @SuppressWarnings("unchecked")
        CcdValue<OtherParty> ccdValue = Mockito.mock(CcdValue.class);
        List<CcdValue<OtherParty>> otherPartyList = new ArrayList<>();
        otherPartyList.add(ccdValue);
        SscsCaseData sscsCaseData = Mockito.mock(SscsCaseData.class);
        OtherParty otherParty = Mockito.mock(OtherParty.class);
        Appeal appeal = Mockito.mock(Appeal.class);
        Name name = Mockito.mock(Name.class);
        Role role = Mockito.mock(Role.class);
        // when
        Mockito.when(appeal.getHearingType()).thenReturn(null);
        Mockito.when(sscsCaseData.getAppeal()).thenReturn(appeal);
        Mockito.when(name.getFullName()).thenReturn("Mr Harry Potter");
        Mockito.when(role.getName()).thenReturn("party_role");
        hearingsPartiesMapping.when(() -> HearingsPartiesMapping.getIndividualInterpreterLanguage(hearingOptions)).thenReturn(Optional.of("Telugu"));
        hearingsPartiesMapping.when(() -> HearingsPartiesMapping.getPartyUnavailabilityRange(hearingOptions)).thenReturn(getUnavailabilityRange());
        hearingsPartiesMapping.when(() -> HearingsPartiesMapping.getIndividualPreferredHearingChannel(appeal.getHearingType(), hearingSubtype)).thenReturn(Optional.ofNullable(FACE_TO_FACE.getHmcReference()));
        Mockito.when(otherParty.getHearingSubtype()).thenReturn(hearingSubtype);
        Mockito.when(otherParty.getHearingOptions()).thenReturn(hearingOptions);
        Mockito.when(otherParty.getName()).thenReturn(name);
        Mockito.when(otherParty.getRole()).thenReturn(role);
        Mockito.when(otherParty.getId()).thenReturn("party_id_1");
        Mockito.when(ccdValue.getValue()).thenReturn(otherParty);
        Mockito.when(sscsCaseData.getOtherParties()).thenReturn(otherPartyList);
        //then
        List<PartyDetails> partyDetailsList = SscsCaseDataUtils.getParties(sscsCaseData);
        assertEquals(1, partyDetailsList.size());
        PartyDetails partyDetails = partyDetailsList.stream().findFirst().orElseThrow();
        assertEquals("party_id_1", partyDetails.getPartyID());
        assertEquals(PartyType.IND, partyDetails.getPartyType());
        assertEquals(HearingUtils.FACE_TO_FACE, partyDetails.getPartyChannel());
        assertEquals("Mr Harry Potter", partyDetails.getPartyName());
        assertEquals("party_role", partyDetails.getPartyRole());
        assertEquals("Telugu", partyDetails.getIndividualDetails().getInterpreterLanguage());
        assertEquals(OrganisationDetails.builder().build(), partyDetails.getOrganisationDetails());
        assertNull(partyDetails.getUnavailabilityDow());
        List<UnavailabilityRange> unavailabilityRanges = partyDetails.getUnavailabilityRanges();
        assertEquals(0, unavailabilityRanges.size());
    }

    @Test
    void shouldGetEmptyPartiesRequiredWhenNoOtherParties() {
        // given
        SscsCaseData sscsCaseData = Mockito.mock(SscsCaseData.class);
        // when
        Mockito.when(sscsCaseData.getOtherParties()).thenReturn(null);
        //then
        assertTrue(SscsCaseDataUtils.getParties(sscsCaseData).isEmpty());
    }

    @Test
    void shouldGetHearingWindowWhenUrgentCase() {
        // given
        SscsCaseData sscsCaseData = Mockito.mock(SscsCaseData.class);
        // when
        Mockito.when(sscsCaseData.getEvents()).thenReturn(getEventsOfCaseData());
        Mockito.when(sscsCaseData.getUrgentCase()).thenReturn((YesNo.YES.getValue()));
        //then
        HearingWindow hearingWindow = SscsCaseDataUtils.getHearingWindow(sscsCaseData);
        assertEquals("2022-02-26", hearingWindow.getHearingWindowDateRange().getHearingWindowStartDateRange());
        assertNull(hearingWindow.getHearingWindowDateRange().getHearingWindowEndDateRange());
    }

    @Test
    void shouldGetHearingWindowWhenNonUrgentCase() {
        // given
        SscsCaseData sscsCaseData = Mockito.mock(SscsCaseData.class);
        // when
        Mockito.when(sscsCaseData.getEvents()).thenReturn(getEventsOfCaseData());
        Mockito.when(sscsCaseData.getUrgentCase()).thenReturn((YesNo.NO.getValue()));
        //then
        HearingWindow hearingWindow = SscsCaseDataUtils.getHearingWindow(sscsCaseData);
        assertEquals("2022-03-12", hearingWindow.getHearingWindowDateRange().getHearingWindowStartDateRange());
        assertNull(hearingWindow.getHearingWindowDateRange().getHearingWindowEndDateRange());
    }

    @Test
    void shouldGetNoHearingWindowWhenCaseResponseOverdue() {
        // given
        SscsCaseData sscsCaseData = Mockito.mock(SscsCaseData.class);
        // when
        Mockito.when(sscsCaseData.getEvents()).thenReturn(getOverdueEventsOfCaseData());
        Mockito.when(sscsCaseData.getUrgentCase()).thenReturn((YesNo.YES.getValue()));
        //then
        HearingWindow hearingWindow = SscsCaseDataUtils.getHearingWindow(sscsCaseData);
        assertNull(hearingWindow.getHearingWindowDateRange().getHearingWindowStartDateRange());
        assertNull(hearingWindow.getHearingWindowDateRange().getHearingWindowEndDateRange());
    }

    @Test
    void shouldGetCaseFlags() {
        // given
        SscsCaseData sscsCaseData = Mockito.mock(SscsCaseData.class);
        MockedStatic<PartyFlagsMapping> partyFlagsMapping = Mockito.mockStatic(PartyFlagsMapping.class);
        // when
        partyFlagsMapping.when(() -> PartyFlagsMapping.getPartyFlags(sscsCaseData)).thenReturn(getPartyFlags());
        // then
        CaseFlags caseFlags = SscsCaseDataUtils.getCaseFlags(sscsCaseData);
        assertNull(caseFlags.getFlagAmendUrl());
        assertEquals(8, caseFlags.getFlags().size());
        assertEquals(SIGN_LANGUAGE_TYPE.getFlagId(), caseFlags.getFlags().stream().findFirst().orElseThrow().getFlagId());
    }

    private List<ExcludeDate> getExcludeDates() {
        return new ArrayList<>() {
            {
                add(ExcludeDate.builder()
                        .value(DateRange.builder()
                                .start("2022-01-12")
                                .end("2022-01-19")
                                .build())
                        .build());
            }
        };
    }

    private List<UnavailabilityRange> getUnavailabilityRange() {
        return new ArrayList<>() {
            {
                add(UnavailabilityRange.builder()
                        .unavailableFromDate("2022-01-12")
                        .unavailableToDate("2022-01-19")
                        .build());
            }
        };
    }

    private List<Event> getEventsOfCaseData() {
        return new ArrayList<>() {
            {
                add(Event.builder()
                        .value(EventDetails.builder()
                                .date("2022-02-12T20:30:00")
                                .type("responseReceived")
                                .description("Dwp respond")
                                .build())
                        .build());
            }
        };
    }

    private List<Event> getOverdueEventsOfCaseData() {
        return new ArrayList<>() {
            {
                add(Event.builder()
                        .value(EventDetails.builder()
                                .date("2022-02-12T20:30:00")
                                .type("responseOverdue")
                                .description("Dwp response overdue")
                                .build())
                        .build());
            }
        };
    }

    private List<PartyFlags> getPartyFlags() {
        return new ArrayList<>() {
            {
                add(PartyFlags.builder()
                        .flagId(SIGN_LANGUAGE_TYPE.getFlagId())
                        .flagDescription(SIGN_LANGUAGE_TYPE.getFlagDescription())
                        .flagParentId(SIGN_LANGUAGE_TYPE.getParentId())
                        .build());
                add(PartyFlags.builder()
                        .flagId(DISABLED_ACCESS.getFlagId())
                        .flagDescription(DISABLED_ACCESS.getFlagDescription())
                        .flagParentId(DISABLED_ACCESS.getParentId()).build());
                add(PartyFlags.builder()
                        .flagId(HEARING_LOOP.getFlagId())
                        .flagDescription(HEARING_LOOP.getFlagDescription())
                        .flagParentId(HEARING_LOOP.getParentId()).build());
                add(PartyFlags.builder()
                        .flagId(IS_CONFIDENTIAL_CASE.getFlagId())
                        .flagDescription(IS_CONFIDENTIAL_CASE.getFlagDescription())
                        .flagParentId(IS_CONFIDENTIAL_CASE.getParentId())
                        .build());
                add(PartyFlags.builder()
                        .flagId(DWP_UCB.getFlagId())
                        .flagDescription(DWP_UCB.getFlagDescription())
                        .flagParentId(DWP_UCB.getParentId()).build());
                add(PartyFlags.builder()
                        .flagId(DWP_PHME.getFlagId())
                        .flagDescription(DWP_PHME.getFlagDescription())
                        .flagParentId(DWP_PHME.getParentId()).build());
                add(PartyFlags.builder()
                        .flagId(URGENT_CASE.getFlagId())
                        .flagDescription(URGENT_CASE.getFlagDescription())
                        .flagParentId(URGENT_CASE.getParentId()).build());
                add(PartyFlags.builder()
                        .flagId(ADJOURN_CASE_INTERPRETER_LANGUAGE.getFlagId())
                        .flagDescription(ADJOURN_CASE_INTERPRETER_LANGUAGE.getFlagDescription())
                        .flagParentId(ADJOURN_CASE_INTERPRETER_LANGUAGE.getParentId()).build());
            }
        };
    }
}
