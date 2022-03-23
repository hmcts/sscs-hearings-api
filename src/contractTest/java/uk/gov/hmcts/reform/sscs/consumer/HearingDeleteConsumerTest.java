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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.sscs.ContractTestDataProvider;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDeleteRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingResponse;
import uk.gov.hmcts.reform.sscs.service.HmcHearingApi;
import uk.gov.hmcts.reform.sscs.utility.BasePactTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

@ExtendWith(PactConsumerTestExt.class)
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.sscs.service"})
@ActiveProfiles("contract")
@SpringBootTest
@PactTestFor(port = "10000")
@PactFolder("pacts")
public class HearingDeleteConsumerTest extends BasePactTest {

    private static final String RESPONSE_STATUS = "CANCELLATION_REQUESTED";

    private static final String ID = "id";
    private static final String VALID_CASE_ID = "123";
    private static final String FORBIDDEN_CASE_ID = "456";
    private static final String NOT_FOUND_CASE_ID = "789";

    @Autowired
    private HmcHearingApi hmcHearingApi;

    @Pact(consumer = ContractTestDataProvider.CONSUMER_NAME)
    public RequestResponsePact deleteHearingRequestForValidRequest(PactDslWithProvider builder) {
        return builder.given(ContractTestDataProvider.CONSUMER_NAME + " successfully deleting a hearing request ")
            .uponReceiving("Request to delete hearing request")
            .path(ContractTestDataProvider.HEARING_PATH)
            .method(HttpMethod.DELETE.toString())
            .query(ID + "=" + VALID_CASE_ID)
            .body(ContractTestDataProvider.toJsonString(ContractTestDataProvider.generateHearingDeleteRequest()))
            .headers(ContractTestDataProvider.authorisedHeaders)
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .body(generateHearingsJsonBody(ContractTestDataProvider.MSG_200_HEARING,RESPONSE_STATUS))
            .toPact();
    }

    @Pact(consumer = ContractTestDataProvider.CONSUMER_NAME)
    public RequestResponsePact badRequestErrorFromDeleteHearing(PactDslWithProvider builder) {
        return builder.given(ContractTestDataProvider.CONSUMER_NAME
                                 + " throws bad request error while trying to delete hearing")
            .uponReceiving("Request to DELETE hearing for bad hearing request")
            .path(ContractTestDataProvider.HEARING_PATH)
            .method(HttpMethod.DELETE.toString())
            .query(ID + "=" + VALID_CASE_ID)
            .body(ContractTestDataProvider.toJsonString(ContractTestDataProvider.generateInvalidHearingDeleteRequest()))
            .headers(ContractTestDataProvider.authorisedHeaders)
            .willRespondWith().status(HttpStatus.BAD_REQUEST.value())
            .body(new PactDslJsonBody()
                      .stringType(ContractTestDataProvider.FIELD_MESSAGE,ContractTestDataProvider.MSG_400_HEARING)
                      .stringValue(ContractTestDataProvider.FIELD_STATUS, HttpStatus.BAD_REQUEST.value()
                          + " " + HttpStatus.BAD_REQUEST.getReasonPhrase())
                      .eachLike(ContractTestDataProvider.FIELD_ERRORS, 1)
                      .closeArray())
            .toPact();
    }

    @Pact(consumer = ContractTestDataProvider.CONSUMER_NAME)
    public RequestResponsePact unauthorisedRequestErrorFromDeleteHearing(PactDslWithProvider builder) {
        return builder.given(ContractTestDataProvider.CONSUMER_NAME
                                 + " throws unauthorised error while trying to delete hearing")
            .uponReceiving("Request to DELETE hearing for unauthorised hearing request")
            .path(ContractTestDataProvider.HEARING_PATH)
            .method(HttpMethod.DELETE.toString())
            .query(ID + "=" + VALID_CASE_ID)
            .body(ContractTestDataProvider.toJsonString(ContractTestDataProvider.generateHearingDeleteRequest()))
            .headers(ContractTestDataProvider.unauthorisedHeaders)
            .willRespondWith().status(HttpStatus.UNAUTHORIZED.value())
            .body(new PactDslJsonBody()
                      .stringType(ContractTestDataProvider.FIELD_MESSAGE, ContractTestDataProvider.MSG_401_HEARING)
                      .stringValue(ContractTestDataProvider.FIELD_STATUS, HttpStatus.UNAUTHORIZED.value()
                          + " " + HttpStatus.UNAUTHORIZED.getReasonPhrase())
                      .eachLike(ContractTestDataProvider.FIELD_ERRORS, 1)
                      .closeArray())
            .toPact();
    }

