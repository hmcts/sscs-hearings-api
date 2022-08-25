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
import uk.gov.hmcts.reform.sscs.model.partiesnotified.PartiesNotifiedRequestPayload;
import uk.gov.hmcts.reform.sscs.service.HmcPartiesNotifiedApi;
import uk.gov.hmcts.reform.sscs.utility.BasePactTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.FIELD_ERRORS;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.FIELD_MESSAGE;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.FIELD_STATUS;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.FORBIDDEN_HEARING_ID;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.IDAM_OAUTH2_TOKEN;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.MSG_400_HEARING;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.MSG_401_HEARING;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.MSG_403_HEARING;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.MSG_404_HEARING;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.NOT_FOUND_HEARING_ID;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.PARTIES_NOTIFIED_PATH;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.SERVICE_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.VALID_HEARING_ID;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.VERSION_NUMBER;
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

    public static final String CONSUMER_NAME = "sscs_hearingsPartiesNotifiedApi";
    public static final String PROVIDER_NAME = "hmcPartiesNotifiedServiceProvider";
    private static final String FIELD_STATUS_FORMAT = "%d %s";
    private static final String VERSION = "version";
    private static final String RECEIVED = "received";
    private static final LocalDateTime RECEIVED_DATETIME = LocalDateTime.now();
    private static final String QUERY_FORMAT = "%s=%s&%s=%s";


    @Autowired
    private HmcPartiesNotifiedApi hmcPartiesNotifiedApi;

    @BeforeEach
    public void timeGapBetweenEachTest() throws InterruptedException {
        TimeUnit.SECONDS.sleep(2);
    }

    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact putPartiesNotifiedRequestForValidRequest(PactDslWithProvider builder) {
        return builder.given(CONSUMER_NAME + " successfully putting a PartiesNotified request ")
            .uponReceiving("Request to put PartiesNotified request")
            .path(PARTIES_NOTIFIED_PATH + "/" + VALID_HEARING_ID)
            .method(HttpMethod.PUT.toString())
            .query(String.format(QUERY_FORMAT, VERSION, VERSION_NUMBER, RECEIVED, RECEIVED_DATETIME.format(DateTimeFormatter.ISO_DATE_TIME)))
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
            .path(PARTIES_NOTIFIED_PATH + "/" + VALID_HEARING_ID)
            .method(HttpMethod.PUT.toString())
            .query(String.format(QUERY_FORMAT, VERSION, VERSION_NUMBER, RECEIVED, RECEIVED_DATETIME.format(DateTimeFormatter.ISO_DATE_TIME)))
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
    public RequestResponsePact unauthorisedRequestErrorFromPutPartiesNotified(PactDslWithProvider builder) {
        return builder.given(CONSUMER_NAME
                + " throws unauthorised error while trying to put PartiesNotified")
            .uponReceiving("Request to PUT PartiesNotified for unauthorised PartiesNotified request")
            .path(PARTIES_NOTIFIED_PATH + "/" + VALID_HEARING_ID)
            .method(HttpMethod.PUT.toString())
            .query(String.format(QUERY_FORMAT, VERSION, VERSION_NUMBER, RECEIVED, RECEIVED_DATETIME.format(DateTimeFormatter.ISO_DATE_TIME)))
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
    public RequestResponsePact forbiddenRequestErrorFromPutPartiesNotified(PactDslWithProvider builder) {
        return builder.given(CONSUMER_NAME
                + " throws forbidden error while trying to put PartiesNotified")
            .uponReceiving("Request to PUT PartiesNotified for forbidden PartiesNotified request")
            .path(PARTIES_NOTIFIED_PATH + "/" + FORBIDDEN_HEARING_ID)
            .method(HttpMethod.PUT.toString())
            .query(String.format(QUERY_FORMAT, VERSION, VERSION_NUMBER, RECEIVED, RECEIVED_DATETIME.format(DateTimeFormatter.ISO_DATE_TIME)))
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
    public RequestResponsePact notFoundRequestErrorFromPutPartiesNotified(PactDslWithProvider builder) {
        return builder.given(CONSUMER_NAME
                + " throws not found request error while trying to put PartiesNotified")
            .uponReceiving("Request to PUT PartiesNotified for not found PartiesNotified request")
            .path(PARTIES_NOTIFIED_PATH + "/" + NOT_FOUND_HEARING_ID)
            .method(HttpMethod.PUT.toString())
            .query(String.format(QUERY_FORMAT, VERSION, VERSION_NUMBER, RECEIVED, RECEIVED_DATETIME.format(DateTimeFormatter.ISO_DATE_TIME)))
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
    public void shouldSuccessfullyPutPartiesNotified() {

        hmcPartiesNotifiedApi.updatePartiesNotifiedRequest(
            IDAM_OAUTH2_TOKEN,
            SERVICE_AUTHORIZATION_TOKEN,
            VALID_HEARING_ID,
            VERSION_NUMBER,
            RECEIVED_DATETIME,
            generatePartiesPutRequest()
        );

    }

    @Test
    @PactTestFor(pactMethod = "badRequestErrorFromPutPartiesNotified")
    public void shouldReturn400BadRequestForPutPartiesNotified(MockServer mockServer) {
        executeCall(mockServer, authorisedHeaders, VALID_HEARING_ID,
            generateInvalidPartiesPutRequest(), HttpStatus.BAD_REQUEST);
    }

    @Test
    @PactTestFor(pactMethod = "unauthorisedRequestErrorFromPutPartiesNotified")
    public void shouldReturn401UnauthorisedRequestForPutHearing(MockServer mockServer) {
        executeCall(mockServer, unauthorisedHeaders, VALID_HEARING_ID,
            generatePartiesPutRequest(), HttpStatus.UNAUTHORIZED);
    }

    @Test
    @PactTestFor(pactMethod = "forbiddenRequestErrorFromPutPartiesNotified")
    public void shouldReturn403ForbiddenRequestForPutHearing(MockServer mockServer) {
        executeCall(mockServer, authorisedHeaders, FORBIDDEN_HEARING_ID,
            generatePartiesPutRequest(), HttpStatus.FORBIDDEN);
    }

    @Test
    @PactTestFor(pactMethod = "notFoundRequestErrorFromPutPartiesNotified")
    public void shouldReturn404NotFoundRequestForPutHearing(MockServer mockServer)  {
        executeCall(mockServer, authorisedHeaders, NOT_FOUND_HEARING_ID,
            generatePartiesPutRequest(), HttpStatus.NOT_FOUND);
    }

    private void executeCall(MockServer mockServer, Map<String, String> headers, Long hearingId,
                             PartiesNotifiedRequestPayload payload, HttpStatus status) {
        RestAssured.given().headers(headers)
            .contentType(ContentType.JSON)
            .queryParam(VERSION, VERSION_NUMBER)
            .queryParam(RECEIVED, RECEIVED_DATETIME.format(DateTimeFormatter.ISO_DATE_TIME))
            .body(toJsonString(payload))
            .when()
            .put(mockServer.getUrl() + PARTIES_NOTIFIED_PATH + "/" + hearingId)
            .then().extract().response().prettyPeek()
            .then().statusCode(status.value())
            .and().extract().body().jsonPath();
    }
}
