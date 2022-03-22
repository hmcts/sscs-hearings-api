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
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingResponse;
import uk.gov.hmcts.reform.sscs.service.HmcHearingApi;
import uk.gov.hmcts.reform.sscs.utility.BasePactTest;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(PactConsumerTestExt.class)
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.sscs.service"})
@ActiveProfiles("contract")
@SpringBootTest
@PactTestFor(port = "10000")
@PactFolder("pacts")
public class HearingPutConsumerTest extends BasePactTest {

    @Autowired
    private HmcHearingApi hmcHearingApi;

    @BeforeEach
    public void timeGapBetweenEachTest() throws InterruptedException {
        TimeUnit.SECONDS.sleep(2);
    }

    @Pact(consumer = ContractTestDataProvider.CONSUMER_NAME)
    public RequestResponsePact updateHearingRequestForValidRequest(PactDslWithProvider builder) {
        return builder.given(ContractTestDataProvider.CONSUMER_NAME + " successfully updating hearing request ")
            .uponReceiving("Request to update hearing request to save details")
            .path(ContractTestDataProvider.PATH_HEARING)
            .method(HttpMethod.PUT.toString())
            .query(ContractTestDataProvider.FIELD_ID + "=" + ContractTestDataProvider.VALID_CASE_ID)
            .body(ContractTestDataProvider.toJsonString(ContractTestDataProvider.generateHearingRequest()))
            .headers(ContractTestDataProvider.headers).willRespondWith()
            .status(HttpStatus.OK.value())
            .body(generatePostHearingsJsonBody(ContractTestDataProvider.MSG_200_PUT_HEARING))
            .toPact();
    }

    @Pact(consumer = ContractTestDataProvider.CONSUMER_NAME)
    public RequestResponsePact validationErrorFromPutHearing(PactDslWithProvider builder) {
        return builder.given(ContractTestDataProvider.CONSUMER_NAME
                                 + " throws validation error while trying to update hearing")
            .uponReceiving("Request to UPDATE hearing for invalid hearing request")
            .path(ContractTestDataProvider.PATH_HEARING).method(HttpMethod.PUT.toString())
            .body(ContractTestDataProvider.toJsonString(ContractTestDataProvider.generateInvalidHearingRequest()))
            .headers(ContractTestDataProvider.headers)
            .willRespondWith()
             .status(HttpStatus.BAD_REQUEST.value())
            .body(new PactDslJsonBody()
                      .stringType(ContractTestDataProvider.FIELD_MESSAGE, ContractTestDataProvider.MSG_400_PUT_HEARING)
                      .stringValue(ContractTestDataProvider.FIELD_STATUS, ContractTestDataProvider.BAD_REQUEST)
                      .eachLike(ContractTestDataProvider.FIELD_ERRORS, 1)
                      .closeArray())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "updateHearingRequestForValidRequest")
    public void shouldSuccessfullyPutHearingRequest() throws JsonProcessingException {
        HearingResponse hearingResponse = hmcHearingApi.updateHearingRequest(
            ContractTestDataProvider.IDAM_OAUTH2_TOKEN,
            ContractTestDataProvider.SERVICE_AUTHORIZATION_TOKEN,
            ContractTestDataProvider.VALID_CASE_ID,
            ContractTestDataProvider.generateHearingRequest()
        );

        assertTrue(hearingResponse.getHearingRequestId() > ContractTestDataProvider.ZERO_LENGTH);
        assertFalse(hearingResponse.getStatus().isEmpty());
        assertTrue(hearingResponse.getVersionNumber() != ContractTestDataProvider.ZERO_NUMBER_LENGTH);
        assertTrue(hearingResponse.getTimeStamp() != null);
    }

    @Test
    @PactTestFor(pactMethod = "validationErrorFromPutHearing")
    public void shouldReturn400BadRequestForPostHearing(MockServer mockServer) {
        RestAssured.given().headers(ContractTestDataProvider.headers)
            .contentType(io.restassured.http.ContentType.JSON)
            .body(ContractTestDataProvider.toJsonString(ContractTestDataProvider.generateInvalidHearingRequest()))
            .when()
            .put(mockServer.getUrl() + ContractTestDataProvider.PATH_HEARING)
            .then().statusCode(HttpStatus.BAD_REQUEST.value())
            .and().extract()
            .body()
            .jsonPath();
    }
}
