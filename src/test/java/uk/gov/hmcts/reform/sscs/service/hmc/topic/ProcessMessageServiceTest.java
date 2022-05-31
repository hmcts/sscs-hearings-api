package uk.gov.hmcts.reform.sscs.service.hmc.topic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.exception.*;
import uk.gov.hmcts.reform.sscs.model.hmc.message.HearingUpdate;
import uk.gov.hmcts.reform.sscs.model.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RequestDetails;
import uk.gov.hmcts.reform.sscs.service.CcdCaseService;
import uk.gov.hmcts.reform.sscs.service.HmcHearingApiService;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.sscs.ccd.domain.State.UNKNOWN;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.CANCELLED;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.EXCEPTION;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.LISTED;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.ListingStatus.DRAFT;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.ListingStatus.FIXED;

@ExtendWith(MockitoExtension.class)
class ProcessMessageServiceTest {

    public static final String HEARING_ID = "abcdef";
    public static final long CASE_ID = 123L;

    private SscsCaseDetails sscsCaseDetails;
    private SscsCaseData caseData;
    private HearingGetResponse hearingGetResponse;
    private HmcMessage hmcMessage;

    @Mock
    private HmcHearingApiService hmcHearingApiService;

    @Mock
    private CcdCaseService ccdCaseService;

    @Mock
    private CaseStateUpdateService caseStateUpdateService;

    @InjectMocks
    private ProcessMessageService processMessageService;

    @BeforeEach
    void setUp() throws GetHearingException, GetCaseException {
        hearingGetResponse = HearingGetResponse.builder()
                .hearingResponse(HearingResponse.builder().build())
                .requestDetails(RequestDetails.builder().build())
                .build();

        given(hmcHearingApiService.getHearingRequest(HEARING_ID))
                .willReturn(hearingGetResponse);

        caseData = SscsCaseData.builder()
                .state(UNKNOWN)
                .ccdCaseId(String.valueOf(CASE_ID))
                .build();

        sscsCaseDetails = SscsCaseDetails.builder()
                .data(caseData)
                .build();

        given(ccdCaseService.getCaseDetails(CASE_ID))
                .willReturn(sscsCaseDetails);

        hmcMessage = HmcMessage.builder()
                .caseId(CASE_ID)
                .hearingId(HEARING_ID)
                .hearingUpdate(HearingUpdate.builder().build())
                .build();
    }

    @DisplayName("When listing Status is Fixed and and HmcStatus is valid, "
            + "updateListed and updateCaseData are called once")
    @ParameterizedTest
    @EnumSource(value = HmcStatus.class, names = {"LISTED", "UPDATE_SUBMITTED"})
    void testUpdateListed(HmcStatus hmcStatus)
            throws UpdateCaseException, GetHearingException, GetCaseException, InvalidIdException, InvalidHmcMessageException, InvalidMappingException, InvalidHearingDataException {
        // given
        hearingGetResponse.getHearingResponse().setListingStatus(FIXED);

        hmcMessage.getHearingUpdate().setHmcStatus(hmcStatus);

        givenHmcStatusUpdateCaseDataWillReturnSscsCaseDetails(sscsCaseDetails, hmcStatus);

        // when
        processMessageService.processEventMessage(hmcMessage);

        // then
        verify(caseStateUpdateService, times(1))
                .updateListed(hearingGetResponse, hmcMessage, caseData);
        verifyUpdateCaseDataCalledCorrectlyForHmcStatus(caseData, hmcStatus);
    }



    @DisplayName("When listing Status is not Fixed, updateListed and updateCaseData are not called")
    @Test
    void testUpdateListedNotFixed() throws Exception {
        // given
        hearingGetResponse.getHearingResponse().setListingStatus(DRAFT);
        hmcMessage.getHearingUpdate().setHmcStatus(LISTED);

        // when
        processMessageService.processEventMessage(hmcMessage);

        // then
        verify(caseStateUpdateService, never()).updateListed(any(), any(), any());
        verify(ccdCaseService, never()).updateCaseData(any(),any(),any(),any());
    }

    @DisplayName("When listing Status is Fixed but HmcStatus is not valid, updateListed is not called")
    @ParameterizedTest
    @EnumSource(
        value = HmcStatus.class,
        mode = EnumSource.Mode.EXCLUDE,
        names = {"LISTED", "UPDATE_SUBMITTED"})
    void testUpdateListedNotFixed(HmcStatus hmcStatus) throws Exception {
        // given
        hearingGetResponse.getHearingResponse().setListingStatus(FIXED);
        hmcMessage.getHearingUpdate().setHmcStatus(hmcStatus);

        // when
        processMessageService.processEventMessage(hmcMessage);

        // then
        verify(caseStateUpdateService, never()).updateListed(any(), any(), any());
    }

    @DisplayName("When valid listing Status and list assist case status is given, "
            + "updateListed and updateCaseData are called once")
    @Test
    void testShouldSetCcdStateForCancelledHearingsCorrectly() throws Exception {
        // given
        hearingGetResponse.getRequestDetails().setStatus("Cancelled");
        hearingGetResponse.getHearingResponse().setHearingCancellationReason("Withdrawn");
        hmcMessage.getHearingUpdate().setHmcStatus(CANCELLED);

        givenHmcStatusUpdateCaseDataWillReturnSscsCaseDetails(sscsCaseDetails, CANCELLED);

        // when
        processMessageService.processEventMessage(hmcMessage);

        // then
        verify(caseStateUpdateService, times(1))
                .updateCancelled(hearingGetResponse, caseData);
        verifyUpdateCaseDataCalledCorrectlyForHmcStatus(caseData, CANCELLED);
    }

