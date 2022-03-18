package uk.gov.hmcts.reform.sscs.helper;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.helper.HearingsMapping;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.NO;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.YES;

class HearingsMappingTest {

    @Value("${exui.url}")
    private static String exUiUrl;

    final String caseId = "122342343434234";

    @Test
    @DisplayName("When a valid hearing wrapper is given updateFlags updates the updatedCaseData in the wrapper "
            + "with the correct flags")
    void updateFlags() {
        // TODO Finish Test when method done
        HearingWrapper wrapper = HearingWrapper.builder()
                .updatedCaseData(SscsCaseData.builder()
                        .autoListFlag(YES)
                        .hearingsInWelshFlag(YES)
                        .additionalSecurityFlag(NO)
                        .sensitiveFlag(YES)
                        .build())
                .build();
        HearingsMapping.updateFlags(wrapper);

        assertEquals(YES, wrapper.getUpdatedCaseData().getAutoListFlag());
        assertEquals(YES, wrapper.getUpdatedCaseData().getHearingsInWelshFlag());
        assertEquals(NO, wrapper.getUpdatedCaseData().getAdditionalSecurityFlag());
        assertEquals(YES, wrapper.getUpdatedCaseData().getSensitiveFlag());
    }

    @Test
    @DisplayName("When a valid hearing wrapper is given createHmcCaseDetails returns the correct HMC Case Details")
    void createHmcCaseDetails() {
        // TODO Finish Test when method done
        HearingWrapper wrapper = HearingWrapper.builder()
                .updatedCaseData(SscsCaseData.builder()
                        .ccdCaseId(caseId)
                        .caseManagementLocation(CaseManagementLocation.builder()
                                .baseLocation("Test")
                                .region("Test")
                                .build())
                        .build())
                .build();
        HmcCaseDetails result = HearingsMapping.createHmcCaseDetails(wrapper);

        assertEquals(String.format("%s/cases/case-details/%s", exUiUrl, caseId), result.getCaseDeepLink());
        assertNull(result.getCaseManagementLocationCode());
    }

    @Test
    @DisplayName("When a valid hearing wrapper is given createHearingRequest returns the correct Hearing Request")
    void createHearingRequest() {
        // TODO Finish Test when method done
        List<Event> events = new ArrayList<>();
        LocalDateTime testDateTime = LocalDateTime.now();
        events.add(Event.builder().value(EventDetails.builder()
                .type(EventType.DWP_RESPOND.getCcdType())
                .date(testDateTime.toString())
                .build()).build());

        HearingWrapper wrapper = HearingWrapper.builder()
                .updatedCaseData(SscsCaseData.builder()
                        .ccdCaseId(caseId)
                        .caseManagementLocation(CaseManagementLocation.builder()
                                .baseLocation("Test")
                                .region("Test")
                                .build())
                        .appeal(Appeal.builder()
                                .hearingOptions(HearingOptions.builder().other("Test Comment").build())
                                .build())
                        .events(events)
                        .autoListFlag(YES)
                        .hearingsInWelshFlag(YES)
                        .sensitiveFlag(YES)
                        .additionalSecurityFlag(YES)
                        .adjournCaseInterpreterRequired("Yes")
                        .urgentCase("Yes")
                        .build())
                .build();
        HearingRequest result = HearingsMapping.createHearingRequest(wrapper);

        assertNotNull(result.getInitialRequestTimestamp());
        assertEquals(YES, result.getAutoListFlag());
        assertEquals(YES, result.getInWelshFlag());
        assertEquals(NO, result.getIsLinkedFlag());
        assertEquals(YES, result.getAdditionalSecurityFlag());
        assertEquals(YES, result.getInterpreterRequiredFlag());
        assertNull(result.getHearingType());
        assertNull(result.getFirstDateTimeMustBe());
        HearingWindowRange expectedDateTime = HearingWindowRange.builder()
                .dateRangeStart(testDateTime.plusMonths(1).toLocalDate()).build();
        assertEquals(expectedDateTime, result.getHearingWindowRange());
        assertEquals(30, result.getDuration());
        assertEquals(HearingPriorityType.HIGH, result.getHearingPriorityType());
        assertEquals(0, result.getHearingLocations().size());
        assertEquals(new ArrayList<>(), result.getHearingLocations());
        assertEquals(0, result.getFacilitiesRequired().size());
        assertEquals(new ArrayList<>(), result.getFacilitiesRequired());
        assertEquals("Test Comment", result.getListingComments());
        assertNull(result.getLeadJudgeContractType());
        assertNotNull(result.getPanelRequirements());
        assertEquals(0, result.getPanelRequirements().getRoleTypes().size());
        assertEquals(new ArrayList<>(), result.getPanelRequirements().getRoleTypes());
        assertEquals(0, result.getPanelRequirements().getAuthorisationSubType().size());
        assertEquals(new ArrayList<>(), result.getPanelRequirements().getAuthorisationSubType());
        assertEquals(0, result.getPanelRequirements().getPanelPreferences().size());
        assertEquals(new ArrayList<>(), result.getPanelRequirements().getPanelPreferences());
        assertEquals(0, result.getPanelRequirements().getPanelSpecialisms().size());
        assertEquals(new ArrayList<>(), result.getPanelRequirements().getPanelSpecialisms());
    }

