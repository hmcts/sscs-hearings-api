package uk.gov.hmcts.reform.sscs.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.commons.httpclient.HttpClientConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.sscs.BasePactTesting;
import uk.gov.hmcts.reform.sscs.model.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.service.HmcHearingApi;

@ExtendWith(SpringExtension.class)
@ExtendWith(PactConsumerTestExt.class)
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.sscs.service"})
@ActiveProfiles("contract")
@SpringBootTest
@PactTestFor(port = "10000")
@PactFolder("build/pacts")
public class GetHearingPactConsumerTest extends BasePactTesting {

    private static final String PATH_HEARING = "/hearing";
    private static final String FIELD_ID = "id";
    private static final String VALID_CASE_ID = "123";
    private static final String OPTION_FIELD_IS_VALID = "?isValid";

    @Autowired
    private HmcHearingApi hmcHearingApi;

    /**
     * get Hearings For Valid Case Ref only.
     *
     * @param builder Builder object
     * @return response Response object
     */
    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact getHearing(PactDslWithProvider builder) {
        return builder
            .given("sscs haaring api successfully returns case")
            .uponReceiving("Request to GET hearing for given valid case ref only")
            .path(PATH_HEARING)
            .method(HttpMethod.GET.toString())
            .query(FIELD_ID + "=" + VALID_CASE_ID)
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .body("{\"partyID\":\"12\", \"hearingSubChannel\":\"123123\"}")
            .toPact();
    }



    @Test
    @PactTestFor(pactMethod = "getHearing")
    public void shouldSuccessfullyGetHearing(MockServer mockServer) {
        HearingGetResponse result = hmcHearingApi.getHearingRequest(
            IDAM_OAUTH2_TOKEN,
            SERVICE_AUTHORIZATION_TOKEN,
            VALID_CASE_ID
        );
    }



}
