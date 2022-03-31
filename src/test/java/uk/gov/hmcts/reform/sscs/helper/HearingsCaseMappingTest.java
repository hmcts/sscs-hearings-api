package uk.gov.hmcts.reform.sscs.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.CaseCategory;
import uk.gov.hmcts.reform.sscs.model.single.hearing.CaseDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.NO;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.YES;
import static uk.gov.hmcts.reform.sscs.helper.HearingsCaseMapping.CASE_SUB_TYPE;
import static uk.gov.hmcts.reform.sscs.helper.HearingsCaseMapping.CASE_TYPE;

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
                .originalCaseData(caseData)
                .updatedCaseData(caseData)
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

    @DisplayName("getServiceCode Test")
    @Test
    void getServiceCode() {
        // TODO Finish Test when method done
        String result = HearingsCaseMapping.getServiceCode();
        String expected = SSCS_SERVICE_CODE;

        assertEquals(expected, result);
    }

    @DisplayName("getCaseID Test")
    @Test
    void getCaseID() {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder().ccdCaseId(String.valueOf(CASE_ID)).build();

        String result = HearingsCaseMapping.getCaseID(caseData);
        String expected = String.valueOf(CASE_ID);

        assertEquals(expected, result);
    }

    @DisplayName("When case ID is given getCaseDeepLink returns the correct link")
    @Test
    void getCaseDeepLink() {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder().ccdCaseId(String.valueOf(CASE_ID)).build();
        String result = HearingsCaseMapping.getCaseDeepLink(caseData);
        String expected = String.format("%s/cases/case-details/%s", EX_UI_URL, CASE_ID);

        assertEquals(expected, result);
    }

    @DisplayName("getInternalCaseName Test")
    @Test
    void getInternalCaseName() {
        // TODO Finish Test when method done
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
        // TODO Finish Test when method done
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
        "true,true|true,true",
        "true,false|true,true",
        "true,true|false,true",
        "true,false|false,true",
        "true,false,true",
        "true,true,true",
        "true,null,true",
        "false,true|true,true",
        "false,false|true,true",
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
            for (boolean flag : Arrays.stream(otherPartiesFlags.split(ARRAY_SPLIT_REGEX)).map(Boolean::parseBoolean).collect(Collectors.toList())) {
                otherParties.add(CcdValue.<OtherParty>builder().value(OtherParty.builder().unacceptableCustomerBehaviour(flag ? YES : NO).build()).build());
            }
        }

        SscsCaseData caseData = SscsCaseData.builder()
                .appeal(Appeal.builder()
                        .appellant(Appellant.builder().unacceptableCustomerBehaviour(appellantFlag ? YES : NO).build())
                        .build())
                .otherParties(otherParties)
                .build();
        boolean result = HearingsCaseMapping.shouldBeAdditionalSecurityFlag(caseData);

        assertEquals(expected, result);
    }

    @DisplayName("isInterpreterRequired Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "Yes,Yes,true,true",
        "Yes,Yes,false,true",
        "Yes,No,true,true",
        "Yes,No,false,true",
        "No,Yes,true,true",
        "No,Yes,false,true",
        "No,No,true,true",
        "No,No,false,false",
        "Yes,null,true,true",
        "Yes,null,false,true",
        "null,Yes,true,true",
        "null,Yes,false,true",
        "null,null,true,true",
        "null,null,false,false",
    }, nullValues = {"null"})
    void isInterpreterRequired(String adjournCaseInterpreterRequired, String languageInterpreter, boolean signLanguage, boolean expected) {
        // TODO add tests for otherParties
        List<String> arrangements = new ArrayList<>();
        if (signLanguage) {
            arrangements.add("signLanguageInterpreter");
        }
        SscsCaseData caseData = SscsCaseData.builder()
                .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder()
                                .languageInterpreter(languageInterpreter)
                                .arrangements(arrangements)
                                .build())
                        .build())
                .adjournCaseInterpreterRequired(adjournCaseInterpreterRequired)
                .build();
        boolean result = HearingsCaseMapping.isInterpreterRequired(caseData);

        assertEquals(expected, result);
    }

    @DisplayName("buildCaseCategories Parameterised Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "benefitCode,issueCode", // TODO replace with actual values
    }, nullValues = {"null"})
    void buildCaseCategories(String benefitCode, String issueCode) {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder()
                .benefitCode(benefitCode)
                .issueCode(issueCode)
                .build();

        List<CaseCategory> result = HearingsCaseMapping.buildCaseCategories(caseData);

        assertThat(result)
                .extracting("categoryType", "categoryValue")
                .contains(tuple(CASE_TYPE, benefitCode), tuple(CASE_SUB_TYPE, issueCode));
    }

    @DisplayName("When a case with a valid CaseManagementLocation is given getCaseManagementLocationCode returns the correct EPIMS ID")
    @Test
    void getCaseManagementLocationCode() {
        // TODO Finish Test when method done
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
        // TODO Finish Test when method done
        boolean result = HearingsCaseMapping.shouldBeSensitiveFlag();

        assertFalse(result);
    }

    @DisplayName("getCaseCreated Test")
    @Test
    void getCaseCreated() {
        // TODO Confirm Correct
        String caseCreatedDate = "2022-04-01";
        SscsCaseData caseData = SscsCaseData.builder()
                .caseCreated(caseCreatedDate)
                .build();

        String result = HearingsCaseMapping.getCaseCreated(caseData);

        assertEquals(caseCreatedDate, result);
    }
}
