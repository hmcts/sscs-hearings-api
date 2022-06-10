package uk.gov.hmcts.reform.sscs.service.hmc.topic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.exception.InvalidHmcMessageException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.model.hmc.message.HearingUpdate;
import uk.gov.hmcts.reform.sscs.model.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus;
import uk.gov.hmcts.reform.sscs.model.single.hearing.CaseDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RequestDetails;
import uk.gov.hmcts.reform.sscs.service.CcdCaseService;
import uk.gov.hmcts.reform.sscs.service.HmcHearingApiService;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.sscs.ccd.domain.State.UNKNOWN;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.ADJOURNED;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.CANCELLED;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.EXCEPTION;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.LISTED;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.ListingStatus.DRAFT;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.ListingStatus.FIXED;
import static uk.gov.hmcts.reform.sscs.reference.data.model.CancellationReason.WITHDRAWN;

@ExtendWith(MockitoExtension.class)
class ProcessHmcMessageServiceTest {

    public static final String HEARING_ID = "abcdef";
    public static final long CASE_ID = 123L;

    @Mock
    private HmcHearingApiService hmcHearingApiService;

    @Mock
    private CcdCaseService ccdCaseService;

    @Mock
    private HearingUpdateService hearingUpdateService;

    @InjectMocks
    private ProcessHmcMessageService processHmcMessageService;

    private SscsCaseDetails sscsCaseDetails;
    private SscsCaseData caseData;
    private HearingGetResponse hearingGetResponse;
    private HmcMessage hmcMessage;

    @BeforeEach
    void setUp() {
        hearingGetResponse = HearingGetResponse.builder()
                .requestDetails(RequestDetails.builder().build())
                .hearingDetails(HearingDetails.builder().build())
                .caseDetails(CaseDetails.builder().build())
                .partyDetails(new ArrayList<>())
                .hearingResponse(HearingResponse.builder().build())
                .build();

        caseData = SscsCaseData.builder()
                .state(UNKNOWN)
                .ccdCaseId(String.valueOf(CASE_ID))
                .build();

        sscsCaseDetails = SscsCaseDetails.builder()
                .data(caseData)
                .build();

        hmcMessage = HmcMessage.builder()
                .hmctsServiceCode("BBA3")
                .caseId(CASE_ID)
                .hearingId(HEARING_ID)
                .hearingUpdate(HearingUpdate.builder()
                        .hmcStatus(ADJOURNED)
                        .build())
                .build();
    }

    @DisplayName("When listing Status is Fixed and and HmcStatus is valid, "
            + "updateListed and updateCaseData are called once")
    @ParameterizedTest
    @EnumSource(value = HmcStatus.class, names = {"LISTED", "UPDATE_SUBMITTED"})
    void testUpdateListed(HmcStatus hmcStatus) throws Exception {
        // given

        hearingGetResponse.getRequestDetails().setStatus(hmcStatus);
        hearingGetResponse.getHearingResponse().setListingStatus(FIXED);

        hmcMessage.getHearingUpdate().setHmcStatus(hmcStatus);

        given(hmcHearingApiService.getHearingRequest(HEARING_ID))
                .willReturn(hearingGetResponse);

        givenHmcStatusUpdateCaseDataWillReturnSscsCaseDetails(sscsCaseDetails, hmcStatus);

        given(ccdCaseService.getCaseDetails(CASE_ID))
                .willReturn(sscsCaseDetails);

        // when
        processHmcMessageService.processEventMessage(hmcMessage);

        // then
        verifyUpdateCaseDataCalledCorrectlyForHmcStatus(caseData, hmcStatus);
    }



    @DisplayName("When listing Status is not Fixed, updateListed and updateCaseData are not called")
    @Test
    void testUpdateListedNotFixed() throws Exception {
        // given
        hearingGetResponse.getRequestDetails().setStatus(LISTED);
        hearingGetResponse.getHearingResponse().setListingStatus(DRAFT);
        hmcMessage.getHearingUpdate().setHmcStatus(LISTED);

        given(hmcHearingApiService.getHearingRequest(HEARING_ID))
                .willReturn(hearingGetResponse);

        given(ccdCaseService.getCaseDetails(CASE_ID))
                .willReturn(sscsCaseDetails);

        // when
        processHmcMessageService.processEventMessage(hmcMessage);

        // then
        verify(ccdCaseService, never()).updateCaseData(any(),any(),any(),any());
    }