    @DisplayName("shouldBeAutoListed Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {"YES,YES", "NO,NO", "null,NO"}, nullValues = {"null"})
    void shouldBeAutoListed(YesNo autoListFlag, YesNo expected) {
        // TODO Finish Test when method done
        YesNo result = HearingsMapping.shouldBeAutoListed(autoListFlag);

        assertEquals(expected, result);
    }

    @DisplayName("shouldBeHearingsInWelshFlag Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {"YES,YES", "NO,NO", "null,NO"}, nullValues = {"null"})
    void shouldBeHearingsInWelshFlag(YesNo hearingsInWelshFlag, YesNo expected) {
        YesNo result = HearingsMapping.shouldBeHearingsInWelshFlag(hearingsInWelshFlag);

        assertEquals(expected, result);
    }

    @DisplayName("shouldBeAdditionalSecurityFlag Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {"YES,YES", "NO,NO", "null,NO"}, nullValues = {"null"})
    void shouldBeAdditionalSecurityFlag(YesNo additionalSecurityFlag, YesNo expected) {
        // TODO Finish Test when method done
        YesNo result = HearingsMapping.shouldBeAdditionalSecurityFlag(additionalSecurityFlag);

        assertEquals(expected, result);
    }

    @DisplayName("shouldBeSensitiveFlag Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {"YES,YES", "NO,NO", "null,NO"}, nullValues = {"null"})
    void shouldBeSensitiveFlag(YesNo sensitiveFlag, YesNo expected) {
        // TODO Finish Test when method done
        YesNo result = HearingsMapping.shouldBeSensitiveFlag(sensitiveFlag);

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("When case ID is given getCaseDeepLink returns the correct link")
    void getCaseDeepLink() {
        // TODO Finish Test when method done
        String result = HearingsMapping.getCaseDeepLink(caseId);
        String expected = String.format("%s/cases/case-details/%s", exUiUrl, caseId);

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("When ... is given getCaseManagementLocationCode returns the correct EPIMS ID")
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

    @Test
    @DisplayName("When .. is given getFacilitiesRequired return the correct facilities Required")
    void getFacilitiesRequired() {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder().build();
        List<CcdValue<String>> result = HearingsMapping.getFacilitiesRequired(caseData);
        List<CcdValue<String>> expected = new ArrayList<>();

        assertEquals(0, result.size());
        assertEquals(expected, result);
    }

    @DisplayName("When case with valid DWP_RESPOND event and is auto-listable is given "
            + "getHearingWindowRange returns a window starting within 1 month of the event's date")
    @ParameterizedTest
    @CsvSource(value = {
        "DWP_RESPOND,YES",
    }, nullValues = {"null"})
    void getHearingWindowRange(EventType eventType, YesNo autoListFlag) {
        // TODO Finish Test when method done
        List<Event> events = new ArrayList<>();
        LocalDateTime testDateTime = LocalDateTime.now();
        events.add(Event.builder().value(EventDetails.builder()
                .type(eventType.getCcdType())
                .date(testDateTime.toString())
                .build()).build());

        SscsCaseData caseData = SscsCaseData.builder().autoListFlag(autoListFlag).events(events).build();
        HearingWindowRange result = HearingsMapping.getHearingWindowRange(caseData);

        HearingWindowRange expected = HearingWindowRange.builder()
                .dateRangeStart(testDateTime.plusMonths(1).toLocalDate()).build();
        assertEquals(expected, result);
    }

    @DisplayName("When case with no valid event or is negative auto-listable is given "
            + "getHearingWindowRange returns a null value")
    @ParameterizedTest
    @CsvSource(value = {
        "WITHDRAWN,YES",
        "null,YES",
        "DWP_RESPOND,NO",
        "DWP_RESPOND,null",
    }, nullValues = {"null"})
    void getHearingWindowRangeNullReturn(EventType eventType, YesNo autoListFlag) {
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

        SscsCaseData caseData = SscsCaseData.builder().autoListFlag(autoListFlag).events(events).build();
        HearingWindowRange result = HearingsMapping.getHearingWindowRange(caseData);

        assertNull(result);
    }

    @Test
    @DisplayName("When .. is given getFacilitiesRequired returns the valid LocalDateTime")
    void getFirstDateTimeMustBe() {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder().build();
        LocalDateTime result = HearingsMapping.getFirstDateTimeMustBe(caseData);
        LocalDateTime expected = null;

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("When .. is given getPanelRequirements returns the valid PanelRequirements")
    void getPanelRequirements() {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder().build();

        PanelRequirements expected =  PanelRequirements.builder().build();
        List<CcdValue<String>> roleTypes = new ArrayList<>();
        expected.setRoleTypes(roleTypes);
        List<CcdValue<String>> authorisationSubType = new ArrayList<>();
        expected.setAuthorisationSubType(authorisationSubType);
        List<CcdValue<PanelPreference>> panelPreferences = new ArrayList<>();
        expected.setPanelPreferences(panelPreferences);
        List<CcdValue<String>> panelSpecialisms = new ArrayList<>();
        expected.setPanelSpecialisms(panelSpecialisms);

        PanelRequirements result = HearingsMapping.getPanelRequirements(caseData);

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("When .. is given getPanelPreferences returns the correct List of PanelPreferences")
    void getPanelPreferences() {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder().build();
        List<CcdValue<PanelPreference>> result = HearingsMapping.getPanelPreferences(caseData);
        List<CcdValue<PanelPreference>> expected = new ArrayList<>();

        assertEquals(0, result.size());
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("When .. is given getLeadJudgeContractType returns the correct LeadJudgeContractType")
    void getLeadJudgeContractType() {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder().build();
        String result = HearingsMapping.getLeadJudgeContractType(caseData);
        String expected = null;

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

    @Disabled
    @DisplayName("getHearingLocations Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {"TBD,TBD,[{CLUSTER,TBD}]"}, nullValues = {"null"})
    void getHearingLocations(String baseLocation, String region,
                             List<Pair<HearingLocationType,String>> expectedParams) {
        // TODO Finish Test when method done

        CaseManagementLocation managementLocation = CaseManagementLocation.builder()
                .baseLocation(baseLocation)
                .region(region)
                .build();
        List<CcdValue<HearingLocation>> result = HearingsMapping.getHearingLocations(managementLocation);
        List<CcdValue<HearingLocation>> expected = new ArrayList<>();

        assertEquals(0, result.size());
        assertEquals(expected, result);
    }

    @DisplayName("getHearingPriority Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "Yes,Yes,HIGH", "Yes,No,HIGH", "No,Yes,HIGH",
        "No,No,NORMAL",
        "Yes,null,HIGH", "No,null,NORMAL",
        "null,Yes,HIGH", "null,No,NORMAL",
        "null,null,NORMAL",
        "Yes,,HIGH", "No,,NORMAL",
        ",Yes,HIGH", ",No,NORMAL",
        ",,NORMAL"
    }, nullValues = {"null"})
    void getHearingPriority(String isAdjournCase, String isUrgentCase, HearingPriorityType expected) {
        // TODO Finish Test when method done
        HearingPriorityType result = HearingsMapping.getHearingPriority(isAdjournCase, isUrgentCase);

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

    @Test
    @DisplayName("When .. is given getHearingType returns the correct Hearing Type")
    void getHearingType() {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder().build();
        SscsHearingType result = HearingsMapping.getHearingType(caseData);
        SscsHearingType expected = null;

        assertEquals(expected, result);
    }

    @DisplayName("isInterpreterRequired Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {"Yes,YES", "No,NO", "null,NO"}, nullValues = {"null"})
    void isInterpreterRequired(String adjournCaseInterpreterRequired, YesNo expected) {
        YesNo result = HearingsMapping.isInterpreterRequired(adjournCaseInterpreterRequired);

        assertEquals(expected, result);
    }


    @Test
    @DisplayName("When .. is given isCaseLinked returns if case is linked")
    void isCaseLinked() {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder().build();
        YesNo result = HearingsMapping.isCaseLinked(caseData);
        YesNo expected = NO;

        assertEquals(expected, result);
    }
}
