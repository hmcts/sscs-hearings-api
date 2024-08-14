package uk.gov.hmcts.reform.sscs.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactBuilder;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactDirectory;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.sscs.ContractTestDataProvider;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HmcUpdateResponse;
import uk.gov.hmcts.reform.sscs.service.HmcHearingApi;
import uk.gov.hmcts.reform.sscs.utility.BasePactTest;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.BAD_REQUEST;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.CONSUMER_NAME;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.FIELD_ERRORS;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.FIELD_MESSAGE;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.FIELD_STATUS;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.FORBIDDEN_CASE_ID;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.HEARING_PATH;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.IDAM_OAUTH2_TOKEN;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.MSG_200_HEARING;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.MSG_400_HEARING;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.MSG_401_HEARING;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.MSG_403_HEARING;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.MSG_404_HEARING;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.NOT_FOUND_CASE_ID;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.PROVIDER_NAME;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.SERVICE_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.VALID_CASE_ID;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.ZERO_NUMBER_LENGTH;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.authorisedHeaders;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.generateHearingRequest;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.generateInvalidHearingRequest;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.toJsonString;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.unauthorisedHeaders;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.HEARING_REQUESTED;

@ExtendWith(PactConsumerTestExt.class)
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.sscs.service"})
@ActiveProfiles("contract")
@SpringBootTest
@PactTestFor(port = "10000")
@PactDirectory("pacts")
class HearingPutConsumerTest extends BasePactTest {

    @Autowired
    private HmcHearingApi hmcHearingApi;

    @BeforeEach
    public void timeGapBetweenEachTest() throws InterruptedException {
        TimeUnit.SECONDS.sleep(2);
    }

    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public V4Pact updateHearingRequestForValidRequest(PactBuilder builder) {
        return builder
            .usingLegacyDsl()
            .given(CONSUMER_NAME + " successfully updating hearing request ")
            .uponReceiving("Request to update hearing request to save details")
            .path(HEARING_PATH + "/" + VALID_CASE_ID)
            .method(HttpMethod.PUT.toString())
            .body(toJsonString(generateHearingRequest()))
            .headers(authorisedHeaders).willRespondWith()
            .status(HttpStatus.OK.value())
            .body(generateHearingsJsonBody(MSG_200_HEARING, HEARING_REQUESTED))
            .toPact(V4Pact.class);
    }

    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public V4Pact validationErrorFromPutHearing(PactBuilder builder) {
        return builder
            .usingLegacyDsl()
            .given(CONSUMER_NAME
                                 + " throws validation error while trying to update hearing")
            .uponReceiving("Request to UPDATE hearing for invalid hearing request")
            .path(HEARING_PATH + "/" + VALID_CASE_ID)
            .method(HttpMethod.PUT.toString())
            .body(toJsonString(generateInvalidHearingRequest()))
            .headers(authorisedHeaders)
            .willRespondWith()
             .status(HttpStatus.BAD_REQUEST.value())
            .body(new PactDslJsonBody()
                      .stringType(FIELD_MESSAGE, MSG_400_HEARING)
                      .stringValue(FIELD_STATUS, BAD_REQUEST)
                      .eachLike(FIELD_ERRORS, 1)
                      .closeArray())
            .toPact(V4Pact.class);
    }

    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public V4Pact unauthorisedRequestErrorFromPutHearing(PactBuilder builder) {
        return builder.usingLegacyDsl().given(CONSUMER_NAME
                                 + " throws unauthorised error while trying to update hearing")
            .uponReceiving("Request to UPDATE hearing for unauthorised hearing request")
            .path(HEARING_PATH + "/" + VALID_CASE_ID).method(HttpMethod.PUT.toString())
            .body(toJsonString(generateHearingRequest()))
            .headers(unauthorisedHeaders)
            .willRespondWith().status(HttpStatus.UNAUTHORIZED.value())
            .body(new PactDslJsonBody()
                      .stringType(FIELD_MESSAGE, MSG_401_HEARING)
                      .stringValue(FIELD_STATUS, HttpStatus.UNAUTHORIZED.value()
                          + " " + HttpStatus.UNAUTHORIZED.getReasonPhrase())
                      .eachLike(FIELD_ERRORS, 1)
                      .closeArray())
            .toPact(V4Pact.class);
    }

    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public V4Pact forbiddenRequestErrorFromPutHearing(PactBuilder builder) {
        return builder.usingLegacyDsl().given(CONSUMER_NAME
                                 + " throws forbidden error while trying to updating hearing")
            .uponReceiving("Request to UPDATE hearing for forbidden hearing request")
            .path(HEARING_PATH + "/" + FORBIDDEN_CASE_ID).method(HttpMethod.PUT.toString())
            .body(toJsonString(generateHearingRequest()))
            .headers(authorisedHeaders)
            .willRespondWith().status(HttpStatus.FORBIDDEN.value())
            .body(new PactDslJsonBody()
                      .stringType(FIELD_MESSAGE, MSG_403_HEARING)
                      .stringValue(FIELD_STATUS, HttpStatus.FORBIDDEN.value()
                          + " " + HttpStatus.FORBIDDEN.getReasonPhrase())
                      .eachLike(FIELD_ERRORS, 1)
                      .closeArray())
            .toPact(V4Pact.class);
    }

    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public V4Pact notFoundRequestErrorFromPutHearing(PactBuilder builder) {
        return builder.usingLegacyDsl().given(CONSUMER_NAME
                                 + " throws not found request error while trying to update hearing")
            .uponReceiving("Request to UPDATE hearing for not found hearing request")
            .path(HEARING_PATH + "/" + NOT_FOUND_CASE_ID).method(HttpMethod.PUT.toString())
            .body(toJsonString(generateHearingRequest()))
            .headers(authorisedHeaders)
            .willRespondWith().status(HttpStatus.NOT_FOUND.value())
            .body(new PactDslJsonBody()
                      .stringType(FIELD_MESSAGE, MSG_404_HEARING)
                      .stringValue(FIELD_STATUS, HttpStatus.NOT_FOUND.value()
                          + " " + HttpStatus.NOT_FOUND.getReasonPhrase())
                      .eachLike(FIELD_ERRORS, 1)
                      .closeArray())
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "updateHearingRequestForValidRequest")
    void shouldSuccessfullyPutHearingRequest() {
        HmcUpdateResponse hmcUpdateResponse = hmcHearingApi.updateHearingRequest(
            IDAM_OAUTH2_TOKEN,
            SERVICE_AUTHORIZATION_TOKEN,
            null,
            VALID_CASE_ID,
            generateHearingRequest()
        );

        assertNotNull(hmcUpdateResponse.getHearingRequestId());
        assertThat(hmcUpdateResponse.getStatus()).isEqualTo(HEARING_REQUESTED);
        assertNotNull(hmcUpdateResponse.getVersionNumber());
        assertNotSame(ZERO_NUMBER_LENGTH, hmcUpdateResponse.getVersionNumber());
        assertThat(hmcUpdateResponse.getTimeStamp()).isEqualToIgnoringSeconds(LocalDateTime.parse(ContractTestDataProvider.HEARING_DATE));
    }

