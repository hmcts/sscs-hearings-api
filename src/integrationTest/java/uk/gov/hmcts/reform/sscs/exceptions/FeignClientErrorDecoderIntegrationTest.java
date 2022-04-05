package uk.gov.hmcts.reform.sscs.exceptions;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.sscs.service.HmcHearingApi;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.sscs.service"})
@ActiveProfiles("integration")
public class FeignClientErrorDecoderIntegrationTest {

    private static final String PATH_HEARING = "/hearing?";
    private static final String STATUS = "status";
    private static final String IDAM_OAUTH2_TOKEN = "test-idam-token";
    private static final String SERVICE_AUTHORIZATION_TOKEN = "test-s2s-token";
    private static final String SERVICE_UNAUTHORIZATION_TOKEN = "unauthorise-test-s2s-token";
    private static final String FIELD_ID = "id";
    private static final String BAD_CASE_ID = "400";
    private static final String FOUND_CASE_ID = "123";
    private static final String NOT_FOUND_CASE_ID = "321";
    private static final String FORBIDDEN_FOUND_CASE_ID = "456";

    @Autowired
    private HmcHearingApi hmcHearingApi;

    static WireMockServer wireMockServer;

    @BeforeAll
    static void setUp() {
        wireMockServer = new WireMockServer(options().port(10010));
        wireMockServer.start();
    }

    @AfterAll
    static void cleanUp() {
        wireMockServer.stop();
    }

    @Test
    public void testMockReturnBadRequest400() {
        wireMockServer.stubFor(WireMock.get(urlEqualTo(PATH_HEARING + FIELD_ID + "=" + BAD_CASE_ID))
                                   .willReturn(aResponse().withStatus(HTTP_BAD_REQUEST)));

        assertThatExceptionOfType(ResponseStatusException.class).isThrownBy(
                () -> hmcHearingApi.getHearingRequest(
                    IDAM_OAUTH2_TOKEN,
                    SERVICE_AUTHORIZATION_TOKEN,
                    BAD_CASE_ID))
            .extracting(STATUS).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testMockReturnUnauthorised401() {
        wireMockServer.stubFor(WireMock.get(urlEqualTo(PATH_HEARING + FIELD_ID + "=" + FOUND_CASE_ID))
                                   .willReturn(aResponse().withStatus(HTTP_UNAUTHORIZED)));

        assertThatExceptionOfType(ResponseStatusException.class).isThrownBy(
                () -> hmcHearingApi.getHearingRequest(
                    IDAM_OAUTH2_TOKEN,
                    SERVICE_UNAUTHORIZATION_TOKEN,
                    FOUND_CASE_ID))
            .extracting(STATUS).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void testMockReturnForbidden403() {
        wireMockServer.stubFor(WireMock.get(urlEqualTo(PATH_HEARING + FIELD_ID + "=" + FORBIDDEN_FOUND_CASE_ID))
                                   .willReturn(aResponse().withStatus(HTTP_FORBIDDEN)));

        assertThatExceptionOfType(ResponseStatusException.class).isThrownBy(
                () -> hmcHearingApi.getHearingRequest(
                    IDAM_OAUTH2_TOKEN,
                    SERVICE_AUTHORIZATION_TOKEN,
                    FORBIDDEN_FOUND_CASE_ID))
            .extracting(STATUS).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void testMockReturnNotFound404() {
        wireMockServer.stubFor(WireMock.get(urlEqualTo(PATH_HEARING + FIELD_ID + "=" + NOT_FOUND_CASE_ID))
                    .willReturn(aResponse().withStatus(HTTP_NOT_FOUND)));

        assertThatExceptionOfType(ResponseStatusException.class).isThrownBy(
                () -> hmcHearingApi.getHearingRequest(
                    IDAM_OAUTH2_TOKEN,
                    SERVICE_AUTHORIZATION_TOKEN,
                    NOT_FOUND_CASE_ID))
            .extracting(STATUS).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
