package uk.gov.hmcts.reform.sscs;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import org.springframework.http.HttpHeaders;

import java.time.LocalDateTime;
import java.util.Map;


public class BasePactTesting {

    protected static final String PROVIDER_NAME = "sscsHearingApiProvider";
    protected static final String CONSUMER_NAME = "sscsHearingApiConsumer";

    protected static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    protected static final String IDAM_OAUTH2_TOKEN = "pact-test-idam-token";
    protected static final String SERVICE_AUTHORIZATION_TOKEN = "pact-test-s2s-token";
    protected static final String CONTENT_TYPE = "Content-Type";
    protected static final String APPLICATION_JSON = "application/json";

    protected static final Map<String, String> headers = Map.of(
        HttpHeaders.AUTHORIZATION, IDAM_OAUTH2_TOKEN,
        SERVICE_AUTHORIZATION, SERVICE_AUTHORIZATION_TOKEN,
        CONTENT_TYPE, APPLICATION_JSON
    );

    protected PactDslJsonBody generateValidHearingGetResponsePactDslJsonBody(LocalDateTime date) {
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