    @Test
    @PactTestFor(pactMethod = "validationErrorFromPutHearing")
    void shouldReturn400BadRequestForPutHearing(MockServer mockServer) {

        executeCall(mockServer, authorisedHeaders,
                VALID_CASE_ID,
                           toJsonString(
                           generateInvalidHearingRequest()),
                HttpStatus.BAD_REQUEST.value()
        );
    }

    @Test
    @PactTestFor(pactMethod = "unauthorisedRequestErrorFromPutHearing")
    void shouldReturn401UnauthorisedRequestForPutHearing(MockServer mockServer) {
        executeCall(mockServer, unauthorisedHeaders,
                VALID_CASE_ID,
                           toJsonString(generateHearingRequest()),
                HttpStatus.UNAUTHORIZED.value()
        );
    }

    @Test
    @PactTestFor(pactMethod = "forbiddenRequestErrorFromPutHearing")
    void shouldReturn403ForbiddenRequestForPutHearing(MockServer mockServer) {
        executeCall(mockServer, authorisedHeaders,
                FORBIDDEN_CASE_ID,
                           toJsonString(generateHearingRequest()),
                HttpStatus.FORBIDDEN.value()
        );
    }

    @Test
    @PactTestFor(pactMethod = "notFoundRequestErrorFromPutHearing")
    void shouldReturn404NotFoundRequestForPutHearing(MockServer mockServer) {

        executeCall(mockServer, authorisedHeaders,
                NOT_FOUND_CASE_ID,
                           toJsonString(generateHearingRequest()),
                HttpStatus.NOT_FOUND.value()
        );

    }

    private void executeCall(MockServer mockServer, Map<String, String> headers,
                             String idValue, String hearingRequest,
                             int httpStatus) {
        RestAssured.given().headers(headers)
            .contentType(io.restassured.http.ContentType.JSON)
            .body(hearingRequest)
            .when()
            .put(mockServer.getUrl() + HEARING_PATH + "/" + idValue)
            .then().statusCode(httpStatus)
            .and().extract()
            .body();
    }
}