    @Pact(consumer = ContractTestDataProvider.CONSUMER_NAME)
    public RequestResponsePact forbiddenRequestErrorFromDeleteHearing(PactDslWithProvider builder) {
        return builder.given(ContractTestDataProvider.CONSUMER_NAME
                                 + " throws forbidden error while trying to delete hearing")
            .uponReceiving("Request to DELETE hearing for forbidden hearing request")
            .path(ContractTestDataProvider.HEARING_PATH).method(HttpMethod.DELETE.toString())
            .query(ID + "=" + FORBIDDEN_CASE_ID)
            .body(ContractTestDataProvider.toJsonString(ContractTestDataProvider.generateHearingDeleteRequest()))
            .headers(ContractTestDataProvider.authorisedHeaders)
            .willRespondWith().status(HttpStatus.FORBIDDEN.value())
            .body(new PactDslJsonBody()
                      .stringType(ContractTestDataProvider.FIELD_MESSAGE,ContractTestDataProvider.MSG_403_HEARING)
                      .stringValue(ContractTestDataProvider.FIELD_STATUS, HttpStatus.FORBIDDEN.value()
                          + " " + HttpStatus.FORBIDDEN.getReasonPhrase())
                      .eachLike(ContractTestDataProvider.FIELD_ERRORS, 1)
                      .closeArray())
            .toPact();
    }

    @Pact(consumer = ContractTestDataProvider.CONSUMER_NAME)
    public RequestResponsePact notFoundRequestErrorFromDeleteHearing(PactDslWithProvider builder) {
        return builder.given(ContractTestDataProvider.CONSUMER_NAME
                                 + " throws not found request error while trying to delete hearing")
            .uponReceiving("Request to DELETE hearing for not found hearing request")
            .path(ContractTestDataProvider.HEARING_PATH).method(HttpMethod.DELETE.toString())
            .query(ID + "=" + NOT_FOUND_CASE_ID)
            .body(ContractTestDataProvider.toJsonString(ContractTestDataProvider.generateHearingDeleteRequest()))
            .headers(ContractTestDataProvider.authorisedHeaders)
            .willRespondWith().status(HttpStatus.NOT_FOUND.value())
            .body(new PactDslJsonBody()
                      .stringType(ContractTestDataProvider.FIELD_MESSAGE, ContractTestDataProvider.MSG_404_HEARING)
                      .stringValue(ContractTestDataProvider.FIELD_STATUS, HttpStatus.NOT_FOUND.value()
                          + " " + HttpStatus.NOT_FOUND.getReasonPhrase())
                      .eachLike(ContractTestDataProvider.FIELD_ERRORS, 1)
                      .closeArray())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "deleteHearingRequestForValidRequest")
    public void shouldSuccessfullyDeleteHearingRequest() {
        HearingResponse hearingResponse = hmcHearingApi.deleteHearingRequest(
            ContractTestDataProvider.IDAM_OAUTH2_TOKEN,
            ContractTestDataProvider.SERVICE_AUTHORIZATION_TOKEN,
            VALID_CASE_ID,
            ContractTestDataProvider.generateHearingDeleteRequest()
        );

        assertNotNull(hearingResponse.getHearingRequestId());
        assertFalse(hearingResponse.getStatus().isEmpty());
        assertNotNull(hearingResponse.getVersionNumber());
        assertNotSame(ContractTestDataProvider.ZERO_NUMBER_LENGTH, hearingResponse.getVersionNumber());
        assertNotNull(hearingResponse.getTimeStamp());
    }

    @Test
    @PactTestFor(pactMethod = "badRequestErrorFromDeleteHearing")
    public void shouldReturn400BadRequestForDeleteHearing(MockServer mockServer) {
        executeCall(mockServer, ContractTestDataProvider.authorisedHeaders, VALID_CASE_ID,
                    ContractTestDataProvider.generateInvalidHearingDeleteRequest(), HttpStatus.BAD_REQUEST
        );
    }

    @Test
    @PactTestFor(pactMethod = "unauthorisedRequestErrorFromDeleteHearing")
    public void shouldReturn401UnauthorisedRequestForDeleteHearing(MockServer mockServer) {
        executeCall(mockServer, ContractTestDataProvider.unauthorisedHeaders, VALID_CASE_ID,
                    ContractTestDataProvider.generateHearingDeleteRequest(), HttpStatus.UNAUTHORIZED
        );
    }

    @Test
    @PactTestFor(pactMethod = "forbiddenRequestErrorFromDeleteHearing")
    public void shouldReturn403ForbiddenRequestForDeleteHearing(MockServer mockServer) {
        executeCall(mockServer, ContractTestDataProvider.authorisedHeaders, FORBIDDEN_CASE_ID,
                    ContractTestDataProvider.generateHearingDeleteRequest(), HttpStatus.FORBIDDEN
        );
    }

    @Test
    @PactTestFor(pactMethod = "notFoundRequestErrorFromDeleteHearing")
    public void shouldReturn404NotFoundRequestForDeleteHearing(MockServer mockServer) {
        executeCall(mockServer, ContractTestDataProvider.authorisedHeaders, NOT_FOUND_CASE_ID,
                    ContractTestDataProvider.generateHearingDeleteRequest(), HttpStatus.NOT_FOUND
        );
    }

    private void executeCall(MockServer mockServer, Map<String, String> headers, String caseId,
                             HearingDeleteRequestPayload payload, HttpStatus status) {
        RestAssured.given().headers(headers)
            .contentType(ContentType.JSON)
            .queryParam(ID, caseId)
            .body(ContractTestDataProvider.toJsonString(payload)).when()
            .delete(mockServer.getUrl() + ContractTestDataProvider.HEARING_PATH)
            .then().statusCode(status.value())
            .and().extract()
            .body()
            .jsonPath();
    }

}
