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

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.CONSUMER_NAME;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.PROVIDER_NAME;

@ExtendWith(PactConsumerTestExt.class)
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.sscs.service"})
@ActiveProfiles("contract")
@SpringBootTest
@PactTestFor(port = "10000")
@PactFolder("pacts")
public class HearingPostConsumerTest extends BasePactTest {

    @Autowired
    private HmcHearingApi hmcHearingApi;

    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact createHearingRequestForValidRequest(PactDslWithProvider builder) {
        return builder.given(ContractTestDataProvider.CONSUMER_NAME + " successfully creating hearing request ")
            .uponReceiving("Request to create hearing request to save details")
            .path(ContractTestDataProvider.HEARING_PATH)
            .method(HttpMethod.POST.toString()).body(
                ContractTestDataProvider.toJsonString(ContractTestDataProvider.generateHearingRequest()))
            .headers(ContractTestDataProvider.authorisedHeaders).willRespondWith()
            .status(HttpStatus.OK.value())
            .body(generatePostHearingsJsonBody(ContractTestDataProvider.MSG_200_HEARING))
            .toPact();
    }

    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact validationErrorFromPostHearing(PactDslWithProvider builder) {
        return builder.given(ContractTestDataProvider.CONSUMER_NAME
                                 + " throws validation error while trying to create hearing")
                .uponReceiving("Request to CREATE hearing for invalid hearing request")
                    .path(ContractTestDataProvider.HEARING_PATH).method(HttpMethod.POST.toString())
            .body(ContractTestDataProvider.toJsonString(ContractTestDataProvider.generateInvalidHearingRequest()))
            .headers(ContractTestDataProvider.authorisedHeaders)
            .willRespondWith().status(HttpStatus.BAD_REQUEST.value())
            .body(new PactDslJsonBody()
                      .stringType(ContractTestDataProvider.FIELD_MESSAGE, ContractTestDataProvider.MSG_400_HEARING)
                      .stringValue(ContractTestDataProvider.FIELD_STATUS, ContractTestDataProvider.BAD_REQUEST)
                          .eachLike(ContractTestDataProvider.FIELD_ERRORS, 1)
                      .closeArray())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createHearingRequestForValidRequest")
    public void shouldSuccessfullyPostHearingRequest() throws JsonProcessingException {
        HearingResponse hearingResponse = hmcHearingApi.createHearingRequest(
            ContractTestDataProvider.IDAM_OAUTH2_TOKEN,
            ContractTestDataProvider.SERVICE_AUTHORIZATION_TOKEN,
            ContractTestDataProvider.generateHearingRequest()
        );

        assertNotNull(hearingResponse.getHearingRequestId());
        assertTrue(hearingResponse.getStatus().equalsIgnoreCase(ContractTestDataProvider.HEARING_RESPONSE_STATUS));
        assertNotNull(hearingResponse.getVersionNumber());
        assertNotSame(ContractTestDataProvider.ZERO_NUMBER_LENGTH, hearingResponse.getVersionNumber());
        assertNotNull(hearingResponse.getTimeStamp()
                          .compareTo(LocalDateTime.parse(ContractTestDataProvider.HEARING_DATE)));
    }

    @Test
    @PactTestFor(pactMethod = "validationErrorFromPostHearing")
    public void shouldReturn400BadRequestForPostHearing(MockServer mockServer) {
        RestAssured.given().headers(ContractTestDataProvider.authorisedHeaders)
            .contentType(io.restassured.http.ContentType.JSON)
                .body(ContractTestDataProvider.toJsonString(ContractTestDataProvider.generateInvalidHearingRequest()))
                .when()
                .post(mockServer.getUrl() + ContractTestDataProvider.HEARING_PATH)
                    .then().statusCode(HttpStatus.BAD_REQUEST.value())
                .and().extract()
                .body()
                    .jsonPath();
    }
}