    @DisplayName("When listing Status is Fixed but HmcStatus is not valid, updateListed is not called")
    @ParameterizedTest
    @EnumSource(
        value = HmcStatus.class,
        mode = EnumSource.Mode.EXCLUDE,
        names = {"LISTED", "UPDATE_SUBMITTED", "AWAITING_LISTING"})
    void testUpdateListedNotFixed(HmcStatus hmcStatus) throws Exception {
        // given
        hearingGetResponse.getRequestDetails().setStatus(hmcStatus);
        hearingGetResponse.getHearingResponse().setListingStatus(FIXED);
        hmcMessage.getHearingUpdate().setHmcStatus(hmcStatus);

        given(hmcHearingApiService.getHearingRequest(HEARING_ID))
                .willReturn(hearingGetResponse);

        given(ccdCaseService.getCaseDetails(CASE_ID))
                .willReturn(sscsCaseDetails);

        // when
        processHmcMessageService.processEventMessage(hmcMessage);

        // then
        verify(ccdCaseService, never()).updateCaseData(any(), any(), any(), any());
    }

    @DisplayName("When valid listing Status and list assist case status is given, "
            + "updateListed and updateCaseData are called once")
    @Test
    void testShouldSetCcdStateForCancelledHearingsCorrectly() throws Exception {
        // given
        hearingGetResponse.getRequestDetails().setStatus(CANCELLED);
        hearingGetResponse.getHearingResponse().setHearingCancellationReason(WITHDRAWN);
        hmcMessage.getHearingUpdate().setHmcStatus(CANCELLED);

        given(hmcHearingApiService.getHearingRequest(HEARING_ID))
                .willReturn(hearingGetResponse);

        givenHmcStatusUpdateCaseDataWillReturnSscsCaseDetails(sscsCaseDetails, CANCELLED);

        given(ccdCaseService.getCaseDetails(CASE_ID))
                .willReturn(sscsCaseDetails);

        // when
        processHmcMessageService.processEventMessage(hmcMessage);

        // then
        verifyUpdateCaseDataCalledCorrectlyForHmcStatus(caseData, CANCELLED);
    }

    @DisplayName("When non Cancelled status given in but hearingCancellationReason is valid, "
            + "updateCancelled and updateCaseData are called")
    @Test
    void testUpdateCancelledNonCancelledStatusRequest() throws Exception {
        // given
        hearingGetResponse.getRequestDetails().setStatus(CANCELLED);
        hearingGetResponse.getHearingResponse().setHearingCancellationReason(WITHDRAWN);
        hmcMessage.getHearingUpdate().setHmcStatus(CANCELLED);

        given(hmcHearingApiService.getHearingRequest(HEARING_ID))
                .willReturn(hearingGetResponse);

        givenHmcStatusUpdateCaseDataWillReturnSscsCaseDetails(sscsCaseDetails, CANCELLED);

        given(ccdCaseService.getCaseDetails(CASE_ID))
                .willReturn(sscsCaseDetails);

        // when
        processHmcMessageService.processEventMessage(hmcMessage);

        // then
        verifyUpdateCaseDataCalledCorrectlyForHmcStatus(caseData, CANCELLED);
    }

    @DisplayName("When no cancellation reason is given but status is Cancelled, "
            + "updateCancelled and updateCaseData are not called")
    @Test
    void testUpdateCancelledNullReason() throws Exception {
        // given
        hearingGetResponse.getRequestDetails().setStatus(CANCELLED);
        hmcMessage.getHearingUpdate().setHmcStatus(CANCELLED);

        given(hmcHearingApiService.getHearingRequest(HEARING_ID))
                .willReturn(hearingGetResponse);

        givenHmcStatusUpdateCaseDataWillReturnSscsCaseDetails(sscsCaseDetails, CANCELLED);

        given(ccdCaseService.getCaseDetails(CASE_ID))
                .willReturn(sscsCaseDetails);

        // when
        processHmcMessageService.processEventMessage(hmcMessage);

        // then
        verifyUpdateCaseDataCalledCorrectlyForHmcStatus(caseData, CANCELLED);

    }

