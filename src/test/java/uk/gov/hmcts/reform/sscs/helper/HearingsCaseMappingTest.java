package uk.gov.hmcts.reform.sscs.helper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import uk.gov.hmcts.reform.sscs.model.single.hearing.CaseCategory;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.NO;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.YES;
import static uk.gov.hmcts.reform.sscs.helper.HearingsCaseMapping.buildCaseCategories;

class HearingsCaseMappingTest {

    public static final long HEARING_REQUEST_ID = 12345;
    public static final String HMC_STATUS = "TestStatus";
    public static final long VERSION = 1;
    public static final String CANCELLATION_REASON_CODE = "TestCancelCode";
    private static final long CASE_ID = 1625080769409918L;
    private static final long MISSING_CASE_ID = 99250807409918L;

    @Value("${exui.url}")
    private static String exUiUrl;

    @DisplayName("When a valid hearing wrapper is given buildHearingCaseDetails returns the correct Hearing Case Details")
    @Test
    void buildHearingCaseDetails() {
        // TODO Finish Test when method done
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
        boolean result = HearingsCaseMapping.shouldBeAdditionalSecurityFlag(caseData);

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

    @DisplayName("shouldBeSensitiveFlag Test")
    @Test
    void shouldBeSensitiveFlag() {
        // TODO Finish Test when method done
        boolean result = HearingsCaseMapping.shouldBeSensitiveFlag();

        assertFalse(result);
    }

    @DisplayName("When case ID is given getCaseDeepLink returns the correct link")
    @Test
    void getCaseDeepLink() {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder().ccdCaseId(String.valueOf(CASE_ID)).build();
        String result = HearingsCaseMapping.getCaseDeepLink(caseData);
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
        String result = HearingsCaseMapping.getCaseManagementLocationCode(location);
        String expected = null;

        assertEquals(expected, result);
    }

    @DisplayName("isInterpreterRequired Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {"Yes,true", "No,false", "null,false"}, nullValues = {"null"})
    void isInterpreterRequired(String adjournCaseInterpreterRequired, boolean expected) {
        boolean result = HearingsCaseMapping.isInterpreterRequired(adjournCaseInterpreterRequired);

        assertEquals(expected, result);
    }
}
