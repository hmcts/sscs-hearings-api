package uk.gov.hmcts.reform.sscs;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("functional")
public class SecurityConfigurationTest {

    @Value("${server.url}")
    private String testUrl;

    @ParameterizedTest
    @ValueSource(strings = {"/", "/health",
        "/swagger-ui.html", "/swagger-ui/index.html"})
    public void shouldReturnOkForAnonymousPaths(String path) {
        RequestSpecification requestSpecification = new RequestSpecBuilder()
            .setBaseUri(testUrl)
            .setRelaxedHTTPSValidation()
            .build();

        Response requestResponse =   given(requestSpecification)
            .when()
            .get(path);

        assertThat(requestResponse.getStatusCode())
            .as("Anonymous paths do not require authentication to be accessed.")
            .isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void shouldReturnUnauthorisedWithNoAuthHeader() {
        RequestSpecification requestSpecification = new RequestSpecBuilder()
            .setBaseUri(testUrl)
            .setRelaxedHTTPSValidation()
            .build();

        Response requestResponse =   given(requestSpecification)
            .when()
            .get("/hearings");

        assertThat(requestResponse.getStatusCode())
            .as("Non-anonymous paths should be rejected with service authorisation.")
            .isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

}
