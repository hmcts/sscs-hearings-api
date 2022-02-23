package uk.gov.hmcts.reform.sscs;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static io.restassured.RestAssured.given;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("smoke")
public class SmokeTest {

    @Value("${server.url}")
    private String testUrl;

    @Test
    public void rootShouldReturnStatus200() {

        RequestSpecification requestSpecification = new RequestSpecBuilder()
            .setBaseUri(testUrl)
            .setRelaxedHTTPSValidation()
            .build();

        given(requestSpecification)
            .when()
            .get("/")
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value());
    }
}
