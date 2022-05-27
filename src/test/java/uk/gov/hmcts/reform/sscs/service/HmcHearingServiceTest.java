package uk.gov.hmcts.reform.sscs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class HmcHearingServiceTest {

    private static final String SERVICE_AUTH_TOKEN = "a token";
    private static final String SERVICE_AUTHORIZATION = "authorisation";
    private static final String HEARING_ID = "12345";

    @Mock
    private HmcHearingApi hmcHearingApi;

    @Mock
    private AuthTokenGenerator serviceAuthTokenGenerator;

    @Mock
    private IdamService idamService;

    @InjectMocks
    private HmcHearingService getHearingRequest;

    @BeforeEach
    void setUp() {
        given(serviceAuthTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
        given(idamService.generateServiceAuthorization()).willReturn(SERVICE_AUTHORIZATION);
    }

    @DisplayName("When getHearingRequest is given the correct parameters it returns a valid response without error")
    @Test
    void testGetHearingRequest() {
        given(hmcHearingApi.getHearingRequest(SERVICE_AUTH_TOKEN, SERVICE_AUTHORIZATION, HEARING_ID))
                .willReturn(HearingGetResponse.builder().build());

        HearingGetResponse result = getHearingRequest.getHearingRequest(HEARING_ID);

        assertThat(result).isNotNull();
    }
}