    @DisplayName("When HmcStatus is Exception updateFailed and updateCaseData are called")
    @Test
    void testUpdateCancelledInvalidStatusNullReason() throws Exception {
        // given
        hearingGetResponse.getRequestDetails().setStatus(EXCEPTION);
        hmcMessage.getHearingUpdate().setHmcStatus(EXCEPTION);

        givenHmcStatusUpdateCaseDataWillReturnSscsCaseDetails(sscsCaseDetails, EXCEPTION);

        given(hmcHearingApiService.getHearingRequest(HEARING_ID))
                .willReturn(hearingGetResponse);

        given(ccdCaseService.getCaseDetails(CASE_ID))
                .willReturn(sscsCaseDetails);

        // when
        processHmcMessageService.processEventMessage(hmcMessage);

        // then
        verifyUpdateCaseDataCalledCorrectlyForHmcStatus(caseData, EXCEPTION);
    }

    @DisplayName("When not listed, updated, canceled or exception nothing is called")
    @ParameterizedTest
    @EnumSource(
            value = HmcStatus.class,
            mode = EnumSource.Mode.EXCLUDE,
            names = {"LISTED", "UPDATE_SUBMITTED", "CANCELLED", "EXCEPTION"})
    void testProcessEventMessageInvalidHmcStatus(HmcStatus value) throws Exception {
        // given
        hearingGetResponse.getRequestDetails().setStatus(value);

        hmcMessage.getHearingUpdate().setHmcStatus(value);

        given(ccdCaseService.getCaseDetails(CASE_ID))
                .willReturn(sscsCaseDetails);

        given(hmcHearingApiService.getHearingRequest(HEARING_ID))
                .willReturn(hearingGetResponse);

        // when
        processHmcMessageService.processEventMessage(hmcMessage);

        // then
        verify(ccdCaseService, never()).updateCaseData(any(),any(),any(),any());
    }

    @DisplayName("When HmcStatus given differs from hearingGetResponse status, processEventMessage throws the correct error and message")
    @Test
    void testProcessEventMessageStatusMismatch() throws Exception {
        // given
        hearingGetResponse.getRequestDetails().setStatus(CANCELLED);
        hmcMessage.getHearingUpdate().setHmcStatus(EXCEPTION);

        given(hmcHearingApiService.getHearingRequest(HEARING_ID))
                .willReturn(hearingGetResponse);

        // when + then
        assertThatExceptionOfType(InvalidHmcMessageException.class)
                .isThrownBy(() -> processHmcMessageService.processEventMessage(hmcMessage));
    }

    private void verifyUpdateCaseDataCalledCorrectlyForHmcStatus(SscsCaseData caseData, HmcStatus hmcStatus) throws UpdateCaseException {
        String ccdUpdateDescription = String.format(hmcStatus.getCcdUpdateDescription(), HEARING_ID);
        verify(ccdCaseService, times(1))
                .updateCaseData(caseData,
                        hmcStatus.getEventMapper().apply(hearingGetResponse),
                        hmcStatus.getCcdUpdateSummary(),
                        ccdUpdateDescription);
    }

    private void givenHmcStatusUpdateCaseDataWillReturnSscsCaseDetails(SscsCaseDetails sscsCaseDetails, HmcStatus hmcStatus)
            throws UpdateCaseException {
        String ccdUpdateDescription = String.format(hmcStatus.getCcdUpdateDescription(), HEARING_ID);
        given(ccdCaseService.updateCaseData(sscsCaseDetails.getData(),
                hmcStatus.getEventMapper().apply(hearingGetResponse),
                hmcStatus.getCcdUpdateSummary(),
                ccdUpdateDescription))
                .willReturn(sscsCaseDetails);
    }
}
