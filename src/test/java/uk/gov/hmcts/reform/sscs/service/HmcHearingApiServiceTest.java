package uk.gov.hmcts.reform.sscs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sscs.exception.GetHearingException;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.model.single.hearing.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class HmcHearingApiServiceTest {

    private static final String IDAM_OAUTH2_TOKEN = "TestOauth2Token";
    private static final String SERVICE_AUTHORIZATION = "TestServiceAuthorization";
    private static final String CANCEL_REASON_TEMP = "AWAITING_LISTING";
    private static final long VERSION = 1;
    private static final long CASE_ID = 1625080769409918L;
    private static final long MISSING_CASE_ID = 99250807409918L;
    private static final String HEARING_ID = "12345";
    private static final long HEARING_REQUEST_ID = 12345;

    @Mock
    private HmcHearingApi hmcHearingApi;

    @Mock
    private AuthTokenGenerator serviceAuthTokenGenerator;

    @Mock
    private IdamService idamService;

    @InjectMocks
    private HmcHearingApiService hmcHearingsService;

    @BeforeEach
    void setUp() {
        given(serviceAuthTokenGenerator.generate()).willReturn(IDAM_OAUTH2_TOKEN);
        given(idamService.generateServiceAuthorization()).willReturn(SERVICE_AUTHORIZATION);
    }

    @DisplayName("When getHearingRequest is given the correct parameters it returns a valid response without error")
    @Test
    void testGetHearingRequest() throws GetHearingException {
        HearingGetResponse response = HearingGetResponse.builder().build();

        given(hmcHearingApi.getHearingRequest(IDAM_OAUTH2_TOKEN, SERVICE_AUTHORIZATION, HEARING_ID))
                .willReturn(response);

        HearingGetResponse result = hmcHearingsService.getHearingRequest(HEARING_ID);

        assertThat(result)
                .isNotNull()
                .isEqualTo(response);
    }

    @DisplayName("When the api getHearingRequest returns a null the correct error and message is thrown")
    @Test
    void testGetHearingRequestNullResponse() {
        given(hmcHearingApi.getHearingRequest(IDAM_OAUTH2_TOKEN, SERVICE_AUTHORIZATION, HEARING_ID))
                .willReturn(null);

        assertThatExceptionOfType(GetHearingException.class)
                .isThrownBy(() -> hmcHearingsService.getHearingRequest(HEARING_ID))
                .withMessageContaining("Failed to retrieve hearing");
    }

    @DisplayName("sendDeleteHearingRequest should send request successfully")
    @Test
    void testSendCreateHearingRequest() {
        HearingRequestPayload payload = HearingRequestPayload.builder()
                .caseDetails(CaseDetails.builder()
                        .caseId(String.valueOf(CASE_ID))
                        .build())
                .build();

        HearingResponse response = HearingResponse.builder()
                .hearingCancellationReason(CANCEL_REASON_TEMP)
                .hearingRequestId(HEARING_REQUEST_ID)
                .versionNumber(VERSION)
                .build();

        given(hmcHearingApi.createHearingRequest(IDAM_OAUTH2_TOKEN, SERVICE_AUTHORIZATION, payload)).willReturn(response);

        HearingResponse result = hmcHearingsService.sendCreateHearingRequest(payload);

        assertThat(result)
                .isNotNull()
                .isEqualTo(response);
    }

    @DisplayName("sendDeleteHearingRequest should send request successfully")
    @Test
    void testSendUpdateHearingRequest() {
        HearingRequestPayload payload = HearingRequestPayload.builder()
                .caseDetails(CaseDetails.builder()
                        .caseId(String.valueOf(CASE_ID))
                        .build())
                .build();

        HearingResponse response = HearingResponse.builder()
                .hearingCancellationReason(CANCEL_REASON_TEMP)
                .hearingRequestId(HEARING_REQUEST_ID)
                .versionNumber(VERSION)
                .build();

        given(hmcHearingApi.updateHearingRequest(IDAM_OAUTH2_TOKEN, SERVICE_AUTHORIZATION, String.valueOf(HEARING_REQUEST_ID), payload)).willReturn(response);

        HearingResponse result = hmcHearingsService.sendUpdateHearingRequest(payload, String.valueOf(HEARING_REQUEST_ID));

        assertThat(result)
                .isNotNull()
                .isEqualTo(response);
    }


    @DisplayName("sendDeleteHearingRequest should send request successfully")
    @Test
    void testSendDeleteHearingRequest() {
        HearingCancelRequestPayload payload = HearingCancelRequestPayload.builder().build();

        HearingResponse response = HearingResponse.builder()
                .hearingCancellationReason(CANCEL_REASON_TEMP)
                .hearingRequestId(HEARING_REQUEST_ID)
                .versionNumber(VERSION)
                .build();

        given(hmcHearingApi.cancelHearingRequest(IDAM_OAUTH2_TOKEN, SERVICE_AUTHORIZATION, String.valueOf(HEARING_REQUEST_ID), payload)).willReturn(response);

        HearingResponse result = hmcHearingsService.sendCancelHearingRequest(payload, String.valueOf(HEARING_REQUEST_ID));

        assertThat(result)
                .isNotNull()
                .isEqualTo(response);
    }
}
