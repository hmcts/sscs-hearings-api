package uk.gov.hmcts.reform.sscs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.InvalidIdException;
import uk.gov.hmcts.reform.sscs.exception.UnhandleableHearingStateException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.model.HearingEvent;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequest;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingCancelRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;
import static uk.gov.hmcts.reform.sscs.ccd.domain.HearingRoute.LIST_ASSIST;
import static uk.gov.hmcts.reform.sscs.ccd.domain.HearingState.CANCEL_HEARING;
import static uk.gov.hmcts.reform.sscs.ccd.domain.HearingState.CREATE_HEARING;
import static uk.gov.hmcts.reform.sscs.ccd.domain.HearingState.UPDATED_CASE;
import static uk.gov.hmcts.reform.sscs.ccd.domain.HearingState.UPDATE_HEARING;
import static uk.gov.hmcts.reform.sscs.ccd.domain.HearingState.PARTY_NOTIFIED;

@ExtendWith(MockitoExtension.class)
class HearingsServiceTest {


    private static final long HEARING_REQUEST_ID = 12345;
    private static final String HMC_STATUS = "TestStatus";
    private static final long VERSION = 1;
    private static final long CASE_ID = 1625080769409918L;
    private static final long MISSING_CASE_ID = 99250807409918L;
    private static final String CANCEL_REASON_TEMP = "AWAITING_LISTING";
    private static final String IDAM_OAUTH2_TOKEN = "TestOauth2Token";
    private static final String SERVICE_AUTHORIZATION = "TestServiceAuthorization";

    private HearingsService hearingsService;
    private HearingWrapper wrapper;
    private HearingRequest request;
    private SscsCaseDetails expectedCaseDetails;

    @Mock
    private HmcHearingApi hmcHearingApi;

    @Mock
    private CcdCaseService ccdCaseService;

    @Mock
    private IdamService idamService;

    @Mock
    private HmcHearingPartiesNotifiedApi hmcHearingPartiesNotifiedApi;

    @BeforeEach
    void setup() {
        openMocks(this);

        SscsCaseData caseData = SscsCaseData.builder()
            .ccdCaseId(String.valueOf(CASE_ID))
                .caseManagementLocation(CaseManagementLocation.builder()
                        .build())
            .appeal(Appeal.builder()
                    .hearingOptions(HearingOptions.builder()
                            .build())
                    .appellant(Appellant.builder()
                            .name(Name.builder()
                                    .build())
                            .build())
                    .build())

            .build();

        wrapper = HearingWrapper.builder()
            .state(CREATE_HEARING)
            .caseData(caseData)
            .caseData(caseData)
            .build();

        request = HearingRequest
                .builder(String.valueOf(CASE_ID))
                .hearingState(CREATE_HEARING)
                .hearingRoute(LIST_ASSIST)
                .build();

        expectedCaseDetails = SscsCaseDetails.builder()
            .data(SscsCaseData.builder()
                .ccdCaseId(String.valueOf(CASE_ID))
                .build())
            .build();

        hearingsService = new HearingsService(hmcHearingApi, ccdCaseService, idamService, hmcHearingPartiesNotifiedApi);
    }

    @DisplayName("When wrapper with a valid Hearing State is given addHearingResponse should run without error")
    @Test
    void processHearingRequest() throws GetCaseException, InvalidIdException {
        given(ccdCaseService.getCaseDetails(String.valueOf(CASE_ID))).willReturn(expectedCaseDetails);

        request.setHearingState(UPDATED_CASE);
        assertThatNoException()
                .isThrownBy(() -> hearingsService.processHearingRequest(request));
    }

    @DisplayName("When wrapper with a PARTY_NOTIFIED Hearing State is given partyNotified should run without error")
    @Test
    void partyNotified() throws GetCaseException, InvalidIdException {
        given(idamService.getIdamTokens())
                .willReturn(IdamTokens.builder()
                        .idamOauth2Token(IDAM_OAUTH2_TOKEN)
                        .serviceAuthorization(SERVICE_AUTHORIZATION)
                        .build());
        given(ccdCaseService.getCaseDetails(String.valueOf(CASE_ID))).willReturn(expectedCaseDetails);

        request.setHearingState(PARTY_NOTIFIED);
        assertThatNoException()
                .isThrownBy(() -> hearingsService.processHearingRequest(request));
    }

    @DisplayName("When wrapper with a invalid Hearing State is given addHearingResponse should throw an UnhandleableHearingState error")
    @ParameterizedTest
    @CsvSource(value = {
        "null",
    }, nullValues = {"null"})
    void processHearingRequestInvalidState(HearingState state) {
        request.setHearingState(state);

        UnhandleableHearingStateException thrown = assertThrows(UnhandleableHearingStateException.class, () -> {
            hearingsService.processHearingRequest(request);
        });

        assertThat(thrown.getMessage()).isNotEmpty();
    }

    @DisplayName("When wrapper with a valid create Hearing State is given addHearingResponse should run without error")
    @Test
    void processHearingWrapperCreate() {
        given(idamService.getIdamTokens())
                .willReturn(IdamTokens.builder()
                        .idamOauth2Token(IDAM_OAUTH2_TOKEN)
                        .serviceAuthorization(SERVICE_AUTHORIZATION)
                        .build());

        given(hmcHearingApi.createHearingRequest(any(), any(), any()))
                .willReturn(HearingResponse.builder().build());

        wrapper.setState(CREATE_HEARING);

        assertThatNoException()
                .isThrownBy(() -> hearingsService.processHearingWrapper(wrapper));
    }