    @DisplayName("When non Cancelled status given in but hearingCancellationReason is valid, "
            + "updateCancelled and updateCaseData are called")
    @Test
    void testUpdateCancelledNonCancelledStatusRequest() throws Exception {
        // given
        hearingGetResponse.getRequestDetails().setStatus("test");
        hearingGetResponse.getHearingResponse().setHearingCancellationReason("Withdrawn");
        hmcMessage.getHearingUpdate().setHmcStatus(CANCELLED);

        givenHmcStatusUpdateCaseDataWillReturnSscsCaseDetails(sscsCaseDetails, CANCELLED);

        // when
        processMessageService.processEventMessage(hmcMessage);

        // then
        verify(caseStateUpdateService, times(1))
                .updateCancelled(hearingGetResponse, caseData);
        verifyUpdateCaseDataCalledCorrectlyForHmcStatus(caseData, CANCELLED);
    }

    @DisplayName("When no cancellation reason is given but status is Cancelled, "
            + "updateCancelled and updateCaseData are not called")
    @Test
    void testUpdateCancelledNullReason() throws Exception {
        // given
        hearingGetResponse.getRequestDetails().setStatus("Cancelled");
        hmcMessage.getHearingUpdate().setHmcStatus(CANCELLED);

        givenHmcStatusUpdateCaseDataWillReturnSscsCaseDetails(sscsCaseDetails, CANCELLED);

        // when
        processMessageService.processEventMessage(hmcMessage);

        // then
        verify(caseStateUpdateService, times(1))
                .updateCancelled(hearingGetResponse, caseData);
        verifyUpdateCaseDataCalledCorrectlyForHmcStatus(caseData, CANCELLED);

    }

    @DisplayName("When invalid status given in request and no cancellation reason is given,"
            + "updateCancelled and updateCaseData are not called")
    @ParameterizedTest
    @ValueSource(strings = {"test"})
    @NullAndEmptySource
    void testUpdateCancelledInvalidStatusNullReason(String value) throws Exception {
        // given
        hearingGetResponse.getRequestDetails().setStatus(value);
        hmcMessage.getHearingUpdate().setHmcStatus(CANCELLED);

        // when
        processMessageService.processEventMessage(hmcMessage);

        // then
        verify(caseStateUpdateService, never()).updateCancelled(any(), any());
        verify(ccdCaseService, never()).updateCaseData(any(),any(),any(),any());
    }

    @DisplayName("When HmcStatus is Exception updateFailed and updateCaseData are called")
    @Test
    void testUpdateCancelledInvalidStatusNullReason() throws Exception {
        // given
        hmcMessage.getHearingUpdate().setHmcStatus(EXCEPTION);

        givenHmcStatusUpdateCaseDataWillReturnSscsCaseDetails(sscsCaseDetails, EXCEPTION);

        // when
        processMessageService.processEventMessage(hmcMessage);

        // then
        verify(caseStateUpdateService, times(1))
                .updateFailed(caseData);
        verifyUpdateCaseDataCalledCorrectlyForHmcStatus(caseData, EXCEPTION);
    }

    @DisplayName("When not listed, updated, canceled or exception nothing is called")
    @Test
    void testInvalidCall() throws Exception {
        // given
        hmcMessage.getHearingUpdate().setHmcStatus(null);

        // when
        processMessageService.processEventMessage(hmcMessage);

        // then
        verify(caseStateUpdateService, never()).updateListed(any(), any(), any());
        verify(caseStateUpdateService, never()).updateCancelled(any(), any());
        verify(caseStateUpdateService, never()).updateFailed(any());
        verify(ccdCaseService, never()).updateCaseData(any(),any(),any(),any());
    }

    private void verifyUpdateCaseDataCalledCorrectlyForHmcStatus(SscsCaseData caseData, HmcStatus hmcStatus) throws UpdateCaseException, InvalidIdException {
        String ccdUpdateDescription = String.format(hmcStatus.getCcdUpdateDescription(), HEARING_ID);
        verify(ccdCaseService, times(1))
                .updateCaseData(caseData,
                        hmcStatus.getCcdUpdateEventType(),
                        hmcStatus.getCcdUpdateSummary(),
                        ccdUpdateDescription);
    }

    private void givenHmcStatusUpdateCaseDataWillReturnSscsCaseDetails(SscsCaseDetails sscsCaseDetails, HmcStatus hmcStatus)
            throws UpdateCaseException, InvalidIdException {
        String ccdUpdateDescription = String.format(hmcStatus.getCcdUpdateDescription(), HEARING_ID);
        given(ccdCaseService.updateCaseData(sscsCaseDetails.getData(),
                hmcStatus.getCcdUpdateEventType(),
                hmcStatus.getCcdUpdateSummary(),
                ccdUpdateDescription))
                .willReturn(sscsCaseDetails);
    }
}
