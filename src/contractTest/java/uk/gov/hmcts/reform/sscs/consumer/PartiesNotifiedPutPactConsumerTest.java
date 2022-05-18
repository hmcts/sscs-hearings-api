package uk.gov.hmcts.reform.sscs.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.sscs.model.partiesnotified.PartiesNotified;
import uk.gov.hmcts.reform.sscs.service.HmcHearingPartiesNotifiedApi;
import uk.gov.hmcts.reform.sscs.utility.BasePactTest;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.CONSUMER_NAME;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.FIELD_ERRORS;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.FIELD_ID;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.FIELD_MESSAGE;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.FIELD_STATUS;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.IDAM_OAUTH2_TOKEN;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.MSG_400_HEARING;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.MSG_401_HEARING;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.MSG_403_HEARING;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.MSG_404_HEARING;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.PARTIES_NOTIFIED_PATH;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.PROVIDER_NAME;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.SERVICE_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.VALID_CASE_ID;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.authorisedHeaders;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.generateInvalidPartiesPutRequest;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.generatePartiesPutRequest;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.toJsonString;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.unauthorisedHeaders;


@ExtendWith(PactConsumerTestExt.class)
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.sscs.service"})
@ActiveProfiles("contract")
@SpringBootTest
@PactTestFor(port = "10000")
@PactFolder("pacts")
class PartiesNotifiedPutPactConsumerTest extends BasePactTest {
    public static final String FIELD_STATUS_FORMAT = "%d %s";
    private static final String VERSION = "version";
    private static final Long VERSION_NUMBER = 123L;
    private static final String FORBIDDEN_CASE_ID = "456";
    private static final String NOT_FOUND_CASE_ID = "789";
    public static final String QUERY_FORMAT = "%s=%s&%s=%s";

    @Autowired
    private HmcHearingPartiesNotifiedApi hmcHearingPartiesNotifiedApi;

    @BeforeEach
    public void timeGapBetweenEachTest() throws InterruptedException {
        TimeUnit.SECONDS.sleep(2);
    }

    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact putPartiesNotifiedRequestForValidRequest(PactDslWithProvider builder) throws JsonProcessingException {
        return builder.given(CONSUMER_NAME + " successfully putting a PartiesNotified request ")
                .uponReceiving("Request to put PartiesNotified request")
                .path(PARTIES_NOTIFIED_PATH)
                .method(HttpMethod.PUT.toString())
                .query(String.format(QUERY_FORMAT, FIELD_ID, VALID_CASE_ID, VERSION, VERSION_NUMBER))
                .body(toJsonString(generatePartiesPutRequest()))
                .headers(authorisedHeaders)
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .toPact();
    }

    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact badRequestErrorFromPutPartiesNotified(PactDslWithProvider builder) {
        return builder.given(CONSUMER_NAME
                        + " throws bad request error while trying to put PartiesNotified")
                .uponReceiving("Request to PUT PartiesNotified for bad PartiesNotified request")
                .path(PARTIES_NOTIFIED_PATH)
                .method(HttpMethod.PUT.toString())
                .query(String.format(QUERY_FORMAT, FIELD_ID, VALID_CASE_ID, VERSION, VERSION_NUMBER))
                .body(toJsonString(generateInvalidPartiesPutRequest()))
                .headers(authorisedHeaders)
                .willRespondWith()
                .status(HttpStatus.BAD_REQUEST.value())
                .body(new PactDslJsonBody()
                        .stringType(FIELD_MESSAGE, MSG_400_HEARING)
                        .stringValue(FIELD_STATUS, String.format(FIELD_STATUS_FORMAT, HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase()))
                        .eachLike(FIELD_ERRORS, 1)
                        .closeArray())
                .toPact();
    }

    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact unauthorisedRequestErrorFromPutPartiesNotified(PactDslWithProvider builder) throws JsonProcessingException {
        return builder.given(CONSUMER_NAME
                        + " throws unauthorised error while trying to put PartiesNotified")
                .uponReceiving("Request to PUT PartiesNotified for unauthorised PartiesNotified request")
                .path(PARTIES_NOTIFIED_PATH)
                .method(HttpMethod.PUT.toString())
                .query(String.format(QUERY_FORMAT, FIELD_ID, VALID_CASE_ID, VERSION, VERSION_NUMBER))
                .body(toJsonString(generatePartiesPutRequest()))
                .headers(unauthorisedHeaders)
                .willRespondWith().status(HttpStatus.UNAUTHORIZED.value())
                .body(new PactDslJsonBody().stringType(FIELD_MESSAGE, MSG_401_HEARING)
                        .stringValue(FIELD_STATUS, String.format(FIELD_STATUS_FORMAT, HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase()))
                        .eachLike(FIELD_ERRORS, 1)
                        .closeArray())
                .toPact();
    }

    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact forbiddenRequestErrorFromPutPartiesNotified(PactDslWithProvider builder) throws JsonProcessingException {
        return builder.given(CONSUMER_NAME
                        + " throws forbidden error while trying to put PartiesNotified")
                .uponReceiving("Request to PUT PartiesNotified for forbidden PartiesNotified request")
                .path(PARTIES_NOTIFIED_PATH)
                .method(HttpMethod.PUT.toString())
                .query(String.format(QUERY_FORMAT, FIELD_ID, FORBIDDEN_CASE_ID, VERSION, VERSION_NUMBER))
                .body(toJsonString(generatePartiesPutRequest()))
                .headers(authorisedHeaders)
                .willRespondWith().status(HttpStatus.FORBIDDEN.value())
                .body(new PactDslJsonBody().stringType(FIELD_MESSAGE, MSG_403_HEARING)
                        .stringValue(FIELD_STATUS, String.format(FIELD_STATUS_FORMAT, HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase()))
                        .eachLike(FIELD_ERRORS, 1)
                        .closeArray())
                .toPact();
    }

    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact notFoundRequestErrorFromPutPartiesNotified(PactDslWithProvider builder) throws JsonProcessingException {
        return builder.given(CONSUMER_NAME
                        + " throws not found request error while trying to put PartiesNotified")
                .uponReceiving("Request to PUT PartiesNotified for not found PartiesNotified request")
                .path(PARTIES_NOTIFIED_PATH)
                .method(HttpMethod.PUT.toString())
                .query(String.format(QUERY_FORMAT, FIELD_ID, NOT_FOUND_CASE_ID, VERSION, VERSION_NUMBER))
                .body(toJsonString(generatePartiesPutRequest()))
                .headers(authorisedHeaders)
                .willRespondWith().status(HttpStatus.NOT_FOUND.value())
                .body(new PactDslJsonBody().stringType(FIELD_MESSAGE, MSG_404_HEARING)
                        .stringValue(FIELD_STATUS, String.format(FIELD_STATUS_FORMAT, HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase()))
                        .eachLike(FIELD_ERRORS, 1)
                        .closeArray())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "putPartiesNotifiedRequestForValidRequest")
    public void shouldSuccessfullyPutHearingRequest() throws JsonProcessingException {

        hmcHearingPartiesNotifiedApi.updatePartiesNotifiedHearingRequest(
                IDAM_OAUTH2_TOKEN,
                SERVICE_AUTHORIZATION_TOKEN,
                VALID_CASE_ID,
                VERSION_NUMBER,
                generatePartiesPutRequest()
        );

    }

