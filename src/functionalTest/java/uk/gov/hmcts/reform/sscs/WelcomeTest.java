package uk.gov.hmcts.reform.sscs;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static net.serenitybdd.rest.SerenityRest.given;
import static org.hamcrest.Matchers.containsString;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
public class WelcomeTest {

    @Value("${targets.instance}")
    protected String testUrl;

    @Before
    public void setUp() {
        RestAssured.baseURI = testUrl;
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.with().contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    public void should_welcome_with_200_response_code_and_welcome_message() {

        Response result = given()
            .get("/");

        result.then().assertThat()
            .statusCode(HttpStatus.OK.value())
            .and().contentType(MediaType.APPLICATION_JSON_VALUE)
            .and().body(containsString("Welcome to sscs-hearings-api"));
    }
}
