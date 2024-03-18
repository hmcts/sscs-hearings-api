package uk.gov.hmcts.reform.sscs.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.ccd.client.CcdClient;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.SscsCcdConvertService;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.sscs.ccd.domain.EventType.UPDATE_CASE_ONLY;

@ExtendWith(MockitoExtension.class)
class ServiceHearingsServiceTest extends AbstractServiceHearingsServiceTest {

    @Override
    boolean isV2ServiceHearingService() {
        return false;
    }

    @Override
    StartEventResponse testGetServiceHearingValuesNoIdsMocks(IdamService idamService, CcdClient ccdClient, SscsCcdConvertService sscsCcdConvertService,
                                                             SscsCaseDetails caseDetails, CcdCaseService ccdCaseService, Long caseId) throws GetCaseException {
        given(ccdCaseService.getCaseDetails(String.valueOf(caseId))).willReturn(caseDetails);
        return null;
    }

    @Override
    void testGetServiceHearingValuesNoIdsVerifys(CcdCaseService ccdCaseService, CcdClient ccdClient, SscsCcdConvertService sscsCcdConvertService, StartEventResponse startEventResponseMock) throws UpdateCaseException {
        verify(ccdCaseService, times(1)).updateCaseData(any(SscsCaseData.class), eq(UPDATE_CASE_ONLY), anyString(), anyString());
        verify(ccdClient, never()).submitEventForCaseworker(any(IdamTokens.class), anyLong(), any(CaseDataContent.class));
    }
}
