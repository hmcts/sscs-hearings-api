package uk.gov.hmcts.reform.sscs.service;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.ccd.service.SscsCcdConvertService;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CcdCaseServiceTest {

    private static final long CASE_ID = 1625080769409918L;
    private static final long MISSING_CASE_ID = 99250807409918L;
    private static final String INVALID_CASE_ID = "pq7409918";
    private static final String SUMMARY = "Update Summary";
    private static final String DESCRIPTION = "Update Description";

    @InjectMocks
    private CcdCaseService ccdCaseService;

    @Mock
    private CcdService ccdService;

    @Mock
    private IdamService idamService;

    @Mock
    private SscsCcdConvertService sscsCcdConvertService;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Test
    void getByCaseId_shouldReturnCaseDetails() throws GetCaseException {
        given(idamService.getIdamTokens()).willReturn(IdamTokens.builder().build());

        SscsCaseDetails expectedCaseDetails =
                SscsCaseDetails.builder().data(SscsCaseData.builder().build()).build();
        given(ccdService.getByCaseId(eq(CASE_ID), any(IdamTokens.class))).willReturn(expectedCaseDetails);

        SscsCaseDetails caseDetails = ccdCaseService.getCaseDetails(CASE_ID);

        assertThat(expectedCaseDetails).isEqualTo(caseDetails);
    }

    @Test
    void getByCaseId_shouldReturnCaseDetailsWithString() throws GetCaseException {
        given(idamService.getIdamTokens()).willReturn(IdamTokens.builder().build());

        SscsCaseDetails expectedCaseDetails =
                SscsCaseDetails.builder().data(SscsCaseData.builder().build()).build();
        given(ccdService.getByCaseId(eq(CASE_ID), any(IdamTokens.class))).willReturn(expectedCaseDetails);

        SscsCaseDetails caseDetails = ccdCaseService.getCaseDetails(String.valueOf(CASE_ID));

        assertThat(expectedCaseDetails).isEqualTo(caseDetails);
    }

    @Test
    void getByCaseId_shouldThrowGetCaseExceptionWhenNoCaseFound() {
        given(idamService.getIdamTokens()).willReturn(IdamTokens.builder().build());
        given(ccdService.getByCaseId(eq(MISSING_CASE_ID), any(IdamTokens.class))).willReturn(null);

        assertThatExceptionOfType(GetCaseException.class).isThrownBy(
                () -> ccdCaseService.getCaseDetails(MISSING_CASE_ID));
    }

    @Test
    void getByCaseId_shouldThrowGetCaseExceptionWhenStringNotLong() {
        assertThatExceptionOfType(NumberFormatException.class).isThrownBy(
                () -> ccdCaseService.getCaseDetails(INVALID_CASE_ID));
    }

    @Test
    void updateCase_shouldUpdateCaseDetails() throws UpdateCaseException {
        given(idamService.getIdamTokens()).willReturn(IdamTokens.builder().build());
        SscsCaseDetails expectedCaseDetails =
                SscsCaseDetails.builder()
                        .data(SscsCaseData.builder()
                                .ccdCaseId(String.valueOf(CASE_ID)).build()).build();
        given(ccdService.updateCase(
                any(SscsCaseData.class), eq(CASE_ID), anyString(), anyString(), anyString(), any(IdamTokens.class)))
                .willReturn(expectedCaseDetails);

        SscsCaseDetails caseDetails = ccdCaseService.updateCaseData(
                expectedCaseDetails.getData(), EventType.READY_TO_LIST, SUMMARY, DESCRIPTION);

        assertThat(expectedCaseDetails).isEqualTo(caseDetails);
    }

    @Test
    void updateCase_shouldThrowUpdateCaseExceptionWhenCaseUpdateFails() {
        given(idamService.getIdamTokens()).willReturn(IdamTokens.builder().build());
        Request request = Request.create(Request.HttpMethod.GET, "url",
                new HashMap<>(), null, new RequestTemplate());

        given(ccdService.updateCase(
                any(SscsCaseData.class), eq(CASE_ID), anyString(), anyString(), anyString(), any(IdamTokens.class)))
                .willThrow(
                        new FeignException.InternalServerError("Test Error", request, null, null));

        SscsCaseDetails testCaseDetails =
                SscsCaseDetails.builder()
                        .data(SscsCaseData.builder()
                                .ccdCaseId(String.valueOf(CASE_ID)).build()).build();

        assertThatExceptionOfType(UpdateCaseException.class).isThrownBy(
                () -> ccdCaseService.updateCaseData(
                        testCaseDetails.getData(), EventType.READY_TO_LIST, SUMMARY, DESCRIPTION));
    }
}
