package uk.gov.hmcts.reform.sscs.helper;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingLocations;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingWindow;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PanelPreference;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PanelRequirements;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.NO;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.YES;

class HearingsMappingTest {

    public static final String CASE_ID = "122342343434234";

    @Value("${exui.url}")
    private static String exUiUrl;

    @Test
    @DisplayName("When a valid hearing wrapper is given createHearingRequest returns the correct Hearing Request")
    void createHearingRequest() {
        // TODO Finish Test when method done
    }

    @DisplayName("shouldBeAutoListed Parameterized Tests")
    @Test
    void shouldBeAutoListed() {
        // TODO Finish Test when method done
        boolean result = HearingsMapping.shouldBeAutoListed();

        assertTrue(result);
    }

    @DisplayName("shouldBeHearingsInWelshFlag Test")
    @Test
    void shouldBeHearingsInWelshFlag() {
        boolean result = HearingsMapping.shouldBeHearingsInWelshFlag();

        assertFalse(result);
    }

    @Disabled
    @DisplayName("shouldBeAdditionalSecurityFlag Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "true,true|true,true",
        "true,false|true,true",
        "true,true|false,true",
        "true,false|false,true",
        "true,false,true",
        "true,true,true",
        "true,null,true",
        "false,true|true,true",
        "false,false,true,true",
        "false,true|false,true",
        "false,false,false,false",
        "false,true,true",
        "false,false,false",
        "false,null,false",
    }, nullValues = {"null"})
    void shouldBeAdditionalSecurityFlag(boolean appellantFlag, String otherPartiesFlags, boolean expected) {
        // TODO Finish Test when method done
        List<CcdValue<OtherParty>> otherParties = null;
        if (nonNull(otherPartiesFlags)) {
            otherParties = new ArrayList<>();
            for (boolean flag : Arrays.stream(otherPartiesFlags.split("\\s*\\|\\s*")).map(Boolean::parseBoolean).collect(Collectors.toList())) {
                otherParties.add(CcdValue.<OtherParty>builder().value(OtherParty.builder().unacceptableCustomerBehaviour(flag ? YES : NO).build()).build());
            }
        }

        SscsCaseData caseData = SscsCaseData.builder()
                .appeal(Appeal.builder()
                        .appellant(Appellant.builder().unacceptableCustomerBehaviour(appellantFlag ? YES : NO).build())
                        .build())
                .otherParties(otherParties)
                .build();
        boolean result = HearingsMapping.shouldBeAdditionalSecurityFlag(caseData);

        assertEquals(expected, result);
    }

    @DisplayName("shouldBeSensitiveFlag Test")
    @Test
    void shouldBeSensitiveFlag() {
        // TODO Finish Test when method done
        boolean result = HearingsMapping.shouldBeSensitiveFlag();

        assertFalse(result);
    }

    @DisplayName("When case ID is given getCaseDeepLink returns the correct link")
    @Test
    void getCaseDeepLink() {
        // TODO Finish Test when method done
        String result = HearingsMapping.getCaseDeepLink(CASE_ID);
        String expected = String.format("%s/cases/case-details/%s", exUiUrl, CASE_ID);

        assertEquals(expected, result);
    }

    @DisplayName("When ... is given getCaseManagementLocationCode returns the correct EPIMS ID")
    @Test
    void getCaseManagementLocationCode() {
        // TODO Finish Test when method done
        CaseManagementLocation location = CaseManagementLocation.builder()
                .baseLocation("Test Location")
                .region("Test Region")
                .build();
        String result = HearingsMapping.getCaseManagementLocationCode(location);
        String expected = null;

        assertEquals(expected, result);
    }

    @DisplayName("When .. is given getFacilitiesRequired return the correct facilities Required")
    @Test
    void getFacilitiesRequired() {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder().build();
        List<String> result = HearingsMapping.getFacilitiesRequired(caseData);
        List<String> expected = new ArrayList<>();

        assertEquals(0, result.size());
        assertEquals(expected, result);
    }

    @DisplayName("When case with valid DWP_RESPOND event and is auto-listable is given buildHearingWindow returns a window starting within 1 month of the event's date")
    @ParameterizedTest
    @CsvSource(value = {
        "DWP_RESPOND,true",
    }, nullValues = {"null"})
    void buildHearingWindow(EventType eventType, boolean autoListFlag) {
        // TODO Finish Test when method done
        List<Event> events = new ArrayList<>();
        LocalDateTime testDateTime = LocalDateTime.now();
        events.add(Event.builder().value(EventDetails.builder()
                .type(eventType.getCcdType())
                .date(testDateTime.toString())
                .build()).build());



        SscsCaseData caseData = SscsCaseData.builder().events(events).build();
        HearingWindow result = HearingsMapping.buildHearingWindow(caseData, autoListFlag);

        assertNull(result.getFirstDateTimeMustBe());

        HearingWindow expected = HearingWindow.builder()
                .dateRangeStart(testDateTime.plusMonths(1).toLocalDate()).build();
        assertEquals(expected, result);
    }

    @DisplayName("When case with no valid event or is negative auto-listable is given buildHearingWindow returns a null value")
    @ParameterizedTest
    @CsvSource(value = {
        "WITHDRAWN,true",
        "null,true",
        "DWP_RESPOND,false",
    }, nullValues = {"null"})
    void buildHearingWindowNullReturn(EventType eventType, boolean autoListFlag) {
        // TODO Finish Test when method done
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
        HearingWindow result = HearingsMapping.buildHearingWindow(caseData, autoListFlag);

        assertNull(result.getFirstDateTimeMustBe());
        assertNull(result.getDateRangeStart());
        assertNull(result.getDateRangeEnd());
    }

    @DisplayName("When .. is given getFacilitiesRequired returns the valid LocalDateTime")
    @Test
    void getFirstDateTimeMustBe() {
        // TODO Finish Test when method done
        LocalDateTime result = HearingsMapping.getFirstDateTimeMustBe();

        assertNull(result);
    }

    @DisplayName("When .. is given getPanelRequirements returns the valid PanelRequirements")
    @Test
    void getPanelRequirements() {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder().build();

        PanelRequirements expected =  PanelRequirements.builder().build();
        List<String> roleTypes = new ArrayList<>();
        expected.setRoleTypes(roleTypes);
        List<String> authorisationSubTypes = new ArrayList<>();
        expected.setAuthorisationSubTypes(authorisationSubTypes);
        List<PanelPreference> panelPreferences = new ArrayList<>();
        expected.setPanelPreferences(panelPreferences);
        List<String> panelSpecialisms = new ArrayList<>();
        expected.setPanelSpecialisms(panelSpecialisms);

        PanelRequirements result = HearingsMapping.getPanelRequirements(caseData);

        assertEquals(expected, result);
    }

    @DisplayName("When .. is given getPanelPreferences returns the correct List of PanelPreferences")
    @Test
    void getPanelPreferences() {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder().build();
        List<PanelPreference> result = HearingsMapping.getPanelPreferences(caseData);
        List<PanelPreference> expected = new ArrayList<>();

        assertEquals(0, result.size());
        assertEquals(expected, result);
    }

    @DisplayName("When .. is given getLeadJudgeContractType returns the correct LeadJudgeContractType")
    @Test
    void getLeadJudgeContractType() {
        // TODO Finish Test when method done
        String result = HearingsMapping.getLeadJudgeContractType();

        assertNull(result);
    }

    @DisplayName("When appellant and other parties Hearing Options other comments are given "
            + "getListingComments returns all the comments separated by newlines")
    @ParameterizedTest
    @CsvSource(value = {
        "AppellantComments,'OpComments1|OpComments2',AppellantComments\\nOpComments1\\nOpComments2",
        "AppellantComments,'OpComments1',AppellantComments\\nOpComments1",
        "AppellantComments,'',AppellantComments",
        "AppellantComments,'||',AppellantComments",
    }, nullValues = {"null"})
    void getListingComments(String appellant, String otherPartiesComments, String expected) {
        List<CcdValue<OtherParty>> otherParties = new ArrayList<>();
        if (nonNull(otherPartiesComments)) {
            for (String otherPartyComment : otherPartiesComments.split("\\s*\\|\\s*")) {
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

        String result = HearingsMapping.getListingComments(appeal, otherParties);

        assertEquals(expected.replace("\\n","\n"), result);
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
            for (String otherPartyComment : otherPartiesComments.split("\\s*\\|\\s*")) {
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

        String result = HearingsMapping.getListingComments(appeal, otherParties);

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
        List<HearingLocations> result = HearingsMapping.getHearingLocations(managementLocation);
        //List<HearingLocations> expected = new ArrayList<>();

        assertEquals(1, result.size());
        assertEquals("219164", result.get(0).getLocationId());
        assertEquals("court", result.get(0).getLocationType());
    }

    @DisplayName("getHearingPriority Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "Yes,Yes,High", "Yes,No,High", "No,Yes,High",
        "No,No,Normal",
        "Yes,null,High", "No,null,Normal",
        "null,Yes,High", "null,No,Normal",
        "null,null,Normal",
        "Yes,,High", "No,,Normal",
        ",Yes,High", ",No,Normal",
        ",,Normal"
    }, nullValues = {"null"})
    void getHearingPriority(String isAdjournCase, String isUrgentCase, String expected) {
        // TODO Finish Test when method done
        String result = HearingsMapping.getHearingPriority(isAdjournCase, isUrgentCase);

        assertEquals(expected, result);
    }


    @DisplayName("getHearingDuration Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "null,null,null,null,30",
        "061,null,null,null,30",
        "null,IssueCode,null,null,30",
        "051,IssueCode,null,null,60",
        "037,IssueCode,null,60,60",
        "061,IssueCode,2,hours,120",
        "061,IssueCode,60,mins,60",
        "null,IssueCode,-1,hours,30",
    }, nullValues = {"null"})
    void getHearingDuration(String benefitCode, String issueCode,
                            String adjournCaseDuration, String adjournCaseDurationUnits, int expected) {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder()
                .benefitCode(benefitCode)
                .issueCode(issueCode)
                .adjournCaseNextHearingListingDuration(adjournCaseDuration)
                .adjournCaseNextHearingListingDurationUnits(adjournCaseDurationUnits)
                .build();
        int result = HearingsMapping.getHearingDuration(caseData);

        assertEquals(expected, result);
    }

    @DisplayName("When .. is given getHearingType returns the correct Hearing Type")
    @Test
    void getHearingType() {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder().build();
        String result = HearingsMapping.getHearingType(caseData);

        assertNull(result);
    }

    @DisplayName("isInterpreterRequired Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {"Yes,true", "No,false", "null,false"}, nullValues = {"null"})
    void isInterpreterRequired(String adjournCaseInterpreterRequired, boolean expected) {
        boolean result = HearingsMapping.isInterpreterRequired(adjournCaseInterpreterRequired);

        assertEquals(expected, result);
    }

    @DisplayName("When .. is given isCaseLinked returns if case is linked")
    @Test
    void isCaseLinked() {
        // TODO Finish Test when method done
        boolean result = HearingsMapping.isCaseLinked();

        assertFalse(result);
    }
}
