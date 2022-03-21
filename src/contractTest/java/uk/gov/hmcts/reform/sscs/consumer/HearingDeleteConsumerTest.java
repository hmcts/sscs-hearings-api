package uk.gov.hmcts.reform.sscs.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.sscs.ContractTestDataProvider;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingResponse;
import uk.gov.hmcts.reform.sscs.service.HmcHearingApi;
import uk.gov.hmcts.reform.sscs.utility.BasePactTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(PactConsumerTestExt.class)
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.sscs.service"})
@ActiveProfiles("contract")
@SpringBootTest
@PactTestFor(port = "10000")
@PactFolder("pacts")
public class HearingDeleteConsumerTest extends ContractTestDataProvider {

    private static final String ID = "id";
    private static final String VALID_CASE_ID = "123";
    private static final String FORBIDDEN_CASE_ID = "456";
    private static final String NOT_FOUND_CASE_ID = "789";

    @Autowired
    private HmcHearingApi hmcHearingApi;

    @Pact(consumer = CONSUMER_NAME)
    public RequestResponsePact deleteHearingRequestForValidRequest(PactDslWithProvider builder) {
        return builder.given(CONSUMER_NAME + " successfully deleting a hearing request ").uponReceiving(
            "Request to delete hearing request")
            .path(PATH_HEARING).method(HttpMethod.DELETE.toString()).query(ID + "=" + VALID_CASE_ID)
            .body(generateHearingDeleteRequest())
            .headers(headers).willRespondWith()
            .status(HttpStatus.OK.value()).body(
                BasePactTest
                .generateHearingsJsonBody(MSG_200_HEARING))
            .toPact();
    }

    @Pact(consumer = CONSUMER_NAME)
    public RequestResponsePact badRequestErrorFromDeleteHearing(PactDslWithProvider builder) {
        return builder.given(CONSUMER_NAME
                                 + " throws bad request error while trying to delete hearing")
                .uponReceiving("Request to DELETE hearing for bad hearing request")
                    .path(PATH_HEARING).method(HttpMethod.DELETE.toString())
            .query(ID + "=" + VALID_CASE_ID)
            .body(toJsonString(generateInvalidHearingRequest())).headers(headers)
            .willRespondWith().status(HttpStatus.BAD_REQUEST.value())
            .body(new PactDslJsonBody().stringType(FIELD_MESSAGE, MSG_400_HEARING)
                      .stringValue(FIELD_STATUS, HttpStatus.BAD_REQUEST.value()
                          + " " + HttpStatus.BAD_REQUEST.getReasonPhrase())
                          .eachLike(FIELD_ERRORS, 1)
                      .closeArray())
            .toPact();
    }

    @Pact(consumer = CONSUMER_NAME)
    public RequestResponsePact unauthorisedRequestErrorFromDeleteHearing(PactDslWithProvider builder) {
        return builder.given(CONSUMER_NAME
                                 + " throws unauthorised error while trying to delete hearing")
            .uponReceiving("Request to DELETE hearing for unauthorised hearing request")
            .path(PATH_HEARING).method(HttpMethod.DELETE.toString())
            .query(ID + "=" + VALID_CASE_ID)
            .body(generateHearingDeleteRequest()).headers(unauthorisedHeaders)
            .willRespondWith().status(HttpStatus.UNAUTHORIZED.value())
            .body(new PactDslJsonBody().stringType(FIELD_MESSAGE, MSG_401_HEARING)
                      .stringValue(FIELD_STATUS, HttpStatus.UNAUTHORIZED.value()
                          + " " + HttpStatus.UNAUTHORIZED.getReasonPhrase())
                      .eachLike(FIELD_ERRORS, 1)
                      .closeArray())
            .toPact();
    }

    @Pact(consumer = CONSUMER_NAME)
    public RequestResponsePact forbiddenRequestErrorFromDeleteHearing(PactDslWithProvider builder) {
        return builder.given(CONSUMER_NAME
                                 + " throws forbidden error while trying to delete hearing")
            .uponReceiving("Request to DELETE hearing for forbidden hearing request")
            .path(PATH_HEARING).method(HttpMethod.DELETE.toString())
            .query(ID + "=" + FORBIDDEN_CASE_ID)
            .body(generateHearingDeleteRequest()).headers(headers)
            .willRespondWith().status(HttpStatus.FORBIDDEN.value())
            .body(new PactDslJsonBody().stringType(FIELD_MESSAGE, MSG_403_HEARING)
                      .stringValue(FIELD_STATUS, HttpStatus.FORBIDDEN.value()
                          + " " + HttpStatus.FORBIDDEN.getReasonPhrase())
                      .eachLike(FIELD_ERRORS, 1)
                      .closeArray())
            .toPact();
    }

