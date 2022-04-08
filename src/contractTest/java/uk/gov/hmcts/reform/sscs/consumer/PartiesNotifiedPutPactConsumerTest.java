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
import uk.gov.hmcts.reform.sscs.ContractTestDataProvider;
import uk.gov.hmcts.reform.sscs.model.partiesnotified.PartiesNotified;
import uk.gov.hmcts.reform.sscs.service.HmcHearingPartiesNotifiedApi;
import uk.gov.hmcts.reform.sscs.utility.BasePactTest;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.CONSUMER_NAME;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.FIELD_ID;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.PROVIDER_NAME;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.VALID_CASE_ID;

@ExtendWith(PactConsumerTestExt.class)
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.sscs.service"})
@ActiveProfiles("contract")
@SpringBootTest
@PactTestFor(port = "10000")
@PactFolder("pacts")
public class PartiesNotifiedPutPactConsumerTest extends BasePactTest {

    private static final String VERSION = "version";
    private static final String VERSION_NUMBER = "123";
    private static final String FORBIDDEN_CASE_ID = "456";
    private static final String NOT_FOUND_CASE_ID = "789";

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
            .path(ContractTestDataProvider.PARTIES_NOTIFIED_PATH)
            .method(HttpMethod.PUT.toString())
            .query(FIELD_ID + "=" + VALID_CASE_ID)
            .query(VERSION + "=" + VERSION_NUMBER)
            .body(ContractTestDataProvider.toJsonString(ContractTestDataProvider.generatePartiesPutRequest()))
            .headers(ContractTestDataProvider.authorisedHeaders)
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .toPact();
    }

    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact badRequestErrorFromPutPartiesNotified(PactDslWithProvider builder) {
        return builder.given(CONSUMER_NAME
                                 + " throws bad request error while trying to put PartiesNotified")
            .uponReceiving("Request to PUT PartiesNotified for bad PartiesNotified request")
            .path(ContractTestDataProvider.PARTIES_NOTIFIED_PATH)
            .method(HttpMethod.PUT.toString())
            .query(FIELD_ID + "=" + VALID_CASE_ID)
            .query(VERSION + "=" + VERSION_NUMBER)
            .body(ContractTestDataProvider.toJsonString(ContractTestDataProvider.generateInvalidPartiesPutRequest()))
            .headers(ContractTestDataProvider.authorisedHeaders)
            .willRespondWith()
            .status(HttpStatus.BAD_REQUEST.value())
            .body(new PactDslJsonBody()
                      .stringType(ContractTestDataProvider.FIELD_MESSAGE, ContractTestDataProvider.MSG_400_HEARING)
                      .stringValue(ContractTestDataProvider.FIELD_STATUS, HttpStatus.BAD_REQUEST.value()
                          + " " + HttpStatus.BAD_REQUEST.getReasonPhrase())
                      .eachLike(ContractTestDataProvider.FIELD_ERRORS, 1)
                      .closeArray())
            .toPact();
    }

    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact unauthorisedRequestErrorFromPutPartiesNotified(PactDslWithProvider builder) throws JsonProcessingException {
        return builder.given(CONSUMER_NAME
                                 + " throws unauthorised error while trying to put PartiesNotified")
            .uponReceiving("Request to PUT PartiesNotified for unauthorised PartiesNotified request")
            .path(ContractTestDataProvider.PARTIES_NOTIFIED_PATH)
            .method(HttpMethod.PUT.toString())
            .query(FIELD_ID + "=" + VALID_CASE_ID)
            .query(VERSION + "=" + VERSION_NUMBER)
            .body(ContractTestDataProvider.toJsonString(ContractTestDataProvider.generatePartiesPutRequest()))
            .headers(ContractTestDataProvider.unauthorisedHeaders)
            .willRespondWith().status(HttpStatus.UNAUTHORIZED.value())
            .body(new PactDslJsonBody().stringType(ContractTestDataProvider.FIELD_MESSAGE, ContractTestDataProvider.MSG_401_HEARING)
                      .stringValue(ContractTestDataProvider.FIELD_STATUS, HttpStatus.UNAUTHORIZED.value()
                          + " " + HttpStatus.UNAUTHORIZED.getReasonPhrase())
                      .eachLike(ContractTestDataProvider.FIELD_ERRORS, 1)
                      .closeArray())
            .toPact();
    }

    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact forbiddenRequestErrorFromPutPartiesNotified(PactDslWithProvider builder) throws JsonProcessingException {
        return builder.given(CONSUMER_NAME
                                 + " throws forbidden error while trying to put PartiesNotified")
            .uponReceiving("Request to PUT PartiesNotified for forbidden PartiesNotified request")
            .path(ContractTestDataProvider.PARTIES_NOTIFIED_PATH)
            .method(HttpMethod.PUT.toString())
            .query(FIELD_ID + "=" + FORBIDDEN_CASE_ID)
            .query(VERSION + "=" + VERSION_NUMBER)
            .body(ContractTestDataProvider.toJsonString(ContractTestDataProvider.generatePartiesPutRequest()))
            .headers(ContractTestDataProvider.authorisedHeaders)
            .willRespondWith().status(HttpStatus.FORBIDDEN.value())
            .body(new PactDslJsonBody().stringType(ContractTestDataProvider.FIELD_MESSAGE, ContractTestDataProvider.MSG_403_HEARING)
                      .stringValue(ContractTestDataProvider.FIELD_STATUS, HttpStatus.FORBIDDEN.value()
                          + " " + HttpStatus.FORBIDDEN.getReasonPhrase())
                      .eachLike(ContractTestDataProvider.FIELD_ERRORS, 1)
                      .closeArray())
            .toPact();
    }

    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact notFoundRequestErrorFromPutPartiesNotified(PactDslWithProvider builder) throws JsonProcessingException {
        return builder.given(CONSUMER_NAME
                                 + " throws not found request error while trying to put PartiesNotified")
            .uponReceiving("Request to PUT PartiesNotified for not found PartiesNotified request")
            .path(ContractTestDataProvider.PARTIES_NOTIFIED_PATH)
            .method(HttpMethod.PUT.toString())
            .query(FIELD_ID + "=" + NOT_FOUND_CASE_ID)
            .query(VERSION + "=" + VERSION_NUMBER)
            .body(ContractTestDataProvider.toJsonString(ContractTestDataProvider.generatePartiesPutRequest()))
            .headers(ContractTestDataProvider.authorisedHeaders)
            .willRespondWith().status(HttpStatus.NOT_FOUND.value())
            .body(new PactDslJsonBody().stringType(ContractTestDataProvider.FIELD_MESSAGE, ContractTestDataProvider.MSG_404_HEARING)
                      .stringValue(ContractTestDataProvider.FIELD_STATUS, HttpStatus.NOT_FOUND.value()
                          + " " + HttpStatus.NOT_FOUND.getReasonPhrase())
                      .eachLike(ContractTestDataProvider.FIELD_ERRORS, 1)
                      .closeArray())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "putPartiesNotifiedRequestForValidRequest")
    public void shouldSuccessfullyPutHearingRequest() throws JsonProcessingException {

        hmcHearingPartiesNotifiedApi.updatePartiesNotifiedHearingRequest(
            ContractTestDataProvider.IDAM_OAUTH2_TOKEN,
            ContractTestDataProvider.SERVICE_AUTHORIZATION_TOKEN,
            VALID_CASE_ID,
            VERSION_NUMBER,
            ContractTestDataProvider.generatePartiesPutRequest()
        );

    }

    @Test
    @PactTestFor(pactMethod = "badRequestErrorFromPutPartiesNotified")
    public void shouldReturn400BadRequestForPutPartiesNotified(MockServer mockServer) {
        executeCall(mockServer, ContractTestDataProvider.authorisedHeaders, VALID_CASE_ID,
                    VERSION_NUMBER, ContractTestDataProvider.generateInvalidPartiesPutRequest(), HttpStatus.BAD_REQUEST);
    }

    @Test
    @PactTestFor(pactMethod = "unauthorisedRequestErrorFromPutPartiesNotified")
    public void shouldReturn401UnauthorisedRequestForPutHearing(MockServer mockServer) throws JsonProcessingException {
        executeCall(mockServer, ContractTestDataProvider.unauthorisedHeaders, VALID_CASE_ID,
                    VERSION_NUMBER, ContractTestDataProvider.generatePartiesPutRequest(), HttpStatus.UNAUTHORIZED);
    }

    @Test
    @PactTestFor(pactMethod = "forbiddenRequestErrorFromPutPartiesNotified")
    public void shouldReturn403ForbiddenRequestForPutHearing(MockServer mockServer) throws JsonProcessingException {
        executeCall(mockServer, ContractTestDataProvider.authorisedHeaders, FORBIDDEN_CASE_ID,
                    VERSION_NUMBER, ContractTestDataProvider.generatePartiesPutRequest(), HttpStatus.FORBIDDEN);
    }

    @Test
    @PactTestFor(pactMethod = "notFoundRequestErrorFromPutPartiesNotified")
    public void shouldReturn404NotFoundRequestForPutHearing(MockServer mockServer) throws JsonProcessingException {
        executeCall(mockServer, ContractTestDataProvider.authorisedHeaders, NOT_FOUND_CASE_ID,
                  VERSION_NUMBER, ContractTestDataProvider.generatePartiesPutRequest(), HttpStatus.NOT_FOUND);
    }

    private void executeCall(MockServer mockServer, Map<String, String> headers, String caseId,
                             String version, PartiesNotified payload, HttpStatus status) {
        RestAssured.given().headers(headers)
            .contentType(ContentType.JSON)
            .queryParam(FIELD_ID, caseId)
            .queryParam(VERSION, version)
            .body(ContractTestDataProvider.toJsonString(payload))
            .when()
            .put(mockServer.getUrl() + ContractTestDataProvider.PARTIES_NOTIFIED_PATH)
            .then().statusCode(status.value())
            .and().extract()
            .body()
            .jsonPath();
    }

}
