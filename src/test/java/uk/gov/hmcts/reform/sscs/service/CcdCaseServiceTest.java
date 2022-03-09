package uk.gov.hmcts.reform.sscs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CcdCaseServiceTest {

    private static final long CASE_ID = 1625080769409918L;
    private static final long MISSING_CASE_ID = 99250807409918L;

    private CcdCaseService ccdCaseService;

    @Mock
    private CcdService ccdService;

    @Mock
    private IdamService idamService;

    @BeforeEach
    void setUp() {
        given(idamService.getIdamTokens()).willReturn(IdamTokens.builder().build());
        ccdCaseService = new CcdCaseService(ccdService, idamService);
    }

    @Test
    void shouldReturnCaseDetails() throws GetCaseException {

        SscsCaseDetails expectedCaseDetails =
                SscsCaseDetails.builder().data(SscsCaseData.builder().build()).build();
        given(ccdService.getByCaseId(eq(CASE_ID), any(IdamTokens.class))).willReturn(expectedCaseDetails);

        SscsCaseDetails caseDetails = ccdCaseService.getCaseDetails(CASE_ID);

        assertThat(expectedCaseDetails).isEqualTo(caseDetails);
    }

    @Test
    void shouldThrowGetCaseExceptionWhenNoCaseFound() {

        given(ccdService.getByCaseId(eq(MISSING_CASE_ID), any(IdamTokens.class))).willReturn(null);

        assertThatExceptionOfType(GetCaseException.class).isThrownBy(
                () -> ccdCaseService.getCaseDetails(MISSING_CASE_ID));
    }
}
