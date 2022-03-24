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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.sscs.ContractTestDataProvider;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HmcHearingResponse;
import uk.gov.hmcts.reform.sscs.service.HmcHearingApi;
import uk.gov.hmcts.reform.sscs.utility.BasePactTest;

@ExtendWith(PactConsumerTestExt.class)
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.sscs.service"})
@ActiveProfiles("contract")
@SpringBootTest
@PactTestFor(port = "10000")
@PactFolder("pacts")
public class HearingPostConsumerTest extends ContractTestDataProvider {

    private static final String RESPONSE_STATUS = "HEARING_REQUESTED";

    @Autowired
    private HmcHearingApi hmcHearingApi;

    @Pact(consumer = CONSUMER_NAME)
    public RequestResponsePact createHearingRequestForValidRequest(PactDslWithProvider builder) {
        return builder.given(CONSUMER_NAME + " successfully creating hearing request ").uponReceiving(
            "Request to create hearing request to save details")
            .path(HEARING_PATH).method(HttpMethod.POST.toString()).body(
                toJsonString(generateHearingRequest())).headers(authorisedHeaders).willRespondWith()
            .status(HttpStatus.OK.value()).body(
                BasePactTest
                .generateHearingsJsonBody(MSG_200_HEARING, RESPONSE_STATUS))
            .toPact();
    }

    @Pact(consumer = CONSUMER_NAME)
    public RequestResponsePact validationErrorFromPostHearing(PactDslWithProvider builder) {
        return builder.given(CONSUMER_NAME
                                 + " throws validation error while trying to create hearing")
                .uponReceiving("Request to CREATE hearing for invalid hearing request")
                    .path(HEARING_PATH).method(HttpMethod.POST.toString())
            .body(toJsonString(generateInvalidHearingRequest())).headers(authorisedHeaders)
            .willRespondWith().status(HttpStatus.BAD_REQUEST.value())
            .body(new PactDslJsonBody().stringType(FIELD_MESSAGE, MSG_400_HEARING)
                      .stringValue(FIELD_STATUS, BAD_REQUEST)
                          .eachLike(FIELD_ERRORS, 1)
                      .closeArray())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createHearingRequestForValidRequest")
    public void shouldSuccessfullyPostHearingRequest() throws JsonProcessingException {
        HmcHearingResponse hmcHearingResponse = hmcHearingApi.createHearingRequest(
            IDAM_OAUTH2_TOKEN,
            SERVICE_AUTHORIZATION_TOKEN,
            generateHearingRequest()
        );

        Assertions.assertTrue(hmcHearingResponse.getHearingRequestId() > ZERO_LENGTH);
        Assertions.assertFalse(hmcHearingResponse.getStatus().isEmpty());
        Assertions.assertTrue(hmcHearingResponse.getVersionNumber() != ZERO_NUMBER_LENGTH);
        Assertions.assertTrue(hmcHearingResponse.getTimeStamp() != null);
    }

    @Test
    @PactTestFor(pactMethod = "validationErrorFromPostHearing")
    public void shouldReturn400BadRequestForPostHearing(MockServer mockServer) {
        RestAssured.given().headers(authorisedHeaders)
            .contentType(io.restassured.http.ContentType.JSON)
                .body(toJsonString(generateInvalidHearingRequest())).when()
                .post(mockServer.getUrl() + HEARING_PATH)
                    .then().statusCode(HttpStatus.BAD_REQUEST.value())
                .and().extract()
                .body()
                    .jsonPath();
    }
}
