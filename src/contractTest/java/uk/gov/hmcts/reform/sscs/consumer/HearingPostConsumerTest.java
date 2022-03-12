package uk.gov.hmcts.reform.sscs.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.sscs.BasePactTesting;
import uk.gov.hmcts.reform.sscs.model.HearingRequestPayload;
import uk.gov.hmcts.reform.sscs.service.HmcHearingApi;
import uk.gov.hmcts.reform.sscs.utility.HearingResponsePactUtil;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(PactConsumerTestExt.class)
public class HearingPostConsumerTest extends BasePactTesting {

    private static final String PATH_HEARING = "/hearing";
    private static final String FIELD_STATUS = "status";
    private static final String BAD_REQUEST = "BAD_REQUEST";
    private static final String FIELD_MESSAGE = "message";
    private static final String FIELD_ERRORS = "errors";

    @Autowired
    private HmcHearingApi hmcHearingApi;
    HearingRequestPayload validRequest = generateHearingRequest();
    String jsonValidRequest = toJsonString(validRequest);

    HearingRequestPayload invalidRequest = generateInvalidHearingRequest();
    String jsonInvalidRequest = toJsonString(invalidRequest);

    static Map<String, String> headers = Map.of(
        HttpHeaders.AUTHORIZATION, IDAM_OAUTH2_TOKEN,
        SERVICE_AUTHORIZATION, SERVICE_AUTHORIZATION_TOKEN
    );

    @Pact(consumer = CONSUMER_NAME)
    public RequestResponsePact createHearingRequestForValidRequest(PactDslWithProvider builder) {
        return builder
            .given(CONSUMER_NAME + " successfully creating hearing request ")
            .uponReceiving("Request to create hearing request to save details")
            .path(PATH_HEARING)
            .method(HttpMethod.POST.toString())
            .body(jsonValidRequest)
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .body(HearingResponsePactUtil.generatePostHearingsJsonBody(MSG_200_POST_HEARING))
            .toPact();
    }

    @Pact(consumer = CONSUMER_NAME)
    public RequestResponsePact validationErrorFromPostHearing(PactDslWithProvider builder) {
        return builder
            .given(CONSUMER_NAME + " throws validation error while trying to create hearing")
            .uponReceiving("Request to CREATE hearing for invalid hearing request")
            .path(PATH_HEARING)
            .method(HttpMethod.POST.toString())
            .body(jsonInvalidRequest)
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.BAD_REQUEST.value())
            .body(new PactDslJsonBody()
                      .stringType(FIELD_MESSAGE, MSG_400_POST_HEARING)
                      .stringValue(FIELD_STATUS, BAD_REQUEST)
                      .eachLike(FIELD_ERRORS, 1)
                      .closeArray()
            )
            .toPact();
    }


    @Test
    @PactTestFor(pactMethod = "createHearingRequestForValidRequest")
    public void shouldSuccessfullyPostHearingRequest(MockServer mockServer) {
        JsonPath response = RestAssured
            .given()
            .headers(headers)
            .contentType(io.restassured.http.ContentType.JSON)
            .body(jsonValidRequest)
            .when()
            .post(mockServer.getUrl() + PATH_HEARING)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract()
            .body()
            .jsonPath();
        assertThat(response.getString("hearingRequestID")).isNotEmpty();
        assertThat(response.getString("status")).isNotEmpty();
        assertThat(response.getString("timeStamp")).isNotEmpty();
        assertThat(response.getString("versionNumber")).isNotEmpty();
        assertThat(response.getString("status_message")).isEqualTo(MSG_200_POST_HEARING);
    }


    @Test
    @PactTestFor(pactMethod = "validationErrorFromPostHearing")
    public void shouldReturn400BadRequestForPostHearing(MockServer mockServer) {
        RestAssured
            .given()
            .headers(headers)
            .contentType(io.restassured.http.ContentType.JSON)
            .body(jsonInvalidRequest)
            .when()
            .post(mockServer.getUrl() + PATH_HEARING)
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .and()
            .extract()
            .body()
            .jsonPath();
    }
}