    @Test
    @PactTestFor(pactMethod = "badRequestErrorFromPutPartiesNotified")
    public void shouldReturn400BadRequestForPutPartiesNotified(MockServer mockServer) {
        executeCall(mockServer, authorisedHeaders, VALID_CASE_ID,
                VERSION_NUMBER, generateInvalidPartiesPutRequest(), HttpStatus.BAD_REQUEST);
    }

    @Test
    @PactTestFor(pactMethod = "unauthorisedRequestErrorFromPutPartiesNotified")
    public void shouldReturn401UnauthorisedRequestForPutHearing(MockServer mockServer) throws JsonProcessingException {
        executeCall(mockServer, unauthorisedHeaders, VALID_CASE_ID,
                VERSION_NUMBER, generatePartiesPutRequest(), HttpStatus.UNAUTHORIZED);
    }

    @Test
    @PactTestFor(pactMethod = "forbiddenRequestErrorFromPutPartiesNotified")
    public void shouldReturn403ForbiddenRequestForPutHearing(MockServer mockServer) throws JsonProcessingException {
        executeCall(mockServer, authorisedHeaders, FORBIDDEN_CASE_ID,
                VERSION_NUMBER, generatePartiesPutRequest(), HttpStatus.FORBIDDEN);
    }

    @Test
    @PactTestFor(pactMethod = "notFoundRequestErrorFromPutPartiesNotified")
    public void shouldReturn404NotFoundRequestForPutHearing(MockServer mockServer) throws JsonProcessingException {
        executeCall(mockServer, authorisedHeaders, NOT_FOUND_CASE_ID,
                VERSION_NUMBER, generatePartiesPutRequest(), HttpStatus.NOT_FOUND);
    }

    private void executeCall(MockServer mockServer, Map<String, String> headers, String caseId,
                             Long version, PartiesNotified payload, HttpStatus status) {
        RestAssured.given().headers(headers)
                .contentType(ContentType.JSON)
                .queryParam(FIELD_ID, caseId)
                .queryParam(VERSION, version)
                .body(toJsonString(payload))
                .when()
                .put(mockServer.getUrl() + PARTIES_NOTIFIED_PATH)
                .then().statusCode(status.value())
                .and().extract()
                .body()
                .jsonPath();
    }
}
