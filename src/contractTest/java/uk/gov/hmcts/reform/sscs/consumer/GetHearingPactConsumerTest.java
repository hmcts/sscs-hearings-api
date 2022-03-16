package uk.gov.hmcts.reform.sscs.consumer;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.FeignException;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.sscs.BasePactTesting;
import uk.gov.hmcts.reform.sscs.model.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.service.HmcHearingApi;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@ExtendWith(PactConsumerTestExt.class)
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.sscs.service"})
@ActiveProfiles("contract")
@PactTestFor(port = "10000")
public class GetHearingPactConsumerTest extends BasePactTesting {

    private static final String PATH_HEARING = "/hearing";
    private static final String FIELD_ID = "id";
    private static final String VALID_CASE_ID = "123";
    private static final String OPTION_FIELD_IS_VALID = "?isValid";
    private static final String VALID_NO_CONTENT_CASE_ID = "0";

    private static final String BAD_REQUEST_CASE_ID = "400";
    private static final String UNAUTHORISED_CASE_ID = "401";
    private static final String FORBIDDEN_CASE_ID = "403";
    private static final String NOT_FOUND_CASE_ID = "404";

    private static final LocalDateTime date = LocalDateTime.now();

    @Autowired
    private HmcHearingApi hmcHearingApi;


    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact getHearing(PactDslWithProvider builder) throws Exception {

        return builder
            .given("sscs hearing api successfully returns case")
            .uponReceiving("Request to GET hearing for given valid case id")
            .path(PATH_HEARING)
            .method(HttpMethod.GET.toString())
            .query(FIELD_ID + "=" + VALID_CASE_ID)
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .body(generateValidHearingGetResponsePactDslJsonBody(date))
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getHearing")
    public void shouldSuccessfullyGetHearing() throws Exception {

        HearingGetResponse result = hmcHearingApi.getHearingRequest(
            IDAM_OAUTH2_TOKEN,
            SERVICE_AUTHORIZATION_TOKEN,
            VALID_CASE_ID
        );

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        HearingGetResponse expected = objectMapper.readValue(
            generateValidHearingGetResponsePactDslJsonBody(date).toString(),
            HearingGetResponse.class
        );

        Assertions.assertEquals(expected, result);

        TimeUnit.SECONDS.sleep(2);
    }


    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact getHearingWithRefCheck(PactDslWithProvider builder) throws Exception {

        return builder
            .given("sscs hearing api successfully returns case and with ref check")
            .uponReceiving("Request to GET hearing for given valid case id and with ?isValid")
            .path(PATH_HEARING)
            .method(HttpMethod.GET.toString())
            .query(FIELD_ID + "=" + VALID_CASE_ID + OPTION_FIELD_IS_VALID)
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .body(generateValidHearingGetResponsePactDslJsonBody(date))
            .toPact();
    }


    @Test
    @PactTestFor(pactMethod = "getHearingWithRefCheck")
    public void shouldSuccessfullyGetHearingWithRefCheck() throws Exception {

        HearingGetResponse result = hmcHearingApi.getHearingRequest(
            IDAM_OAUTH2_TOKEN,
            SERVICE_AUTHORIZATION_TOKEN,
            VALID_CASE_ID + OPTION_FIELD_IS_VALID
        );

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        HearingGetResponse expected = objectMapper.readValue(
            generateValidHearingGetResponsePactDslJsonBody(date).toString(),
            HearingGetResponse.class
        );

        Assertions.assertEquals(expected, result);

        TimeUnit.SECONDS.sleep(2);
    }

    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact getHearingWithNoContent(PactDslWithProvider builder) throws Exception {

        return builder
            .given("sscs hearing api successfully returns no case")
            .uponReceiving("Request to GET hearing and get nothing")
            .path(PATH_HEARING)
            .method(HttpMethod.GET.toString())
            .query(FIELD_ID + "=" + VALID_NO_CONTENT_CASE_ID)
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.NO_CONTENT.value())
            .body("")
            .toPact();
    }


