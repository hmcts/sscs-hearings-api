package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.service.linkedcases.LinkedCase;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

class LinkedCasesMappingTest {

    public static final long CASE_ID = 99250807409918L;
    public static final String CASE_NAME = "Test Case Name";

    @DisplayName("When a case data is given with a linked case getLinkedCases returns any linked cases stored")
    @Test
    void getLinkedCases() {
        List<CaseLink> linkedCases = new ArrayList<>();
        linkedCases.add(CaseLink.builder()
                .value(CaseLinkDetails.builder()
                        .caseReference(String.valueOf(CASE_ID))
                        .build())
                .build());
        SscsCaseData caseData = SscsCaseData.builder()
                .linkedCase(linkedCases)
                .caseAccessManagementFields(setCaseAccessManagementFields())
                .build();

        List<LinkedCase> result = LinkedCasesMapping.getLinkedCases(caseData);

        assertThat(result)
            .isNotEmpty()
            .extracting("caseReference")
            .containsOnly(String.valueOf(CASE_ID));

        assertThat(result)
            .isNotEmpty()
            .extracting("caseName")
            .containsOnly(String.valueOf(CASE_NAME));

        List <String> reasonsForLinkTest = new ArrayList<>();

        assertEquals(reasonsForLinkTest, result.get(0).getReasonsForLink());
    }

    @DisplayName("When a case data is given with a linkedCase with an null or a null value getLinkedCases returns any valid linked cases stored without error")
    @Test
    void getLinkedCasesNullValue() {
        List<CaseLink> linkedCases = new ArrayList<>();
        linkedCases.add(CaseLink.builder()
                .value(CaseLinkDetails.builder()
                        .caseReference(String.valueOf(CASE_ID))
                        .build())
                .build());
        linkedCases.add(null);
        linkedCases.add(CaseLink.builder().build());
        SscsCaseData caseData = SscsCaseData.builder()
                .linkedCase(linkedCases)
                .caseAccessManagementFields(setCaseAccessManagementFields())
                .build();

        List<LinkedCase> result = LinkedCasesMapping.getLinkedCases(caseData);

        assertThat(result)
            .isNotEmpty()
            .extracting("caseReference")
            .containsOnly(String.valueOf(CASE_ID));

        assertThat(result)
            .isNotEmpty()
            .extracting("caseName")
            .containsOnly(String.valueOf(CASE_NAME));

        List <String> reasonsForLinkTest = new ArrayList<>();

        assertEquals(reasonsForLinkTest, result.get(0).getReasonsForLink());
    }

    @DisplayName("When a case data is given with a linkedCase that has a blank or null case reference getLinkedCases returns any valid linked cases stored without error")
    @Test
    void getLinkedCasesBlankCaseReference() {
        List<CaseLink> linkedCases = new ArrayList<>();
        linkedCases.add(CaseLink.builder()
                .value(CaseLinkDetails.builder()
                        .caseReference(String.valueOf(CASE_ID))
                        .build())
                .build());
        linkedCases.add(CaseLink.builder()
                .value(CaseLinkDetails.builder().build())
                .build());
        linkedCases.add(CaseLink.builder()
                .value(CaseLinkDetails.builder()
                        .caseReference("")
                        .build())
                .build());
        SscsCaseData caseData = SscsCaseData.builder()
                .linkedCase(linkedCases)
                .caseAccessManagementFields(setCaseAccessManagementFields())
                .build();

        List<LinkedCase> result = LinkedCasesMapping.getLinkedCases(caseData);

        assertThat(result)
            .isNotEmpty()
            .extracting("caseReference")
            .containsOnly(String.valueOf(CASE_ID));

        assertThat(result)
            .isNotEmpty()
            .extracting("caseName")
            .containsOnly(String.valueOf(CASE_NAME));

        List <String> reasonsForLinkTest = new ArrayList<>();

        assertEquals(reasonsForLinkTest, result.get(0).getReasonsForLink());
    }

    @DisplayName("When a case data is given with a empty linkedCase object getLinkedCases returns an empty list")
    @Test
    void getLinkedCasesEmptyLinkedCase() {
        SscsCaseData caseData = SscsCaseData.builder()
                .linkedCase(new ArrayList<>())
                .build();

        List<LinkedCase> result = LinkedCasesMapping.getLinkedCases(caseData);

        assertThat(result).isEmpty();
    }

    @DisplayName("When a case data is given with a null linkedCase object getLinkedCases returns an empty list")
    @Test
    void getLinkedCasesNullLinkedCase() {
        SscsCaseData caseData = SscsCaseData.builder().build();

        List<LinkedCase> result = LinkedCasesMapping.getLinkedCases(caseData);

        assertThat(result).isEmpty();
    }

    private CaseAccessManagementFields setCaseAccessManagementFields() {
        CaseAccessManagementFields caseAccessManagementFields = new CaseAccessManagementFields();
        caseAccessManagementFields.setCaseNames(CASE_NAME);

        return caseAccessManagementFields;

    }
}
