package uk.gov.hmcts.reform.sscs.helper;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingLocations;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingWindow;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PanelPreference;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PanelRequirements;
import uk.gov.hmcts.reform.sscs.service.SessionLookupService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.NO;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.YES;

@ExtendWith(MockitoExtension.class)
class HearingsDetailsMappingTest extends HearingsMappingBase {

    @Mock
    private SessionLookupService sessionLookupService;
    private HearingsDetailsMapping hearingsDetailsMapping;

    @BeforeEach
    void setUp() {
        this.hearingsDetailsMapping = new HearingsDetailsMapping(sessionLookupService);
    }

    @DisplayName("When a valid hearing wrapper is given buildHearingDetails returns the correct Hearing Details")
    @Test
    void buildHearingDetails() {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder()
                .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder().build())
                        .build())
                .build();
        HearingWrapper wrapper = HearingWrapper.builder()
                .originalCaseData(caseData)
                .updatedCaseData(caseData)
                .build();

        HearingDetails hearingDetails = hearingsDetailsMapping.buildHearingDetails(wrapper);

        assertNull(hearingDetails.getHearingType());
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
        boolean result = hearingsDetailsMapping.shouldBeAutoListed();

        assertTrue(result);
    }

    @DisplayName("shouldBeHearingsInWelshFlag Test")
    @Test
    void shouldBeHearingsInWelshFlag() {
        boolean result = hearingsDetailsMapping.shouldBeHearingsInWelshFlag();

        assertFalse(result);
    }

    @DisplayName("When .. is given isCaseLinked returns if case is linked")
    @Test
    void isCaseLinked() {
        // TODO Finish Test when method done
        boolean result = hearingsDetailsMapping.isCaseLinked();

        assertFalse(result);
    }

    @DisplayName("When .. is given getHearingType returns the correct Hearing Type")
    @Test
    void getHearingType() {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder().build();
        String result = hearingsDetailsMapping.getHearingType(caseData);

        assertNull(result);
    }

    @DisplayName("When case with valid DWP_RESPOND event and is auto-listable is given buildHearingWindow returns a window starting within 1 month of the event's date")
    @ParameterizedTest
    @CsvSource(value = {
        "DWP_RESPOND,2021-10-10,2021-12-01T10:15:30,true,true,2021-10-24",
        "DWP_RESPOND,2021-10-10,2021-12-01T10:15:30,true,false,2022-01-01",
        "DWP_RESPOND,2021-10-10,null,true,true,2021-10-24",
        "DWP_RESPOND,2021-10-10,null,true,false,null",
        "DWP_RESPOND,null,2021-12-01T10:15:30,true,true,2022-01-01",
        "DWP_RESPOND,null,2021-12-01T10:15:30,true,false,2022-01-01",
        "DWP_RESPOND,null,null,true,true,null",
        "DWP_RESPOND,null,null,true,false,null",
    }, nullValues = {"null"})
    void buildHearingWindow(EventType eventType, String caseStart, String dwpResponded, boolean autoListFlag, boolean isUrgent, LocalDate expected) {
        // TODO Finish Test when method done
        List<Event> events = new ArrayList<>();
        events.add(Event.builder().value(EventDetails.builder()
                .type(eventType.getCcdType())
                .date(dwpResponded)
                .build()).build());

        SscsCaseData caseData = SscsCaseData.builder()
                .caseCreated(caseStart)
                .events(events)
                .urgentCase(isUrgent ? YES.toString() : NO.toString())
                .build();
        HearingWindow result = hearingsDetailsMapping.buildHearingWindow(caseData, autoListFlag);

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
        HearingWindow result = hearingsDetailsMapping.buildHearingWindow(caseData, autoListFlag);

        assertNull(result.getFirstDateTimeMustBe());
        assertNull(result.getDateRangeStart());
        assertNull(result.getDateRangeEnd());
    }

    @DisplayName("When .. is given getFacilitiesRequired returns the valid LocalDateTime")
    @Test
    void getFirstDateTimeMustBe() {
        // TODO Finish Test when method done
        LocalDateTime result = hearingsDetailsMapping.getFirstDateTimeMustBe();

        assertNull(result);
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
        String result = hearingsDetailsMapping.getHearingPriority(isAdjournCase, isUrgentCase);

        assertEquals(expected, result);
    }

    @DisplayName("getNumberOfPhysicalAttendees Test")
    @Test
    void getNumberOfPhysicalAttendees() {
        Number result = hearingsDetailsMapping.getNumberOfPhysicalAttendees();

        assertNull(result);
    }

    @Disabled
    @DisplayName("getHearingLocations Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {"TBD,TBD,[{CLUSTER,TBD}]"}, nullValues = {"null"})
    void getHearingLocations(String baseLocation, String region,
                             List<Pair<String,String>> expectedParams) {
        // TODO Finish Test when method done

        CaseManagementLocation managementLocation = CaseManagementLocation.builder()
                .baseLocation(baseLocation)
                .region(region)
                .build();
        List<HearingLocations> result = hearingsDetailsMapping.getHearingLocations(managementLocation);
        List<HearingLocations> expected = new ArrayList<>();

        assertEquals(0, result.size());
        assertEquals(expected, result);
    }

    @DisplayName("When .. is given getFacilitiesRequired return the correct facilities Required")
    @Test
    void getFacilitiesRequired() {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder().build();
        List<String> result = hearingsDetailsMapping.getFacilitiesRequired(caseData);
        List<String> expected = new ArrayList<>();

        assertEquals(0, result.size());
        assertEquals(expected, result);
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

        String result = hearingsDetailsMapping.getListingComments(appeal, otherParties);

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

        String result = hearingsDetailsMapping.getListingComments(appeal, otherParties);

        assertNull(result);
    }

    @DisplayName("getHearingRequester Test")
    @Test
    void getHearingRequester() {
        String result = hearingsDetailsMapping.getHearingRequester();

        assertNull(result);
    }

    @DisplayName("When .. is given getLeadJudgeContractType returns the correct LeadJudgeContractType")
    @Test
    void getLeadJudgeContractType() {
        // TODO Finish Test when method done
        String result = hearingsDetailsMapping.getLeadJudgeContractType();

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

        PanelRequirements result = hearingsDetailsMapping.getPanelRequirements(caseData);

        assertEquals(expected, result);
    }

    @DisplayName("When .. is given getPanelPreferences returns the correct List of PanelPreferences")
    @Test
    void getPanelPreferences() {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder().build();
        List<PanelPreference> result = hearingsDetailsMapping.getPanelPreferences(caseData);
        List<PanelPreference> expected = new ArrayList<>();

        assertEquals(0, result.size());
        assertEquals(expected, result);
    }


    @DisplayName("getHearingDuration Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "null,null,null,null,30",
        "061,null,null,null,30",
        "null,IssueCode,null,null,30",
        "051,FR,null,null,20",
        "037,IssueCode,null,60,30",
        "061,IssueCode,2,hours,30",
        "061,WI,1,sessions,20",
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
        int result = hearingsDetailsMapping.getHearingDuration(caseData);

        assertEquals(expected, result);
    }
}
