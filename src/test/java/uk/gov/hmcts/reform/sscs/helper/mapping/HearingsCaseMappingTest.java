package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.CaseCategory;
import uk.gov.hmcts.reform.sscs.model.single.hearing.CaseDetails;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.NO;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsCaseMapping.*;

class HearingsCaseMappingTest extends HearingsMappingBase {

    @DisplayName("When a valid hearing wrapper is given buildHearingCaseDetails returns the correct Hearing Case Details")
    @Test
    void buildHearingCaseDetails() {
        // TODO Finish Test when method done
        List<CcdValue<OtherParty>> otherParties = new ArrayList<>();
        otherParties.add(new CcdValue<>(OtherParty.builder()
                .hearingOptions(HearingOptions.builder().build())
                .appointee(Appointee.builder().build())
                .rep(Representative.builder().build())
                .build()));
        otherParties.add(new CcdValue<>(OtherParty.builder()
                .hearingOptions(HearingOptions.builder().build())
                .appointee(Appointee.builder().build())
                .rep(Representative.builder().build())
                .build()));
        SscsCaseData caseData = SscsCaseData.builder()
                .ccdCaseId(String.valueOf(CASE_ID))
                .benefitCode(BENEFIT_CODE)
                .issueCode(ISSUE_CODE)
                .caseCreated(CASE_CREATED)
                .workAllocationFields(WorkAllocationFields.builder()
                        .caseNameHmctsInternal(CASE_NAME_INTERNAL)
                        .caseNamePublic(CASE_NAME_PUBLIC)
                        .build())
                .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder().build())
                        .appellant(Appellant.builder()
                                .appointee(Appointee.builder().build())
                                .build())
                        .rep(Representative.builder().build())
                        .build())
                .otherParties(otherParties)
                .caseManagementLocation(CaseManagementLocation.builder()
                        .baseLocation(EPIMS_ID)
                        .region(REGION)
                        .build())
                .build();
        HearingWrapper wrapper = HearingWrapper.builder()
                .caseData(caseData)
                .caseData(caseData)
                .build();

        CaseDetails caseDetails = HearingsCaseMapping.buildHearingCaseDetails(wrapper);

        assertNotNull(caseDetails.getCaseId());
        assertNotNull(caseDetails.getCaseDeepLink());
        assertNotNull(caseDetails.getHmctsInternalCaseName());
        assertNotNull(caseDetails.getPublicCaseName());
        assertNotNull(caseDetails.getCaseCategories());
        assertNotNull(caseDetails.getCaseManagementLocationCode());
        assertNotNull(caseDetails.getCaseSlaStartDate());
    }

    @DisplayName("getCaseID Test")
    @Test
    void getCaseID() {
        SscsCaseData caseData = SscsCaseData.builder().ccdCaseId(String.valueOf(CASE_ID)).build();

        String result = HearingsCaseMapping.getCaseID(caseData);
        String expected = String.valueOf(CASE_ID);

        assertEquals(expected, result);
    }

    @DisplayName("When case ID is given getCaseDeepLink returns the correct link")
    @Test
    void getCaseDeepLink() {
        SscsCaseData caseData = SscsCaseData.builder().ccdCaseId(String.valueOf(CASE_ID)).build();
        String result = HearingsCaseMapping.getCaseDeepLink(caseData);
        String expected = String.format("%s/cases/case-details/%s", EX_UI_URL, CASE_ID);

        assertEquals(expected, result);
    }

    @DisplayName("getInternalCaseName Test")
    @Test
    void getInternalCaseName() {
        String caseNameInternal = CASE_NAME_INTERNAL;
        SscsCaseData caseData = SscsCaseData.builder()
                .workAllocationFields(WorkAllocationFields.builder()
                        .caseNameHmctsInternal(caseNameInternal)
                        .build())
                .build();

        String result = HearingsCaseMapping.getInternalCaseName(caseData);

        assertEquals(caseNameInternal, result);
    }

    @DisplayName("getPublicCaseName Test")
    @Test
    void getPublicCaseName() {
        String caseNamePublic = CASE_NAME_PUBLIC;
        SscsCaseData caseData = SscsCaseData.builder()
                .workAllocationFields(WorkAllocationFields.builder()
                        .caseNamePublic(caseNamePublic)
                        .build())
                .build();

        String result = HearingsCaseMapping.getPublicCaseName(caseData);

        assertEquals(caseNamePublic, result);
    }

    @DisplayName("shouldBeAdditionalSecurityFlag Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "Yes,YES,YES,true",
        "Yes,YES,NO,true",
        "Yes,NO,YES,true",
        "Yes,NO,NO,true",
        "No,YES,YES,true",
        "No,YES,NO,true",
        "No,NO,YES,true",
        "No,NO,NO,false",
        "null,YES,YES,true",
        "null,YES,NO,true",
        "null,NO,YES,true",
        "null,NO,NO,false",
    }, nullValues = {"null"})
    void shouldBeAdditionalSecurityFlag(String dwpUcbFlag, YesNo appellantUcb, YesNo otherPartiesUcb, boolean expected) {
        List<CcdValue<OtherParty>> otherParties = new ArrayList<>();
        otherParties.add(CcdValue.<OtherParty>builder()
                .value(OtherParty.builder()
                        .unacceptableCustomerBehaviour(otherPartiesUcb)
                        .build())
                .build());

        SscsCaseData caseData = SscsCaseData.builder()
                .dwpUcb(dwpUcbFlag)
                .appeal(Appeal.builder()
                        .appellant(Appellant.builder()
                                .unacceptableCustomerBehaviour(appellantUcb)
                                .build())
                        .build())
                .otherParties(otherParties)
                .build();
        boolean result = HearingsCaseMapping.shouldBeAdditionalSecurityFlag(caseData);

        assertEquals(expected, result);
    }

    @DisplayName("shouldBeAdditionalSecurityOtherParties when otherParties are not null Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "YES,true",
        "NO,false",
        "null,false",
    }, nullValues = {"null"})
    void shouldBeAdditionalSecurityOtherParties(YesNo ucb, boolean expected) {
        List<CcdValue<OtherParty>> otherParties = new ArrayList<>();

        otherParties.add(CcdValue.<OtherParty>builder()
                .value(OtherParty.builder()
                        .unacceptableCustomerBehaviour(ucb)
                        .build())
                .build());
        boolean result = HearingsCaseMapping.shouldBeAdditionalSecurityOtherParties(otherParties);

        assertEquals(expected, result);
    }

    @DisplayName("shouldBeAdditionalSecurityOtherParties when otherParties are null Test")
    @Test
    void shouldBeAdditionalSecurityOtherParties() {
        boolean result = HearingsCaseMapping.shouldBeAdditionalSecurityOtherParties(null);

        assertThat(result).isFalse();
    }


    @DisplayName("shouldBeAdditionalSecurityParty when Appointee isn't null Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "YES,Yes,true",
        "YES,No,false",
        "YES,null,false",
        "NO,Yes,false",
        "NO,No,false",
        "NO,null,false",
        "null,Yes,false",
        "null,No,false",
        "null,null,false",
    }, nullValues = {"null"})
    void shouldBeAdditionalSecurityPartyAppointee(YesNo ucb, String isAppointee, boolean expected) {
        Appointee appointee = Appointee.builder()
                .unacceptableCustomerBehaviour(ucb)
                .build();
        Appellant appellant = Appellant.builder()
                .unacceptableCustomerBehaviour(NO)
                .isAppointee(isAppointee)
                .appointee(appointee)
                .build();

        boolean result = HearingsCaseMapping.shouldBeAdditionalSecurityParty(appellant, null);

        assertEquals(expected, result);
    }

    @DisplayName("shouldBeAdditionalSecurityParty when Rep isn't null Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "YES,Yes,true",
        "YES,No,false",
        "YES,null,false",
        "NO,Yes,false",
        "NO,No,false",
        "NO,null,false",
        "null,Yes,false",
        "null,No,false",
        "null,null,false",
    }, nullValues = {"null"})
    void shouldBeAdditionalSecurityPartyRepresentative(YesNo ucb, String hasRepresentative, boolean expected) {
        Representative rep = Representative.builder()
                .hasRepresentative(hasRepresentative)
                .unacceptableCustomerBehaviour(ucb)
                .build();
        Appellant appellant = Appellant.builder()
                .unacceptableCustomerBehaviour(NO)
                .isAppointee("No")
                .appointee(null)
                .build();

        boolean result = HearingsCaseMapping.shouldBeAdditionalSecurityParty(appellant, rep);

        assertEquals(expected, result);
    }

    @DisplayName("shouldBeAdditionalSecurityParty when Appointee and Rep are both null Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "YES,Yes,true",
        "YES,No,true",
        "YES,null,true",
        "NO,Yes,false",
        "NO,No,false",
        "NO,null,false",
        "null,Yes,false",
        "null,No,false",
        "null,null,false",
    }, nullValues = {"null"})
    void shouldBeAdditionalSecurityPartyNullAppointeeRep(YesNo ucb, String isAppointee, boolean expected) {
        Appellant appellant = Appellant.builder()
                .unacceptableCustomerBehaviour(ucb)
                .isAppointee(isAppointee)
                .appointee(null)
                .build();
        boolean result = HearingsCaseMapping.shouldBeAdditionalSecurityParty(appellant, null);

        assertEquals(expected, result);
    }

    @DisplayName("shouldBeAdditionalSecurityFlag Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "Yes,Yes,Yes,true",
        "Yes,Yes,No,true",
        "Yes,No,Yes,true",
        "Yes,No,No,true",
        "No,Yes,Yes,true",
        "No,Yes,No,true",
        "No,No,Yes,true",
        "No,No,No,false",
        "null,Yes,Yes,true",
        "null,Yes,No,true",
        "null,No,Yes,true",
        "null,No,No,false",
        ",Yes,Yes,true",
        ",Yes,No,true",
        ",No,Yes,true",
        ",No,No,false",
    }, nullValues = {"null"})
    void isInterpreterRequired(String adjournCaseInterpreter, String appellantInterpreter, String otherPartyInterpreter, boolean expected) {
        List<CcdValue<OtherParty>> otherParties = new ArrayList<>();
        otherParties.add(CcdValue.<OtherParty>builder()
                .value(OtherParty.builder()
                        .hearingOptions(HearingOptions.builder()
                                .languageInterpreter(otherPartyInterpreter)
                                .build())
                        .build())
                .build());

        SscsCaseData caseData = SscsCaseData.builder()
                .adjournCaseInterpreterRequired(adjournCaseInterpreter)
                .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder()
                                .languageInterpreter(appellantInterpreter)
                                .build())
                        .build())
                .otherParties(otherParties)
                .build();
        boolean result = HearingsCaseMapping.isInterpreterRequired(caseData);

        assertEquals(expected, result);
    }

    @DisplayName("isInterpreterRequiredOtherParties when otherParties are not null Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "Yes,true,true",
        "Yes,false,true",
        "No,true,true",
        "No,false,false",
    }, nullValues = {"null"})
    void isInterpreterRequiredOtherParties(String interpreter, boolean signLanguage, boolean expected) {
        List<CcdValue<OtherParty>> otherParties = new ArrayList<>();
        otherParties.add(CcdValue.<OtherParty>builder()
                .value(OtherParty.builder()
                        .hearingOptions(HearingOptions.builder()
                                .languageInterpreter(interpreter)
                                .arrangements(signLanguage ?  List.of("signLanguageInterpreter") : null)
                                .build())
                        .build())
                .build());

        boolean result = HearingsCaseMapping.isInterpreterRequiredOtherParties(otherParties);

        assertEquals(expected, result);
    }

    @DisplayName("isInterpreterRequiredOtherParties when otherParties are null Test")
    @Test
    void isInterpreterRequiredOtherParties() {
        boolean result = HearingsCaseMapping.isInterpreterRequiredOtherParties(null);

        assertThat(result).isFalse();
    }

    @DisplayName("isInterpreterRequiredHearingOptions Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "Yes,signLanguageInterpreter|somethingElse,true",
        "Yes,signLanguageInterpreter,true",
        "Yes,somethingElse,true",
        "Yes,null,true",
        "Yes,,true",
        "No,signLanguageInterpreter|somethingElse,true",
        "No,signLanguageInterpreter,true",
        "No,somethingElse,false",
        "No,null,false",
        "No,,false",
        "null,signLanguageInterpreter|somethingElse,true",
        "null,signLanguageInterpreter,true",
        "null,somethingElse,false",
        "null,null,false",
        "null,,false",
        ",signLanguageInterpreter|somethingElse,true",
        ",signLanguageInterpreter,true",
        ",somethingElse,false",
        ",null,false",
        ",,false",
    }, nullValues = {"null"})
    void isInterpreterRequiredHearingOptions(String interpreter, String arrangements, boolean expected) {
        HearingOptions hearingOptions = HearingOptions.builder()
                .languageInterpreter(interpreter)
                .arrangements(nonNull(arrangements) ?  splitCsvParamArray(arrangements) : null)
                .build();
        boolean result = HearingsCaseMapping.isInterpreterRequiredHearingOptions(hearingOptions);

        assertEquals(expected, result);
    }


    @Test
    void buildCaseCategoriesTest(){
        ReflectionTestUtils.setField(HearingsCaseMapping.class, "sscsServiceCode", "BBA3");
        List<CaseCategory> categories = new ArrayList<>();

        SscsCaseData caseData = SscsCaseData.builder()
            .benefitCode(Benefit.CARERS_ALLOWANCE.getBenefitCode())
            .issueCode("AA")
            .build();

        CaseCategory caseCategory = CaseCategory.builder().categoryType("caseType").categoryValue("BBA3-070").build();
        CaseCategory caseCategoryOne = CaseCategory.builder().categoryType("caseSubType").categoryValue("BBA3-070AA").build();

        categories.add(caseCategory);
        categories.add(caseCategoryOne);

        assertEquals(categories, buildCaseCategories(caseData));
    }

    @DisplayName("When a case with a valid CaseManagementLocation is given getCaseManagementLocationCode returns the correct EPIMS ID")
    @Test
    void getCaseManagementLocationCode() {
        SscsCaseData caseData = SscsCaseData.builder()
                    .caseManagementLocation(CaseManagementLocation.builder()
                    .baseLocation(EPIMS_ID)
                    .region(REGION)
                    .build())
                .build();
        String result = HearingsCaseMapping.getCaseManagementLocationCode(caseData);

        assertEquals(EPIMS_ID, result);
    }

    @DisplayName("shouldBeSensitiveFlag Test")
    @Test
    void shouldBeSensitiveFlag() {
        boolean result = HearingsCaseMapping.shouldBeSensitiveFlag();

        assertFalse(result);
    }

    @DisplayName("getCaseCreated Test")
    @Test
    void getCaseCreated() {
        String caseCreatedDate = "2022-04-01";
        SscsCaseData caseData = SscsCaseData.builder()
                .caseCreated(caseCreatedDate)
                .build();

        String result = HearingsCaseMapping.getCaseCreated(caseData);

        assertEquals(caseCreatedDate, result);
    }
}
