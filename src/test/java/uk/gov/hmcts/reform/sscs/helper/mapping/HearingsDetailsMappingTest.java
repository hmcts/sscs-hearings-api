package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingLocations;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingWindow;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PanelPreference;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PanelRequirements;
import uk.gov.hmcts.reform.sscs.reference.data.mappings.HearingTypeLov;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.NO;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.YES;

class HearingsDetailsMappingTest extends HearingsMappingBase {

    @DisplayName("When a valid hearing wrapper is given buildHearingDetails returns the correct Hearing Details")
    @Test
    void buildHearingDetails() {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder()
                .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder().build())
                        .build())
                .caseManagementLocation(CaseManagementLocation.builder()
                                            .baseLocation(EPIMS_ID)
                                            .region(REGION)
                                            .build())
                .build();

        HearingWrapper wrapper = HearingWrapper.builder()
                .caseData(caseData)
                .build();

        HearingDetails hearingDetails = HearingsDetailsMapping.buildHearingDetails(wrapper);

        assertNotNull(hearingDetails.getHearingType());
        assertNotNull(hearingDetails.getHearingWindow());
        assertNotNull(hearingDetails.getDuration());
        assertNotNull(hearingDetails.getHearingPriorityType());
        assertNull(hearingDetails.getNumberOfPhysicalAttendees());
        assertNotNull(hearingDetails.getHearingLocations());
        assertNull(hearingDetails.getListingComments());
        assertNull(hearingDetails.getHearingRequester());
        assertNull(hearingDetails.getLeadJudgeContractType());
        assertNotNull(hearingDetails.getPanelRequirements());
    }

    @DisplayName("shouldBeAutoListed Parameterized Tests")
    @Test
    void shouldBeAutoListed() {
        // TODO Finish Test when method done
        boolean result = HearingsDetailsMapping.shouldBeAutoListed();

        assertTrue(result);
    }

    @DisplayName("shouldBeHearingsInWelshFlag Test")
    @Test
    void shouldBeHearingsInWelshFlag() {
        boolean result = HearingsDetailsMapping.shouldBeHearingsInWelshFlag();

        assertFalse(result);
    }

    @DisplayName("When .. is given isCaseLinked returns if case is linked")
    @Test
    void isCaseLinked() {
        // TODO Finish Test when method done
        boolean result = HearingsDetailsMapping.isCaseLinked();

        assertFalse(result);
    }

    @DisplayName("Hearing type should be substantive.")
    @Test
    void getHearingType() {
        String result = HearingsDetailsMapping.getHearingType();

        assertEquals(result, HearingTypeLov.SUBSTANTIVE.getHmcReference());
    }

    @DisplayName("When case with valid DWP_RESPOND event and is auto-listable is given buildHearingWindow returns a window starting within 1 month of the event's date")
    @ParameterizedTest
    @CsvSource(value = {
        "DWP_RESPOND,2021-12-01T10:15:30,true,true,2021-12-15",
        "DWP_RESPOND,2021-12-01T10:15:30,true,false,2024-04-01",
        "DWP_RESPOND,2021-12-01T10:15:30,false,true,null",
        "DWP_RESPOND,2021-12-01T10:15:30,false,false,null",
        "DWP_RESPOND,null,true,true,null",
        "DWP_RESPOND,null,true,false,null",
        "DWP_RESPOND,null,false,true,null",
        "DWP_RESPOND,null,false,false,null",
    }, nullValues = {"null"})
    void buildHearingWindow(EventType eventType, String dwpResponded, boolean autoListFlag, boolean isUrgent, LocalDate expected) {
        List<Event> events = new ArrayList<>();
        events.add(Event.builder().value(EventDetails.builder()
                .type(eventType.getCcdType())
                .date(dwpResponded)
                .build()).build());

        SscsCaseData caseData = SscsCaseData.builder()
                .events(events)
                .urgentCase(isUrgent ? YES.toString() : NO.toString())
                .build();
        HearingWindow result = HearingsDetailsMapping.buildHearingWindow(caseData, autoListFlag);

        assertNull(result.getFirstDateTimeMustBe());
        assertEquals(expected, result.getDateRangeStart());
        assertNull(result.getDateRangeEnd());
    }

    @DisplayName("When case with no valid event or is negative auto-listable is given buildHearingWindow returns a null value")
    @ParameterizedTest
    @CsvSource(value = {
        "WITHDRAWN,true",
        "null,true",
        "DWP_RESPOND,false",
    }, nullValues = {"null"})
    void buildHearingWindowNullReturn(EventType eventType, boolean autoListFlag) {
        List<Event> events = new ArrayList<>();
        LocalDateTime testDateTime = LocalDateTime.now();
        if (nonNull(eventType)) {
            events.add(Event.builder().value(EventDetails.builder()
                    .type(eventType.getCcdType())
                    .date(testDateTime.toString())
                    .build()).build());
        } else {
            events = null;
        }

        SscsCaseData caseData = SscsCaseData.builder().events(events).build();
        HearingWindow result = HearingsDetailsMapping.buildHearingWindow(caseData, autoListFlag);

        assertNull(result.getFirstDateTimeMustBe());
        assertNull(result.getDateRangeStart());
        assertNull(result.getDateRangeEnd());
    }

    @DisplayName("When .. is given getFacilitiesRequired returns the valid LocalDateTime")
    @Test
    void getFirstDateTimeMustBe() {
        // TODO Finish Test when method done
        LocalDateTime result = HearingsDetailsMapping.getFirstDateTimeMustBe();

        assertNull(result);
    }

    @DisplayName("getHearingPriority Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "Yes,Yes,high", "Yes,No,high", "No,Yes,high",
        "No,No,normal",
        "Yes,null,high", "No,null,normal",
        "null,Yes,high", "null,No,normal",
        "null,null,normal",
        "Yes,,high", "No,,normal",
        ",Yes,high", ",No,normal",
        ",,normal"
    }, nullValues = {"null"})
    void getHearingPriority(String isAdjournCase, String isUrgentCase, String expected) {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder()
                .urgentCase(isUrgentCase)
                .adjournCasePanelMembersExcluded(isAdjournCase)
                .build();
        String result = HearingsDetailsMapping.getHearingPriority(caseData);

        assertEquals(expected, result);
    }

    @DisplayName("getNumberOfPhysicalAttendees Test")
    @Test
    void getNumberOfPhysicalAttendees() {
        Number result = HearingsDetailsMapping.getNumberOfPhysicalAttendees();

        assertNull(result);
    }

    @DisplayName("getHearingLocations Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {"219164,court"}, nullValues = {"null"})
    void getHearingLocations(String baseLocation, String region) {
        CaseManagementLocation managementLocation = CaseManagementLocation.builder()
                .baseLocation(baseLocation)
                .region(region)
                .build();
        List<HearingLocations> result = HearingsDetailsMapping.getHearingLocations(managementLocation);

        assertEquals(1, result.size());
        assertEquals("219164", result.get(0).getLocationId());
        assertEquals("court", result.get(0).getLocationType());
    }

    @DisplayName("When .. is given getFacilitiesRequired return the correct facilities Required")
    @Test
    void getFacilitiesRequired() {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder().build();
        List<String> result = HearingsDetailsMapping.getFacilitiesRequired(caseData);
        List<String> expected = new ArrayList<>();

        assertEquals(0, result.size());
        assertEquals(expected, result);
    }

    @DisplayName("When appellant and other parties Hearing Options other comments are given "
            + "getListingComments returns all the comments separated by newlines")
    @ParameterizedTest
    @CsvSource(value = {
        "AppellantComments,'OpComments1',Appellant - Mx Test Appellant:\\nAppellantComments\\n\\nOther Party - Mx Test OtherParty:\\nOpComments1",
        "AppellantComments,'',Appellant - Mx Test Appellant:\\nAppellantComments",
        "AppellantComments,null,Appellant - Mx Test Appellant:\\nAppellantComments",
    }, nullValues = {"null"})
    void getListingComments(String appellant, String otherPartiesComments, String expected) {
        List<CcdValue<OtherParty>> otherParties = null;

        if (nonNull(otherPartiesComments)) {
            otherParties = new ArrayList<>();
            otherParties.add(new CcdValue<>(OtherParty.builder()
                    .name(Name.builder()
                            .title("Mx")
                            .firstName("Test")
                            .lastName("OtherParty")
                            .build())
                    .hearingOptions(HearingOptions.builder()
                            .other(otherPartiesComments)
                            .build())
                    .build()));
        }

        Appeal appeal = Appeal.builder().appellant(Appellant.builder()
                        .name(Name.builder()
                                .title("Mx")
                                .firstName("Test")
                                .lastName("Appellant")
                                .build())
                        .build())
                .build();
        if (nonNull(appellant)) {
            appeal.setHearingOptions(HearingOptions.builder().other(appellant).build());
        }

        String result = HearingsDetailsMapping.getListingComments(appeal, otherParties);

        assertThat(result).isEqualToNormalizingNewlines(expected.replace("\\n",String.format("%n")));
    }

    @DisplayName("When all null or empty comments are given getListingComments returns null")
    @ParameterizedTest
    @CsvSource(value = {
        "'',''",
        "null,''",
        "null,null",
        "'','||'",
    }, nullValues = {"null"})
    void getListingCommentsReturnsNull(String appellant, String otherPartiesComments) {
        List<CcdValue<OtherParty>> otherParties = new ArrayList<>();
        if (nonNull(otherPartiesComments)) {
            for (String otherPartyComment : splitCsvParamArray(otherPartiesComments)) {
                otherParties.add(new CcdValue<>(OtherParty.builder()
                        .hearingOptions(HearingOptions.builder().other(otherPartyComment).build())
                        .build()));
            }
        } else {
            otherParties = null;
        }
        Appeal appeal = Appeal.builder().build();
        if (nonNull(appellant)) {
            appeal.setHearingOptions(HearingOptions.builder().other(appellant).build());
        }

        String result = HearingsDetailsMapping.getListingComments(appeal, otherParties);

        assertNull(result);
    }


    @DisplayName("getPartyRole with Role Test")
    @Test
    void getPartyRole() {
        Party party = Appellant.builder()
                .role(Role.builder()
                        .name("Test Role")
                        .build())
                .build();
        String result = HearingsDetailsMapping.getPartyRole(party);

        assertThat(result).isEqualTo("Test Role");
    }

    @DisplayName("getPartyRole without Role Test")
    @Test
    void getPartyRoleNoRole() {
        Party party = Appellant.builder().build();
        String result = HearingsDetailsMapping.getPartyRole(party);

        assertThat(result).isEqualTo("Appellant");
    }

    @DisplayName("getPartyRole with empty/null Role Name Test")
    @ParameterizedTest
    @NullAndEmptySource
    void getPartyRoleNoRole(String role) {
        Party party = Appellant.builder()
                .role(Role.builder()
                        .name(role)
                        .build())
                .build();
        String result = HearingsDetailsMapping.getPartyRole(party);

        assertThat(result).isEqualTo("Appellant");
    }

    @DisplayName("getPartyName where Appointee is null Test")
    @ParameterizedTest
    @ValueSource(strings = {"Yes", "No"})
    @NullAndEmptySource
    void getPartyName(String isAppointee) {
        Party party = Appellant.builder()
                .isAppointee(isAppointee)
                .name(Name.builder()
                        .title("Mx")
                        .firstName("Test")
                        .lastName("Appellant")
                        .build())
                .build();
        String result = HearingsDetailsMapping.getEntityName(party);

        assertThat(result).isEqualTo("Mx Test Appellant");
    }

    @DisplayName("getPartyName No Appointee Test")
    @ParameterizedTest
    @ValueSource(strings = {"No"})
    @NullAndEmptySource
    void getPartyNameAppellant(String isAppointee) {
        Party party = Appellant.builder()
                .isAppointee(isAppointee)
                .appointee(Appointee.builder()
                        .name(Name.builder()
                                .title("Mx")
                                .firstName("Test")
                                .lastName("Appointee")
                                .build())
                        .build())
                .name(Name.builder()
                        .title("Mx")
                        .firstName("Test")
                        .lastName("Appellant")
                        .build())
                .build();
        String result = HearingsDetailsMapping.getEntityName(party);

        assertThat(result).isEqualTo("Mx Test Appellant");
    }

    @DisplayName("getHearingRequester Test")
    @Test
    void getHearingRequester() {
        String result = HearingsDetailsMapping.getHearingRequester();

        assertNull(result);
    }

    @DisplayName("When .. is given getLeadJudgeContractType returns the correct LeadJudgeContractType")
    @Test
    void getLeadJudgeContractType() {
        // TODO Finish Test when method done
        String result = HearingsDetailsMapping.getLeadJudgeContractType();

        assertNull(result);
    }

    @DisplayName("When no data is given getPanelRequirements returns the valid but empty PanelRequirements")
    @Test
    void getPanelRequirements() {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder().build();

        PanelRequirements result = HearingsDetailsMapping.getPanelRequirements(caseData);

        assertThat(result).isNotNull();
        assertThat(result.getRoleTypes()).isEmpty();
        assertThat(result.getAuthorisationTypes()).isEmpty();
        assertThat(result.getAuthorisationSubTypes()).isEmpty();
        assertThat(result.getPanelPreferences()).isEmpty();
        assertThat(result.getPanelSpecialisms()).isEmpty();
    }

    @DisplayName("When a industrial case is given getPanelRequirements returns the valid PanelRequirements with second specialist doctor")
    @ParameterizedTest
    @CsvSource(value = {
        "031,EX,cardiologist,eyeSurgeon,BBA3-MQPM1-001|BBA3-MQPM2-003",
        "036,DD,null,carer,BBA3-MQPM1|BBA3-MQPM2-002",
        "031,RC,generalPractitioner,null,BBA3-MQPM1-004",
        "067,OK,null,null,BBA3-MQPM1",
    }, nullValues = {"null"})
    void getPanelRequirements(String benefitCode, String issueCode, String doctorSpecialism, String doctorSpecialismSecond, String expected) {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder()
                .benefitCode(benefitCode)
                .issueCode(issueCode)
                .sscsIndustrialInjuriesData(SscsIndustrialInjuriesData.builder()
                        .panelDoctorSpecialism(doctorSpecialism)
                        .secondPanelDoctorSpecialism(doctorSpecialismSecond)
                        .build())
                .build();

        PanelRequirements result = HearingsDetailsMapping.getPanelRequirements(caseData);

        assertThat(result).isNotNull();
        assertThat(result.getRoleTypes()).isEmpty();
        assertThat(result.getAuthorisationTypes()).isEmpty();
        assertThat(result.getAuthorisationSubTypes()).isEmpty();
        assertThat(result.getPanelPreferences()).isEmpty();

        List<String> expectedList = splitCsvParamArray(expected);
        assertThat(result.getPanelSpecialisms())
                .containsExactlyInAnyOrderElementsOf(expectedList);

    }

    @DisplayName("When .. is given getPanelPreferences returns the correct List of PanelPreferences")
    @Test
    void getPanelPreferences() {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder().build();
        List<PanelPreference> result = HearingsDetailsMapping.getPanelPreferences(caseData);
        List<PanelPreference> expected = new ArrayList<>();

        assertEquals(0, result.size());
        assertEquals(expected, result);
    }


    @DisplayName("getHearingDuration Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "null,null,null,null,30",
        "061,null,null,null,30",
        "null,CP,null,null,30",
        "061,FR,null,null,30",
        "013,EC,null,60,30",
        "003,CE,2,hours,120",
        "061,WI,1,sessions,165",
        "061,FR,1,test,30",
        "null,XA,2,hours,120",
    }, nullValues = {"null"})
    void getHearingDuration(String benefitCode, String issueCode, String adjournCaseDuration, String adjournCaseDurationUnits, int expected) {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder()
                .benefitCode(benefitCode)
                .issueCode(issueCode)
                .adjournCaseNextHearingListingDuration(adjournCaseDuration)
                .adjournCaseNextHearingListingDurationUnits(adjournCaseDurationUnits)
                .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder().build())
                        .build())
                .build();
        int result = HearingsDetailsMapping.getHearingDuration(caseData);

        assertEquals(expected, result);
    }

    @DisplayName("getHearingDurationBenefitIssueCodes Paper Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "null,null,-1",
        "061,null,-1",
        "null,XA,-1",
        "061,FR,-1",
        "999,EC,-1",
        "013,EC,30",
        "003,CE,30",
        "061,WI,30",
    }, nullValues = {"null"})
    void getHearingDurationBenefitIssueCodesPaper(String benefitCode, String issueCode, int expected) {
        SscsCaseData caseData = SscsCaseData.builder()
                .benefitCode(benefitCode)
                .issueCode(issueCode)
                .appeal(Appeal.builder()
                        .hearingType("paper")
                        .hearingSubtype(HearingSubtype.builder().build())
                        .hearingOptions(HearingOptions.builder().build())
                        .build())
                .build();

        int result = HearingsDetailsMapping.getHearingDurationBenefitIssueCodes(caseData);

        assertEquals(expected, result);
    }

    @DisplayName("getHearingDurationBenefitIssueCodes FaceToFace Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "null,null,-1",
        "061,null,-1",
        "null,XA,-1",
        "061,FR,-1",
        "999,EC,-1",
        "013,EC,45",
        "003,CE,60",
        "061,WI,45",
    }, nullValues = {"null"})
    void getHearingDurationBenefitIssueCodesFaceToFace(String benefitCode, String issueCode, int expected) {
        SscsCaseData caseData = SscsCaseData.builder()
                .benefitCode(benefitCode)
                .issueCode(issueCode)
                .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder()
                                .wantsToAttend("Yes")
                                .build())
                        .build())
                .build();

        int result = HearingsDetailsMapping.getHearingDurationBenefitIssueCodes(caseData);

        assertEquals(expected, result);
    }

    @DisplayName("getHearingDurationBenefitIssueCodes Interpreter Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "null,null,-1",
        "061,null,-1",
        "null,XA,-1",
        "061,FR,-1",
        "999,EC,-1",
        "013,EC,75",
        "003,CE,90",
        "061,WI,75",
    }, nullValues = {"null"})
    void getHearingDurationBenefitIssueCodesInterpreter(String benefitCode, String issueCode, int expected) {
        SscsCaseData caseData = SscsCaseData.builder()
                .benefitCode(benefitCode)
                .issueCode(issueCode)
                .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder()
                                .wantsToAttend("Yes")
                                .languageInterpreter("Yes")
                                .build())
                        .build())
                .build();

        int result = HearingsDetailsMapping.getHearingDurationBenefitIssueCodes(caseData);

        assertEquals(expected, result);
    }

    @DisplayName("getElementsDisputed when elementDisputed is Null Test")
    @Test
    void getElementsDisputedNull() {
        SscsCaseData caseData = SscsCaseData.builder().build();
        List<String> result = HearingsDetailsMapping.getElementsDisputed(caseData);

        assertThat(result).isEmpty();
    }

    @DisplayName("getElementsDisputed Test")
    @Test
    void getElementsDisputed() {
        ElementDisputed elementDisputed = ElementDisputed.builder()
                .value(ElementDisputedDetails.builder()
                        .issueCode("WC")
                        .outcome("Test")
                        .build())
                .build();
        SscsCaseData caseData = SscsCaseData.builder()
                .elementsDisputedGeneral(List.of(elementDisputed))
                .elementsDisputedSanctions(List.of(elementDisputed))
                .elementsDisputedOverpayment(List.of(elementDisputed))
                .elementsDisputedHousing(List.of(elementDisputed))
                .elementsDisputedChildCare(List.of(elementDisputed))
                .elementsDisputedCare(List.of(elementDisputed))
                .elementsDisputedChildElement(List.of(elementDisputed))
                .elementsDisputedChildDisabled(List.of(elementDisputed))
                .elementsDisputedLimitedWork(List.of(elementDisputed))
                .build();
        List<String> result = HearingsDetailsMapping.getElementsDisputed(caseData);

        assertThat(result)
                .hasSize(9)
                .containsOnly("WC");
    }
}
