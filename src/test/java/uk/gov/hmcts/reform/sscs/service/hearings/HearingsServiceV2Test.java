package uk.gov.hmcts.reform.sscs.service.hearings;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseManagementLocation;
import uk.gov.hmcts.reform.sscs.ccd.domain.Hearing;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingRoute;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingState;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.exception.UnhandleableHearingStateException;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequest;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingCancelRequestPayload;
import uk.gov.hmcts.reform.sscs.service.CcdCaseService;
import uk.gov.hmcts.reform.sscs.service.HmcHearingApiService;

import java.util.ArrayList;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.sscs.ccd.domain.EventType.CASE_UPDATED;

@ExtendWith(MockitoExtension.class)
class HearingsServiceV2Test {

    @Mock
    private HmcHearingApiService hmcHearingApiService;
    @Mock
    private CcdCaseService ccdCaseService;
    @Mock
    private CreateHearingCaseUpdater createHearingCaseUpdater;
    @Mock
    private AdjournCreateHearingCaseUpdater adjournCreateHearingCaseUpdater;
    @Mock
    private UpdateHearingCaseUpdater updateHearingCaseUpdater;
    @InjectMocks
    private HearingsServiceV2 hearingsService;

    private static final long CASE_ID = 1625080769409918L;
    private static final String HEARING_REQUEST_ID = "12345";
    private static final String BENEFIT_CODE = "002";
    private static final String ISSUE_CODE = "DD";
    private static final String PROCESSING_VENUE = "Processing Venue";

    @Test
    void processHearingRequestThrowsExceptionWhenHearingStateIsNull() {
        HearingRequest hearingRequest = HearingRequest.internalBuilder()
            .ccdCaseId(String.valueOf(CASE_ID))
            .hearingRoute(HearingRoute.LIST_ASSIST)
            .build();

        UnhandleableHearingStateException exceptionThrown = assertThrows(
            UnhandleableHearingStateException.class,
            () -> hearingsService.processHearingRequest(hearingRequest)
        );
        assertNotNull(exceptionThrown.getMessage());
    }

    @DisplayName("When wrapper with a valid Hearing State is given addHearingResponse should run without error")
    @ParameterizedTest
    @EnumSource(
        value = HearingState.class,
        names = {"UPDATED_CASE","PARTY_NOTIFIED"})
    void processHearingRequestShouldNotUpdateCaseForUnsupportedHearingsStates(HearingState hearingState) {
        final HearingRequest hearingRequest = HearingRequest.internalBuilder()
            .hearingRoute(HearingRoute.LIST_ASSIST)
            .ccdCaseId(String.valueOf(CASE_ID))
            .hearingState(hearingState).build();

        assertThatNoException().isThrownBy(
            () -> hearingsService.processHearingRequest(hearingRequest));

        verifyNoInteractions(createHearingCaseUpdater, adjournCreateHearingCaseUpdater, updateHearingCaseUpdater, ccdCaseService);
    }

    @Test
    void processHearingRequestForAdjournAndCreateHearing() throws Exception {
        final HearingRequest hearingRequest = HearingRequest.internalBuilder()
            .hearingRoute(HearingRoute.LIST_ASSIST)
            .ccdCaseId(String.valueOf(CASE_ID))
            .hearingState(HearingState.ADJOURN_CREATE_HEARING).build();

        hearingsService.processHearingRequest(hearingRequest);
        verify(adjournCreateHearingCaseUpdater).createHearingAndUpdateCase(eq(hearingRequest));
        verifyNoInteractions(updateHearingCaseUpdater, ccdCaseService);
    }

    @Test
    void processHearingRequestForCreateHearing() throws Exception {
        final HearingRequest hearingRequest = HearingRequest.internalBuilder()
            .hearingRoute(HearingRoute.LIST_ASSIST)
            .ccdCaseId(String.valueOf(CASE_ID))
            .hearingState(HearingState.CREATE_HEARING).build();

        hearingsService.processHearingRequest(hearingRequest);
        verify(createHearingCaseUpdater).createHearingAndUpdateCase(eq(hearingRequest));
        verifyNoInteractions(updateHearingCaseUpdater, ccdCaseService);
    }

    @Test
    void processHearingRequestForUpdateHearing() throws Exception {
        final HearingRequest hearingRequest = HearingRequest.internalBuilder()
            .hearingRoute(HearingRoute.LIST_ASSIST)
            .ccdCaseId(String.valueOf(CASE_ID))
            .hearingState(HearingState.UPDATE_HEARING).build();

        hearingsService.processHearingRequest(hearingRequest);
        verify(updateHearingCaseUpdater).updateHearingAndCase(eq(hearingRequest));
        verifyNoInteractions(createHearingCaseUpdater, adjournCreateHearingCaseUpdater, ccdCaseService);
    }

    @Test
    void processHearingRequestForCancelHearing() throws Exception {
        SscsCaseData sscsCaseData = SscsCaseData.builder()
            .ccdCaseId(String.valueOf(CASE_ID))
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .caseManagementLocation(CaseManagementLocation.builder().build())
            .appeal(Appeal.builder().build())
            .hearings(new ArrayList<>(Collections.singletonList(Hearing.builder()
                                                                    .value(HearingDetails.builder()
                                                                               .hearingId(HEARING_REQUEST_ID)
                                                                               .versionNumber(1L)
                                                                               .build())
                                                                    .build())))
            .processingVenue(PROCESSING_VENUE)
            .build();

        when(ccdCaseService.getStartEventResponse(eq(CASE_ID), eq(CASE_UPDATED)))
            .thenReturn(SscsCaseDetails.builder().data(sscsCaseData).build());

        final HearingRequest hearingRequest = HearingRequest.internalBuilder()
            .hearingRoute(HearingRoute.LIST_ASSIST)
            .ccdCaseId(String.valueOf(CASE_ID))
            .hearingState(HearingState.CANCEL_HEARING).build();

        hearingsService.processHearingRequest(hearingRequest);
        verify(ccdCaseService).getStartEventResponse(eq(CASE_ID), eq(CASE_UPDATED));
        verify(hmcHearingApiService).sendCancelHearingRequest(
            any(HearingCancelRequestPayload.class), eq(HEARING_REQUEST_ID));
        verifyNoInteractions(createHearingCaseUpdater, adjournCreateHearingCaseUpdater, updateHearingCaseUpdater);
    }

    @Test
    void hearingResponseUpdateRecover() {
    }
}
