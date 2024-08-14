package uk.gov.hmcts.reform.sscs.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactBuilder;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactDirectory;
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
import uk.gov.hmcts.reform.sscs.model.single.hearing.HmcUpdateResponse;
import uk.gov.hmcts.reform.sscs.service.HmcHearingApi;
import uk.gov.hmcts.reform.sscs.utility.BasePactTest;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.CONSUMER_NAME;
import static uk.gov.hmcts.reform.sscs.ContractTestDataProvider.PROVIDER_NAME;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.HEARING_REQUESTED;

@ExtendWith(PactConsumerTestExt.class)
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.sscs.service"})
@ActiveProfiles("contract")
@SpringBootTest
@PactTestFor(port = "10000")
@PactDirectory("pacts")
class HearingPostConsumerTest extends BasePactTest {

    @Autowired
    private HmcHearingApi hmcHearingApi;

    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    V4Pact createHearingRequestForValidRequest(PactBuilder builder) {
        return builder
            .usingLegacyDsl()
            .given(CONSUMER_NAME + " successfully creating hearing request ")
            .uponReceiving("Request to create hearing request to save details")
            .path(ContractTestDataProvider.HEARING_PATH)
            .method(HttpMethod.POST.toString()).body(
                ContractTestDataProvider.toJsonString(ContractTestDataProvider.generateHearingRequest()))
            .headers(ContractTestDataProvider.authorisedHeaders).willRespondWith()
            .status(HttpStatus.OK.value())
            .body(generateHearingsJsonBody(ContractTestDataProvider.MSG_200_HEARING, HEARING_REQUESTED))
            .toPact(V4Pact.class);
    }

    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    V4Pact validationErrorFromPostHearing(PactBuilder builder) {
        return builder.usingLegacyDsl().given(CONSUMER_NAME
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
            .toPact(V4Pact.class);
    }

    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public V4Pact unauthorisedRequestErrorFromPostHearing(PactBuilder builder) {
        return builder.usingLegacyDsl().given(CONSUMER_NAME
                                 + " throws unauthorised error while trying to update hearing")
            .uponReceiving("Request to UPDATE hearing for unauthorised hearing request")
            .path(ContractTestDataProvider.HEARING_PATH).method(HttpMethod.POST.toString())
            .body(ContractTestDataProvider.toJsonString(ContractTestDataProvider.generateHearingRequest()))
            .headers(ContractTestDataProvider.unauthorisedHeaders)
            .willRespondWith().status(HttpStatus.UNAUTHORIZED.value())
            .body(new PactDslJsonBody()
                      .stringType(ContractTestDataProvider.FIELD_MESSAGE,ContractTestDataProvider.MSG_401_HEARING)
                      .stringValue(ContractTestDataProvider.FIELD_STATUS, HttpStatus.UNAUTHORIZED.value()
                          + " " + HttpStatus.UNAUTHORIZED.getReasonPhrase())
                      .eachLike(ContractTestDataProvider.FIELD_ERRORS, 1)
                      .closeArray())
            .toPact(V4Pact.class);
    }

    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public V4Pact forbiddenRequestErrorFromPostHearing(PactBuilder builder) {
        return builder.usingLegacyDsl().given(CONSUMER_NAME
                                 + " throws forbidden error while trying to updating hearing")
            .uponReceiving("Request to UPDATE hearing for forbidden hearing request")
            .path(ContractTestDataProvider.HEARING_PATH).method(HttpMethod.POST.toString())
            .body(ContractTestDataProvider.toJsonString(ContractTestDataProvider.generateHearingRequest()))
            .headers(ContractTestDataProvider.authorisedHeaders)
            .willRespondWith().status(HttpStatus.FORBIDDEN.value())
            .body(new PactDslJsonBody()
                      .stringType(ContractTestDataProvider.FIELD_MESSAGE,ContractTestDataProvider.MSG_403_HEARING)
                      .stringValue(ContractTestDataProvider.FIELD_STATUS, HttpStatus.FORBIDDEN.value()
                          + " " + HttpStatus.FORBIDDEN.getReasonPhrase())
                      .eachLike(ContractTestDataProvider.FIELD_ERRORS, 1)
                      .closeArray())
            .toPact(V4Pact.class);
    }

    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public V4Pact notFoundRequestErrorFromPostHearing(PactBuilder builder) {
        return builder.usingLegacyDsl().given(CONSUMER_NAME
                                 + " throws not found request error while trying to update hearing")
            .uponReceiving("Request to UPDATE hearing for not found hearing request")
            .path(ContractTestDataProvider.HEARING_PATH).method(HttpMethod.POST.toString())
            .body(ContractTestDataProvider.toJsonString(ContractTestDataProvider.generateHearingRequest()))
            .headers(ContractTestDataProvider.authorisedHeaders)
            .willRespondWith().status(HttpStatus.NOT_FOUND.value())
            .body(new PactDslJsonBody()
                      .stringType(ContractTestDataProvider.FIELD_MESSAGE,ContractTestDataProvider.MSG_404_HEARING)
                      .stringValue(ContractTestDataProvider.FIELD_STATUS, HttpStatus.NOT_FOUND.value()
                          + " " + HttpStatus.NOT_FOUND.getReasonPhrase())
                      .eachLike(ContractTestDataProvider.FIELD_ERRORS, 1)
                      .closeArray())
            .toPact(V4Pact.class);
    }


    @Test
    @PactTestFor(pactMethod = "createHearingRequestForValidRequest")
    void shouldSuccessfullyPostHearingRequest() {
        HmcUpdateResponse hmcUpdateResponse = hmcHearingApi.createHearingRequest(
            ContractTestDataProvider.IDAM_OAUTH2_TOKEN,
            ContractTestDataProvider.SERVICE_AUTHORIZATION_TOKEN,
            null,
            ContractTestDataProvider.generateHearingRequest()
        );

        assertNotNull(hmcUpdateResponse.getHearingRequestId());
        assertThat(hmcUpdateResponse.getStatus()).isEqualTo(HEARING_REQUESTED);
        assertNotNull(hmcUpdateResponse.getVersionNumber());
        assertNotSame(ContractTestDataProvider.ZERO_NUMBER_LENGTH, hmcUpdateResponse.getVersionNumber());
        assertThat(hmcUpdateResponse.getTimeStamp()).isEqualToIgnoringSeconds(LocalDateTime.parse(ContractTestDataProvider.HEARING_DATE));
    }


    @Test
    @PactTestFor(pactMethod = "validationErrorFromPostHearing")
    void shouldReturn400BadRequestForPostHearing(MockServer mockServer) {

        executeCall(mockServer, ContractTestDataProvider.authorisedHeaders,
                    ContractTestDataProvider.toJsonString(ContractTestDataProvider.generateInvalidHearingRequest()),
                HttpStatus.BAD_REQUEST.value()
        );
    }

    @Test
    @PactTestFor(pactMethod = "unauthorisedRequestErrorFromPostHearing")
    void shouldReturn401UnauthorisedRequestForPostHearing(MockServer mockServer) {
        executeCall(mockServer, ContractTestDataProvider.unauthorisedHeaders,
                    ContractTestDataProvider.toJsonString(ContractTestDataProvider.generateHearingRequest()),
                HttpStatus.UNAUTHORIZED.value()
        );
    }

    @Test
    @PactTestFor(pactMethod = "forbiddenRequestErrorFromPostHearing")
    void shouldReturn403ForbiddenRequestForPostHearing(MockServer mockServer) {
        executeCall(mockServer, ContractTestDataProvider.authorisedHeaders,
                    ContractTestDataProvider.toJsonString(ContractTestDataProvider.generateHearingRequest()),
                HttpStatus.FORBIDDEN.value()
        );
    }

    @Test
    @PactTestFor(pactMethod = "notFoundRequestErrorFromPostHearing")
    void shouldReturn404NotFoundRequestForPostHearing(MockServer mockServer) {

        executeCall(mockServer, ContractTestDataProvider.authorisedHeaders,
                    ContractTestDataProvider.toJsonString(ContractTestDataProvider.generateHearingRequest()),
                HttpStatus.NOT_FOUND.value()
        );

    }

    private void executeCall(MockServer mockServer, Map<String, String> headers,
                             String hearingRequest,
                             int httpStatus) {
        RestAssured.given().headers(headers)
            .contentType(io.restassured.http.ContentType.JSON)
            .body(hearingRequest)
            .when()
            .post(mockServer.getUrl() + ContractTestDataProvider.HEARING_PATH)
            .then().statusCode(httpStatus)
            .and().extract()
            .body();
    }
}