    @DisplayName("When wrapper with a valid create Hearing State is given addHearingResponse should run without error")
    @Test
    void processHearingWrapperUpdate() {
        given(idamService.getIdamTokens())
                .willReturn(IdamTokens.builder()
                        .idamOauth2Token(IDAM_OAUTH2_TOKEN)
                        .serviceAuthorization(SERVICE_AUTHORIZATION)
                        .build());

        given(hmcHearingApi.updateHearingRequest(any(), any(), any(), any()))
                .willReturn(HearingResponse.builder().build());

        wrapper.setState(UPDATE_HEARING);

        assertThatNoException()
                .isThrownBy(() -> hearingsService.processHearingWrapper(wrapper));
    }

    @DisplayName("When wrapper with a valid cancel Hearing State is given addHearingResponse should run without error")
    @Test
    void processHearingWrapperCancel() {
        given(idamService.getIdamTokens())
                .willReturn(IdamTokens.builder()
                        .idamOauth2Token(IDAM_OAUTH2_TOKEN)
                        .serviceAuthorization(SERVICE_AUTHORIZATION)
                        .build());

        given(hmcHearingApi.cancelHearingRequest(any(), any(), any(), any()))
                .willReturn(HearingResponse.builder().build());

        wrapper.setState(CANCEL_HEARING);

        assertThatNoException()
                .isThrownBy(() -> hearingsService.processHearingWrapper(wrapper));
    }

    @DisplayName("When wrapper with a valid HearingResponse is given updateHearingResponse should return updated valid HearingResponse")
    @ParameterizedTest
    @CsvSource(value = {
        "CREATE_HEARING,CREATE_HEARING",
        "UPDATED_CASE,UPDATED_CASE",
    }, nullValues = {"null"})
    void updateHearingResponse(HearingState state, HearingEvent event) throws UpdateCaseException {
        given(ccdCaseService.updateCaseData(
                any(SscsCaseData.class),
                any(EventType.class),
                anyString(), anyString()))
                .willReturn(expectedCaseDetails);

        wrapper.setState(state);

        HearingResponse response = HearingResponse.builder()
                .versionNumber(VERSION)
                .hearingRequestId(HEARING_REQUEST_ID)
                .build();
        assertThatNoException()
                .isThrownBy(() -> hearingsService.hearingResponseUpdate(wrapper, response));

        verify(ccdCaseService, times(1))
                .updateCaseData(
                        any(SscsCaseData.class),
                        eq(event.getEventType()),
                        eq(event.getSummary()),
                        eq(event.getDescription()));

        assertThat(wrapper.getCaseData().getEvents()).isNotEmpty();
        EventDetails eventDetails = wrapper.getCaseData().getEvents().get(0).getValue();
        assertThat(eventDetails.getType()).isNotEmpty();
        assertThat(eventDetails.getDate()).isNotEmpty();
        assertThat(eventDetails.getDateTime()).isNotNull();
        assertThat(eventDetails.getDescription()).isNotEmpty();
    }

    @DisplayName("When wrapper with a valid HearingResponse is given updateHearingResponse should return updated valid HearingResponse")
    @Test
    void updateHearingResponse() throws UpdateCaseException {
        given(ccdCaseService.updateCaseData(
                any(SscsCaseData.class),
                any(EventType.class),
                anyString(),
                anyString()))
                .willThrow(UpdateCaseException.class);

        HearingResponse response = HearingResponse.builder()
                .versionNumber(VERSION)
                .hearingRequestId(HEARING_REQUEST_ID)
                .build();
        assertThatExceptionOfType(UpdateCaseException.class)
                .isThrownBy(() -> hearingsService.hearingResponseUpdate(wrapper, response));
    }

    @DisplayName("sendDeleteHearingRequest should send request successfully")
    @Test
    void sendDeleteHearingRequest() {
        given(idamService.getIdamTokens())
                .willReturn(IdamTokens.builder()
                        .idamOauth2Token(IDAM_OAUTH2_TOKEN)
                        .serviceAuthorization(SERVICE_AUTHORIZATION)
                        .build());

        HearingCancelRequestPayload payload = HearingCancelRequestPayload.builder()
                // .cancellationReasonCode(CANCEL_REASON_TEMP) // TODO: Uncomment when implemented.
                .build();

        HearingResponse response = HearingResponse.builder()
                .hearingCancellationReason(CANCEL_REASON_TEMP)
                .hearingRequestId(HEARING_REQUEST_ID)
                .versionNumber(VERSION)
                .build();

        given(hmcHearingApi.cancelHearingRequest(IDAM_OAUTH2_TOKEN, SERVICE_AUTHORIZATION, String.valueOf(HEARING_REQUEST_ID), payload)).willReturn(response);

        // wrapper.getCaseData().getSchedulingAndListingFields().setCancellationCode(CANCEL_REASON_TEMP); // TODO: Uncomment when implemented
        wrapper.getCaseData().getSchedulingAndListingFields().setActiveHearingId(HEARING_REQUEST_ID);
        wrapper.getCaseData().getSchedulingAndListingFields().setActiveHearingVersionNumber(VERSION);

        HearingResponse result = hearingsService.sendCancelHearingRequest(wrapper);

        assertThat(result).isNotNull();
        // assertThat(result.getHearingCancellationReason()).isEqualTo(CANCEL_REASON_TEMP);  // TODO: Uncomment when implemented
        assertThat(result.getHearingRequestId()).isEqualTo(HEARING_REQUEST_ID);
        assertThat(result.getVersionNumber()).isEqualTo(VERSION);
    }
}
