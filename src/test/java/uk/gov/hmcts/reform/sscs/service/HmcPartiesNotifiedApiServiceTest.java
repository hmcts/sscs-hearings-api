package uk.gov.hmcts.reform.sscs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.model.partiesnotified.GetPartiesNotifiedResponse;
import uk.gov.hmcts.reform.sscs.model.partiesnotified.PartiesNotifiedRequestPayload;
import uk.gov.hmcts.reform.sscs.model.partiesnotified.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.sscs.model.partiesnotified.ServiceData;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.sscs.model.partiesnotified.NotificationType.LETTER;

@ExtendWith(MockitoExtension.class)
class HmcPartiesNotifiedApiServiceTest {

    private static final String IDAM_OAUTH2_TOKEN = "TestOauth2Token";
    private static final String SERVICE_AUTHORIZATION = "TestServiceAuthorization";
    private static final long VERSION = 1;
    private static final long HEARING_REQUEST_ID = 12345;

    private final LocalDateTime partiesNotifiedDateTime = LocalDateTime.now().minusDays(1);
    private final LocalDateTime responseReceivedDateTime = LocalDateTime.now();

    @Mock
    private HmcPartiesNotifiedApi hmcPartiesNotifiedApi;

    @Mock
    private IdamService idamService;

    @InjectMocks
    private HmcPartiesNotifiedApiService hmcPartiesNotifiedApiService;


    @BeforeEach
    void setUp() {
        given(idamService.getIdamTokens()).willReturn(IdamTokens.builder()
            .serviceAuthorization(SERVICE_AUTHORIZATION)
            .idamOauth2Token(IDAM_OAUTH2_TOKEN)
            .build());
    }

    @DisplayName("When getPartiesNotifiedRequest is given the correct parameters it returns a valid response without error")
    @Test
    void testGetPartiesNotifiedRequest() {
        GetPartiesNotifiedResponse response = GetPartiesNotifiedResponse.builder()
            .responses(List.of(PartiesNotifiedResponse.builder()
                    .partiesNotified(partiesNotifiedDateTime)
                    .requestVersion(VERSION)
                    .responseReceivedDateTime(responseReceivedDateTime)
                    .serviceData(ServiceData.builder()
                        .build())
                .build()))
            .hearingId(HEARING_REQUEST_ID)
            .build();
        given(hmcPartiesNotifiedApi.getPartiesNotifiedRequest(IDAM_OAUTH2_TOKEN, SERVICE_AUTHORIZATION, HEARING_REQUEST_ID))
            .willReturn(response);

        GetPartiesNotifiedResponse result = hmcPartiesNotifiedApiService.getPartiesNotifiedRequest(HEARING_REQUEST_ID);

        assertThat(result)
            .isNotNull()
            .isEqualTo(response);
    }

    @DisplayName("When sendUpdatePartiesNotifiedRequest is given the correct parameters it returns a valid response without error")
    @Test
    void testSendUpdatePartiesNotifiedRequest() {
        ResponseEntity response = ResponseEntity.ok().build();

        PartiesNotifiedRequestPayload payload = PartiesNotifiedRequestPayload.builder()
            .requestVersion(VERSION)
            .serviceData(ServiceData.builder()
                .partyId("1")
                .notificationType(LETTER)
                .build())
            .build();
        given(hmcPartiesNotifiedApi.updatePartiesNotifiedRequest(IDAM_OAUTH2_TOKEN, SERVICE_AUTHORIZATION,
            HEARING_REQUEST_ID, VERSION, responseReceivedDateTime, payload))
            .willReturn(response);

        ResponseEntity result = hmcPartiesNotifiedApiService.sendUpdatePartiesNotifiedRequest(payload,
            HEARING_REQUEST_ID, VERSION, responseReceivedDateTime);

        assertThat(result)
            .isNotNull()
            .isEqualTo(response);
    }
}
