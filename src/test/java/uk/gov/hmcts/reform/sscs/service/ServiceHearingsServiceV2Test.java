package uk.gov.hmcts.reform.sscs.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.ccd.client.CcdClient;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.SscsCcdConvertService;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceHearingsServiceV2Test extends AbstractServiceHearingsServiceTest {

    @Override
    boolean isV2ServiceHearingService() {
        return true;
    }

    @Override
    StartEventResponse testGetServiceHearingValuesNoIdsMocks(IdamService idamService, CcdClient ccdClient, SscsCcdConvertService sscsCcdConvertService, SscsCaseDetails caseDetails, CcdCaseService ccdCaseService, Long caseId) {
        when(idamService.getIdamTokens()).thenReturn(IdamTokens.builder().build());

        StartEventResponse startEventResponseMock = mock(StartEventResponse.class);

        given(ccdClient.startEvent(any(IdamTokens.class), anyLong(), anyString())).willReturn(startEventResponseMock);

        doReturn(CaseDetails.builder().build()).when(startEventResponseMock).getCaseDetails();

        doReturn(caseDetails.getData()).when(sscsCcdConvertService).getCaseData(any());

        doReturn(CaseDataContent.builder().build()).when(sscsCcdConvertService).getCaseDataContent(any(SscsCaseData.class), eq(startEventResponseMock), anyString(), anyString());

        return startEventResponseMock;
    }

    @Override
    void testGetServiceHearingValuesNoIdsVerifys(CcdCaseService ccdCaseService, CcdClient ccdClient, SscsCcdConvertService sscsCcdConvertService, StartEventResponse startEventResponseMock) throws UpdateCaseException {
        verify(sscsCcdConvertService, times(1)).getCaseDataContent(any(SscsCaseData.class), eq(startEventResponseMock), anyString(),anyString());

        verify(ccdClient, times(1)).submitEventForCaseworker(any(IdamTokens.class), anyLong(), any(CaseDataContent.class));

        verify(sscsCcdConvertService, times(1)).getCaseDetails((CaseDetails) any());

        verify(ccdCaseService, never()).updateCaseData(any(), any(), any(), any());

    }
}
