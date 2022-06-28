package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.model.service.linkedcases.ServiceLinkedCases;
import uk.gov.hmcts.reform.sscs.service.CcdCaseService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LinkedCasesMappingTest {
    @InjectMocks
    private CcdCaseService ccdCaseService;

    @Mock
    private CcdService ccdService;

    @Mock
    IdamService idamService;

    public static final String CASE_ID = "99250807409918";
    public static final String CASE_NAME = "Test Case Name";

    @DisplayName("When a case data is given with a linked case getLinkedCases returns any linked cases stored")
    @Test
    void getLinkedCases() throws GetCaseException {
        List<CaseLink> linkedCases = new ArrayList<>();

        ccdCaseService = Mockito.mock(CcdCaseService.class);
        when(ccdCaseService.getCaseDetails(CASE_ID)).thenReturn(buildSscsCaseData());

        linkedCases.add(CaseLink.builder()
                .value(CaseLinkDetails.builder()
                        .caseReference(CASE_ID)
                        .build())
                .build());
        SscsCaseData caseData = SscsCaseData.builder()
                .linkedCase(linkedCases)
                .caseAccessManagementFields(setCaseAccessManagementFields())
                .build();

        List<ServiceLinkedCases> result = LinkedCasesMapping.getLinkedCasesWithNameAndReasons(caseData, ccdCaseService);

        assertThat(result)
            .isNotEmpty()
            .extracting("caseReference")
            .containsOnly(CASE_ID);

        assertThat(result)
            .isNotEmpty()
            .extracting("caseName")
            .containsOnly(CASE_NAME);

        List<String> reasonsForLinkTest = new ArrayList<>();
        assertEquals(reasonsForLinkTest, result.get(0).getReasonsForLink());
    }

    @DisplayName("When a case data is given with a linkedCase with an null or a null value getLinkedCases returns any valid linked cases stored without error")
    @Test
    void getLinkedCasesNullValue() throws GetCaseException {
        List<CaseLink> linkedCases = new ArrayList<>();

        ccdCaseService = Mockito.mock(CcdCaseService.class);
        when(ccdCaseService.getCaseDetails(CASE_ID)).thenReturn(buildSscsCaseData());

        linkedCases.add(CaseLink.builder()
                .value(CaseLinkDetails.builder()
                        .caseReference(CASE_ID)
                        .build())
                .build());
        linkedCases.add(null);
        linkedCases.add(CaseLink.builder().build());
        SscsCaseData caseData = SscsCaseData.builder()
                .linkedCase(linkedCases)
                .caseAccessManagementFields(setCaseAccessManagementFields())
                .build();

        List<ServiceLinkedCases> result = LinkedCasesMapping.getLinkedCasesWithNameAndReasons(caseData, ccdCaseService);

        assertThat(result)
            .isNotEmpty()
            .extracting("caseReference")
            .containsOnly(CASE_ID);

        assertThat(result)
            .isNotEmpty()
            .extracting("caseName")
            .containsOnly(CASE_NAME);

        List<String> reasonsForLinkTest = new ArrayList<>();
        assertEquals(reasonsForLinkTest, result.get(0).getReasonsForLink());
    }

    @DisplayName("When a case data is given with a linkedCase that has a blank or null case reference getLinkedCases returns any valid linked cases stored without error")
    @Test
    void getLinkedCasesBlankCaseReference() throws GetCaseException {
        List<CaseLink> linkedCases = new ArrayList<>();

        ccdCaseService = Mockito.mock(CcdCaseService.class);
        when(ccdCaseService.getCaseDetails(CASE_ID)).thenReturn(buildSscsCaseData());

        linkedCases.add(CaseLink.builder()
                .value(CaseLinkDetails.builder()
                        .caseReference(CASE_ID)
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

        List<ServiceLinkedCases> result = LinkedCasesMapping.getLinkedCasesWithNameAndReasons(caseData, ccdCaseService);

        assertThat(result)
            .isNotEmpty()
            .extracting("caseReference")
            .containsOnly(CASE_ID, "");

        assertThat(result)
            .isNotEmpty()
            .extracting("caseName")
            .containsOnly(CASE_NAME, "");

        List<String> reasonsForLinkTest = new ArrayList<>();
        assertEquals(reasonsForLinkTest, result.get(0).getReasonsForLink());
    }

    @DisplayName("When a case data is given with a empty linkedCase object getLinkedCases returns an empty list")
    @Test
    void getLinkedCasesEmptyLinkedCase() throws GetCaseException {
        SscsCaseData caseData = SscsCaseData.builder()
                .linkedCase(new ArrayList<>())
                .build();

        List<ServiceLinkedCases> result = LinkedCasesMapping.getLinkedCasesWithNameAndReasons(caseData, ccdCaseService);

        assertThat(result).isEmpty();
    }

    @DisplayName("When a case data is given with a null linkedCase object getLinkedCases returns an empty list")
    @Test
    void getLinkedCasesNullLinkedCase() {
        SscsCaseData caseData = SscsCaseData.builder().build();

        List<ServiceLinkedCases> result = LinkedCasesMapping.getLinkedCases(caseData);

        assertThat(result).isEmpty();
    }

    private CaseAccessManagementFields setCaseAccessManagementFields() {
        CaseAccessManagementFields caseAccessManagementFields = new CaseAccessManagementFields();
        caseAccessManagementFields.setCaseNames(CASE_NAME);

        return caseAccessManagementFields;

    }

    private SscsCaseDetails buildSscsCaseData() {
        SscsCaseData caseData = new SscsCaseData();

        CaseAccessManagementFields caseAccessManagementFields = new CaseAccessManagementFields();
        caseAccessManagementFields.setCaseNames(CASE_NAME);
        caseData.setCaseAccessManagementFields(caseAccessManagementFields);

        return SscsCaseDetails.builder()
            .data(caseData)
            .build();
    }
}