    @Test
    @PactTestFor(pactMethod = "getHearingWithNoContent")
    public void shouldSuccessfullyGetHearingWithNoContent() throws Exception {

        HearingGetResponse result = hmcHearingApi.getHearingRequest(
            IDAM_OAUTH2_TOKEN,
            SERVICE_AUTHORIZATION_TOKEN,
            VALID_NO_CONTENT_CASE_ID
        );

        Assert.assertNull(result);

        TimeUnit.SECONDS.sleep(2);
    }


    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact getHearingWithBadRequest(PactDslWithProvider builder) throws Exception {

        return builder
            .given("sscs hearing api fail return with bad request status")
            .uponReceiving("Request to GET hearing for with bad request case id")
            .path(PATH_HEARING)
            .method(HttpMethod.GET.toString())
            .query(FIELD_ID + "=" + BAD_REQUEST_CASE_ID)
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.BAD_REQUEST.value())
            .body("")
            .toPact();
    }


    @Test
    @PactTestFor(pactMethod = "getHearingWithBadRequest")
    public void shouldFailGetHearingWithBadRequest() throws Exception {

        FeignException thrown = Assertions.assertThrows(FeignException.class, () -> {
            HearingGetResponse result = hmcHearingApi.getHearingRequest(
                IDAM_OAUTH2_TOKEN,
                SERVICE_AUTHORIZATION_TOKEN,
                BAD_REQUEST_CASE_ID
            );
        }, "FeignException was expected");

        Assertions.assertEquals(thrown.status(), HttpStatus.BAD_REQUEST.value());


        TimeUnit.SECONDS.sleep(2);
    }


    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact getHearingWithUnauthorized(PactDslWithProvider builder) throws Exception {

        return builder
            .given("sscs hearing api successfully returns case")
            .uponReceiving("Request to GET hearing for with unauthorized case id")
            .path(PATH_HEARING)
            .method(HttpMethod.GET.toString())
            .query(FIELD_ID + "=" + UNAUTHORISED_CASE_ID)
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.UNAUTHORIZED.value())
            .body("")
            .toPact();
    }


    @Test
    @PactTestFor(pactMethod = "getHearingWithUnauthorized")
    public void shouldFailGetHearingWithUnauthorized() throws Exception {

        FeignException thrown = Assertions.assertThrows(FeignException.class, () -> {
            HearingGetResponse result = hmcHearingApi.getHearingRequest(
                IDAM_OAUTH2_TOKEN,
                SERVICE_AUTHORIZATION_TOKEN,
                UNAUTHORISED_CASE_ID
            );
        }, "FeignException was expected");

        Assertions.assertEquals(thrown.status(), HttpStatus.UNAUTHORIZED.value());

        TimeUnit.SECONDS.sleep(2);
    }

    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact getHearingWithForbidden(PactDslWithProvider builder) throws Exception {

        return builder
            .given("sscs hearing api successfully returns case")
            .uponReceiving("Request to GET hearing for with forbidden case id")
            .path(PATH_HEARING)
            .method(HttpMethod.GET.toString())
            .query(FIELD_ID + "=" + FORBIDDEN_CASE_ID)
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.FORBIDDEN.value())
            .body("")
            .toPact();
    }


    @Test
    @PactTestFor(pactMethod = "getHearingWithForbidden")
    public void shouldFailGetHearingWithForbidden() throws Exception {

        FeignException thrown = Assertions.assertThrows(FeignException.class, () -> {
            HearingGetResponse result = hmcHearingApi.getHearingRequest(
                IDAM_OAUTH2_TOKEN,
                SERVICE_AUTHORIZATION_TOKEN,
                FORBIDDEN_CASE_ID
            );
        }, "FeignException was expected");

        Assertions.assertEquals(thrown.status(), HttpStatus.FORBIDDEN.value());

        TimeUnit.SECONDS.sleep(2);
    }


    @Pact(provider = PROVIDER_NAME, consumer = CONSUMER_NAME)
    public RequestResponsePact getHearingWithNotFound(PactDslWithProvider builder) throws Exception {

        return builder
            .given("sscs hearing api successfully returns case")
            .uponReceiving("Request to GET hearing for with not found case id")
            .path(PATH_HEARING)
            .method(HttpMethod.GET.toString())
            .query(FIELD_ID + "=" + NOT_FOUND_CASE_ID)
            .headers(headers)
            .willRespondWith()
            .status(HttpStatus.NOT_FOUND.value())
            .body("")
            .toPact();
    }


    @Test
    @PactTestFor(pactMethod = "getHearingWithNotFound")
    public void shouldFailGetHearingWithNotFound() throws Exception {

        FeignException thrown = Assertions.assertThrows(FeignException.class, () -> {
            HearingGetResponse result = hmcHearingApi.getHearingRequest(
                IDAM_OAUTH2_TOKEN,
                SERVICE_AUTHORIZATION_TOKEN,
                NOT_FOUND_CASE_ID
            );
        }, "FeignException was expected");

        Assertions.assertEquals(thrown.status(), HttpStatus.NOT_FOUND.value());

        TimeUnit.SECONDS.sleep(2);
    }


    private PactDslJsonBody generateValidHearingGetResponsePactDslJsonBody(LocalDateTime date) {
        PactDslJsonBody result = new PactDslJsonBody();

        result
            .object("requestDetails")
            .stringType("requestTimeStamp", date.toString())
            .integerType("versionNumber", 123)
            .stringType("hearingRequestID", "hearingRequestID123")
            .stringType("status", "status123")
            .stringType("timeStamp", date.toString())
            .stringType("hearingGroupRequestId", "hearingGroupRequestId123")
            .stringType("partiesNotified", date.toString())
            .closeObject()
            .object("hearingDetails")
            .booleanValue("autolistFlag", true)
            .booleanValue("hearingIsLinkedFlag", true)
            .booleanValue("privateHearingRequiredFlag", true)
            .booleanValue("hearingInWelshFlag", true)
            .stringType("hearingType", "hearingType123")
            .stringType("leadJudgeContractType", "leadJudgeContractType123")
            .stringType("listingComments", "listingComments123")
            .stringType("hearingRequester", "hearingRequester123")
            .stringType("hearingPriorityType", "hearingPriorityType123")
            .integerType("numberOfPhysicalAttendees", 123)
            .integerType("duration", 123)
            .array("nonStandardHearingDurationReasons")
            .string("nonStandardHearingDurationReasons1")
            .string("nonStandardHearingDurationReasons2")
            .closeArray()
            .object("hearingWindow")
            .stringType("dateRangeStart", "dateRangeStart123")
            .stringType("dateRangeEnd", "dateRangeEnd123")
            .stringType("firstDateTimeMustBe", "firstDateTimeMustBe123")
            .closeObject()
            .object("panelRequirements")
            .array("roleType")
            .string("roleType1")
            .string("roleType2")
            .closeArray()
            .array("authorisationSubType")
            .string("authorisationSubType1")
            .string("authorisationSubType2")
            .closeArray()
            .array("panelSpecialisms")
            .string("panelSpecialisms1")
            .string("panelSpecialisms2")
            .closeArray()
            .minArrayLike("panelPreferences", 0, 1)
            .stringType("memberID", "memberID123")
            .stringType("memberType", "memberType123")
            .stringType("requirementType", "requirementType123")
            .closeObject().closeArray()
            .closeObject()
            .minArrayLike("hearingLocations", 0, 1)
            .stringType("locationType", "locationType123")
            .stringType("locationId", "locationId123")
            .closeObject().closeArray()
            .array("facilitiesRequired")
            .string("facilitiesRequired1")
            .closeArray()
            .closeObject()
            .object("caseDetails")
            .booleanValue("caseAdditionalSecurityFlag", true)
            .booleanValue("caseInterpreterRequiredFlag", true)
            .booleanValue("caserestrictedFlag", true)
            .stringType("hmctsServiceCode", "1234")
            .stringType("caseRef", "1234123412341234")
            .stringType("externalCaseReference", "externalCaseReference123")
            .stringType("caseDeepLink", "caseDeepLink123")
            .stringType("hmctsInternalCaseName", "hmctsInternalCaseName123")
            .stringType("publicCaseName", "publicCaseName123")
            .stringType("caseManagementLocationCode", "caseManagementLocationCode123")
            .stringType("requestTimeStamp", date.toString())
            .stringType("caseSLAStartDate", date.toString())
            .minArrayLike("caseCategories", 0, 1)
            .stringType("categoryType", "categoryType123")
            .stringType("categoryValue", "categoryValue123")
            .closeObject().closeArray()
            .closeObject()
            .minArrayLike("partyDetails", 0, 1)
            .stringType("partyID", "partyID123")
            .stringType("partyType", "partyType123")
            .stringType("partyRole", "partyRole")
            .object("individualDetails")
            .stringType("title", "title123")
            .stringType("firstName", "firstName123")
            .stringType("lastName", "lastName123")
            .stringType("preferredHearingChannel", "preferredHearingChannel123")
            .stringType("interpreterLanguage", "interpreterLanguage123")
            .stringType("vulnerabilityDetails", "vulnerabilityDetails123")
            .stringType("hearingChannelEmail", "hearingChannelEmail123")
            .stringType("hearingChannelPhone", "07345960795")
            .booleanValue("vulnerableFlag", true)
            .array("reasonableAdjustments")
            .string("reasonableAdjustments1")
            .closeArray()
            .minArrayLike("relatedParties", 0, 1)
            .stringType("relatedPartyID", "relatedPartyID123")
            .stringType("relationshipType", "relationshipType123")
            .closeObject().closeArray()
            .closeObject()
            .object("organisationDetails")
            .stringType("name", "name123")
            .stringType("organisationType", "organisationType123")
            .stringType("cftOrganisationID", "cftOrganisationID123")
            .closeObject()
            .minArrayLike("unavailabilityDOW", 0, 1)
            .stringType("DOW", "MONDAY")
            .stringType("DOWUnavailabilityType", "dowUnavailabilityType123")
            .closeObject().closeArray()
            .minArrayLike("unavailabilityRanges", 0, 1)
            .stringType("unavailableFromDate", "unavailableFromDate123")
            .stringType("unavailableToDate", "unavailableToDate123")
            .closeObject().closeArray()
            .closeObject().closeArray()
            .object("hearingResponse")
            .integerType("hearingRequestID", 123)
            .stringType("status", "ACTIVE")
            .stringType("timeStamp", date.toString())
            .stringType("versionNumber", "versionNumber100")
            .stringType("listAssistTransactionID", "ListAssistTransactionID123123")
            .stringType("receivedDateTime", date.toString())
            .integerType("responseVersion", 321)
            .stringType("laCaseStatus", "ACTIVE")
            .stringType("listingStatus", "ACTIVE")
            .stringType("hearingCancellationReason", "hearingCancellationReason_NO_RESULT")
            .minArrayLike("hearingDaySchedule", 0, 1)
            .stringType("listAssistSessionID", "listAssistSessionID123")
            .stringType("hearingVenueId", "hearingVenueId123")
            .stringType("hearingRoomId", "hearingRoomId123")
            .stringType("hearingJudgeId", "hearingJudgeId123")
            .stringType("panelMemberId", "panelMemberId123")
            .minArrayLike("attendees", 0, 1)
            .stringType("partyID", "partyID123")
            .stringType("hearingSubChannel", "hearingSubChannel123")
            .closeObject().closeArray()
            .closeObject().closeArray()
            .closeObject();


        return result;
    }

}