    @Pact(consumer = CONSUMER_NAME)
    public RequestResponsePact notFoundRequestErrorFromDeleteHearing(PactDslWithProvider builder) {
        return builder.given(CONSUMER_NAME
                                 + " throws not found request error while trying to delete hearing")
            .uponReceiving("Request to DELETE hearing for not found hearing request")
            .path(PATH_HEARING).method(HttpMethod.DELETE.toString())
            .query(ID + "=" + NOT_FOUND_CASE_ID)
            .body(generateHearingDeleteRequest()).headers(headers)
            .willRespondWith().status(HttpStatus.NOT_FOUND.value())
            .body(new PactDslJsonBody().stringType(FIELD_MESSAGE, MSG_404_HEARING)
                      .stringValue(FIELD_STATUS, HttpStatus.NOT_FOUND.value()
                          + " " + HttpStatus.NOT_FOUND.getReasonPhrase())
                      .eachLike(FIELD_ERRORS, 1)
                      .closeArray())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "deleteHearingRequestForValidRequest")
    public void shouldSuccessfullyDeleteHearingRequest() {
        HearingResponse hearingResponse = hmcHearingApi.deleteHearingRequest(
            IDAM_OAUTH2_TOKEN,
            SERVICE_AUTHORIZATION_TOKEN,
            VALID_CASE_ID,
            generateHearingDeleteRequest()
        );

        assertTrue(hearingResponse.getHearingRequestId() > ZERO_LENGTH);
        assertFalse(hearingResponse.getStatus().isEmpty());
        assertTrue(hearingResponse.getVersionNumber() != ZERO_NUMBER_LENGTH);
        assertTrue(hearingResponse.getTimeStamp() != null);
    }

    @Test
    @PactTestFor(pactMethod = "badRequestErrorFromDeleteHearing")
    public void shouldReturn400BadRequestForDeleteHearing(MockServer mockServer) {
        RestAssured.given().headers(headers)
            .contentType(io.restassured.http.ContentType.JSON)
            .queryParam(ID, VALID_CASE_ID)
            .body(toJsonString(generateInvalidHearingRequest())).when()
            .delete(mockServer.getUrl() + PATH_HEARING)
            .then().statusCode(HttpStatus.BAD_REQUEST.value())
                .and().extract()
                .body()
                    .jsonPath();
    }

    @Test
    @PactTestFor(pactMethod = "unauthorisedRequestErrorFromDeleteHearing")
    public void shouldReturn401UnauthorisedRequestForDeleteHearing(MockServer mockServer) {
        RestAssured.given().headers(unauthorisedHeaders)
            .contentType(io.restassured.http.ContentType.JSON)
            .queryParam(ID, VALID_CASE_ID)
            .body(generateHearingDeleteRequest()).when()
            .delete(mockServer.getUrl() + PATH_HEARING)
            .then().statusCode(HttpStatus.UNAUTHORIZED.value())
            .and().extract()
            .body()
            .jsonPath();
    }

    @Test
    @PactTestFor(pactMethod = "forbiddenRequestErrorFromDeleteHearing")
    public void shouldReturn403ForbiddenRequestForDeleteHearing(MockServer mockServer) {
        RestAssured.given().headers(headers)
            .contentType(io.restassured.http.ContentType.JSON)
            .queryParam(ID, FORBIDDEN_CASE_ID)
            .body(generateHearingDeleteRequest()).when()
            .delete(mockServer.getUrl() + PATH_HEARING)
            .then().statusCode(HttpStatus.FORBIDDEN.value())
            .and().extract()
            .body()
            .jsonPath();
    }

    @Test
    @PactTestFor(pactMethod = "notFoundRequestErrorFromDeleteHearing")
    public void shouldReturn404NotFoundRequestForDeleteHearing(MockServer mockServer) {
        RestAssured.given().headers(headers)
            .contentType(io.restassured.http.ContentType.JSON)
            .queryParam(ID, NOT_FOUND_CASE_ID)
            .body(generateHearingDeleteRequest()).when()
            .delete(mockServer.getUrl() + PATH_HEARING)
            .then().statusCode(HttpStatus.NOT_FOUND.value())
            .and().extract()
            .body()
            .jsonPath();
    }
}
