package uk.gov.hmcts.reform.sscs;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.sscs.model.single.hearing.CaseCategory;
import uk.gov.hmcts.reform.sscs.model.single.hearing.CaseDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingCancelRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingLocations;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingWindow;
import uk.gov.hmcts.reform.sscs.model.single.hearing.IndividualDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.OrganisationDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PanelPreference;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PanelRequirements;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PartyDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RelatedParty;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RequestDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.UnavailabilityDayOfWeek;
import uk.gov.hmcts.reform.sscs.model.single.hearing.UnavailabilityRange;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ContractTestDataProvider {

    public static final String CONSUMER_NAME = "sscs_hearingsApi";
    public static final String PROVIDER_NAME = "hmcHearingServiceProvider";

    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    public static final String IDAM_OAUTH2_TOKEN = "pact-test-idam-token";
    public static final String UNAUTHORISED_IDAM_OAUTH2_TOKEN = "unauthorised-pact-test-idam-token";
    public static final String SERVICE_AUTHORIZATION_TOKEN = "pact-test-s2s-token";
    public static final String UNAUTHORISED_SERVICE_AUTHORIZATION_TOKEN = "unauthorised-pact-test-s2s-token";

    public static final String MSG_200_HEARING = "Success (with content)";
    public static final String MSG_400_HEARING = "Invalid request";
    public static final String MSG_401_HEARING = "Unauthorised request";
    public static final String MSG_403_HEARING = "Forbidden request";
    public static final String MSG_404_HEARING = "Not Found request";



    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";

    public static final String HEARING_PATH = "/hearing";
    public static final String FIELD_STATUS = "status";
    public static final String BAD_REQUEST = "BAD_REQUEST";
    public static final String FIELD_MESSAGE = "message";
    public static final String FIELD_ERRORS = "errors";
    public static final int ZERO_LENGTH = 0;
    public static final Number ZERO_NUMBER_LENGTH = 0;
    public static final String FIELD_ID = "id";
    public static final String VALID_CASE_ID = "123";
    public static final String FORBIDDEN_CASE_ID = "456";
    public static final String NOT_FOUND_CASE_ID = "789";

    public static final String HEARING_RESPONSE_STATUS = "HEARING_REQUESTED";
    public static final String HEARING_DATE = "2030-08-20T12:40";
    public static final String ACTIVE = "ACTIVE";

    private ContractTestDataProvider() {

    }

    public static final Map<String, String> authorisedHeaders = Map.of(
        HttpHeaders.AUTHORIZATION, IDAM_OAUTH2_TOKEN,
        SERVICE_AUTHORIZATION, SERVICE_AUTHORIZATION_TOKEN,
        CONTENT_TYPE, APPLICATION_JSON
    );

    public static final Map<String, String> unauthorisedHeaders = Map.of(
        HttpHeaders.AUTHORIZATION, UNAUTHORISED_IDAM_OAUTH2_TOKEN,
        SERVICE_AUTHORIZATION, UNAUTHORISED_SERVICE_AUTHORIZATION_TOKEN,
        CONTENT_TYPE, APPLICATION_JSON
    );

    public static HearingRequestPayload generateHearingRequest() {
        HearingRequestPayload request = new HearingRequestPayload();

        request.setHearingDetails(hearingDetails());
        request.setCaseDetails(caseDetails());
        request.setPartiesDetails(partyDetails1());

        return request;
    }

    public static HearingRequestPayload generateInvalidHearingRequest() {
        HearingRequestPayload request = new HearingRequestPayload();
        request.setHearingDetails(hearingDetails());
        request.setPartiesDetails(partyDetails1());
        return request;
    }

    public static HearingCancelRequestPayload generateHearingDeleteRequest() {
        HearingCancelRequestPayload request = new HearingCancelRequestPayload();
        request.setCancellationReasonCode("Cancel reason");
        return request;
    }

    public static HearingCancelRequestPayload generateInvalidHearingDeleteRequest() {
        HearingCancelRequestPayload request = new HearingCancelRequestPayload();
        request.setCancellationReasonCode("");
        return request;
    }

    public static String toJsonString(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String jsonString = "";
        try {
            jsonString = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonString;
    }

    protected static RequestDetails requestDetails() {
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setVersionNumber(123L);
        return requestDetails;
    }

    protected static HearingDetails hearingDetails() {
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutolistFlag(true);
        hearingDetails.setHearingType("Some hearing type");
        hearingDetails.setHearingWindow(hearingWindow());
        hearingDetails.setDuration(1);
        hearingDetails.setNonStandardHearingDurationReasons(Arrays.asList("First reason", "Second reason"));
        hearingDetails.setHearingPriorityType("Priority type");
        HearingLocations location1 = new HearingLocations();
        location1.setLocationId("court");
        location1.setLocationType("Location type");
        List<HearingLocations> hearingLocations = new ArrayList<>();
        hearingLocations.add(location1);
        hearingDetails.setHearingLocations(hearingLocations);
        hearingDetails.setPanelRequirements(panelRequirements1());
        hearingDetails.setAmendReasonCode("amend Reason Code ");
        return hearingDetails;
    }

    protected static HearingWindow hearingWindow() {
        HearingWindow hearingWindow = new HearingWindow();
        hearingWindow.setDateRangeStart(LocalDate.parse("2020-02-01"));
        hearingWindow.setDateRangeEnd(LocalDate.parse("2020-02-12"));

        return hearingWindow;
    }

    protected static CaseDetails caseDetails() {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setHmctsServiceCode("ABBA1");
        caseDetails.setCaseId("12");
        caseDetails.setCaseDeepLink("https://www.google.com");
        caseDetails.setHmctsInternalCaseName("Internal case name");
        caseDetails.setPublicCaseName("Public case name");
        caseDetails.setCaseManagementLocationCode("CMLC123");
        caseDetails.setCaseRestrictedFlag(false);
        caseDetails.setCaseSlaStartDate("2030-08-20");
        CaseCategory category = new CaseCategory();
        category.setCategoryType("caseType");
        category.setCategoryValue("PROBATE");
        category.setCategoryParent("categoryParent");
        List<CaseCategory> caseCategories = new ArrayList<>();
        caseCategories.add(category);
        caseDetails.setCaseCategories(caseCategories);
        return caseDetails;
    }

    protected static PanelRequirements panelRequirements1() {
        List<String> roleType = new ArrayList<>();
        roleType.add("role 1");
        roleType.add("role 2");
        List<String> authorisationTypes = new ArrayList<>();
        authorisationTypes.add("authorisation type 1");
        authorisationTypes.add("authorisation type 2");
        authorisationTypes.add("authorisation type 3");
        List<String> authorisationSubType = new ArrayList<>();
        authorisationSubType.add("authorisation sub 1");
        authorisationSubType.add("authorisation sub 2");
        authorisationSubType.add("authorisation sub 3");
        authorisationSubType.add("authorisation sub 4");

        final PanelPreference panelPreference1 = new PanelPreference();
        panelPreference1.setMemberID("Member 1");
        panelPreference1.setMemberType("Member Type 1");
        panelPreference1.setRequirementType("MUSTINC");
        final PanelPreference panelPreference2 = new PanelPreference();
        panelPreference2.setMemberID("Member 2");
        panelPreference2.setMemberType("Member Type 2");
        panelPreference2.setRequirementType("OPTINC");
        final PanelPreference panelPreference3 = new PanelPreference();
        panelPreference3.setMemberID("Member 3");
        panelPreference3.setMemberType("Member Type 3");
        panelPreference3.setRequirementType("EXCLUDE");
        List<PanelPreference> panelPreferences = new ArrayList<>();
        panelPreferences.add(panelPreference1);
        panelPreferences.add(panelPreference2);
        panelPreferences.add(panelPreference3);
        List<String> panelSpecialisms = new ArrayList<>();
        panelSpecialisms.add("Specialism 1");
        panelSpecialisms.add("Specialism 2");
        panelSpecialisms.add("Specialism 3");
        panelSpecialisms.add("Specialism 4");
        panelSpecialisms.add("Specialism 5");

        PanelRequirements panelRequirements = new PanelRequirements();
        panelRequirements.setRoleTypes(roleType);
        panelRequirements.setAuthorisationSubTypes(authorisationSubType);
        panelRequirements.setPanelPreferences(panelPreferences);
        panelRequirements.setPanelSpecialisms(panelSpecialisms);
        panelRequirements.setAuthorisationTypes(authorisationTypes);

        return panelRequirements;
    }

    protected static List<PartyDetails> partyDetails1() {
        ArrayList<PartyDetails> partyDetailsArrayList = new ArrayList<>();
        partyDetailsArrayList.add(createPartyDetails("P1", "IND", "DEF", null, createOrganisationDetails()));
        partyDetailsArrayList.add(createPartyDetails("P2", "IND", "DEF2", createIndividualDetails(), null));
        partyDetailsArrayList.add(createPartyDetails("P3", "IND", "DEF3", createIndividualDetails(),
                                                     createOrganisationDetails()
        ));
        return partyDetailsArrayList;
    }

    private static OrganisationDetails createOrganisationDetails() {
        OrganisationDetails organisationDetails = new OrganisationDetails();
        organisationDetails.setName("name");
        organisationDetails.setOrganisationType("organisationType");
        organisationDetails.setCftOrganisationID("cftOrganisationId01001");
        return organisationDetails;
    }

    private static IndividualDetails createIndividualDetails() {
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setFirstName("Harry");
        individualDetails.setLastName("Styles");
        List<String> hearingChannelEmail = new ArrayList<String>();
        hearingChannelEmail.add("harry.styles.neveragin1@gmailsss.com");
        hearingChannelEmail.add("harry.styles.neveragin2@gmailsss.com");
        hearingChannelEmail.add("harry.styles.neveragin3@gmailsss.com");
        individualDetails.setHearingChannelEmail(hearingChannelEmail);
        List<String> hearingChannelPhone = new ArrayList<String>();
        hearingChannelPhone.add("+447398087560");
        hearingChannelPhone.add("+447398087561");
        hearingChannelPhone.add("+447398087562");
        individualDetails.setHearingChannelPhone(hearingChannelPhone);
        individualDetails.setInterpreterLanguage("German");
        individualDetails.setPreferredHearingChannel("CBeebies");
        individualDetails.setReasonableAdjustments(createReasonableAdjustments());
        individualDetails.setRelatedParties(createRelatedParties());
        individualDetails.setVulnerableFlag(false);
        individualDetails.setVulnerabilityDetails("Vulnerability details 1");
        individualDetails.setCustodyStatus("ACTIVE");
        individualDetails.setOtherReasonableAdjustmentDetails("Other Reasonable Adjustment Details");
        return individualDetails;
    }

    private static List<RelatedParty> createRelatedParties() {
        RelatedParty relatedParty1 = new RelatedParty();
        relatedParty1.setRelatedPartyId("relatedParty1111");
        relatedParty1.setRelationshipType("Family");
        RelatedParty relatedParty2 = new RelatedParty();
        relatedParty2.setRelatedPartyId("relatedParty3333");
        relatedParty2.setRelationshipType("Blood Brother");

        List<RelatedParty> relatedParties = new ArrayList<>();
        relatedParties.add(relatedParty1);
        relatedParties.add(relatedParty2);
        return relatedParties;
    }

    private static PartyDetails createPartyDetails(String partyID, String partyType, String partyRole,
                                            IndividualDetails individualDetails,
                                            OrganisationDetails organisationDetails) {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID(partyID);
        partyDetails.setPartyType(partyType);
        partyDetails.setPartyRole(partyRole);
        partyDetails.setIndividualDetails(individualDetails);
        partyDetails.setOrganisationDetails(organisationDetails);
        partyDetails.setUnavailabilityRanges(createUnavailableDateRanges());
        partyDetails.setUnavailabilityDayOfWeek(createUnavailabilityDows());
        return partyDetails;
    }

    private static List<String> createReasonableAdjustments() {
        List<String> reasonableAdjustments = new ArrayList<>();
        reasonableAdjustments.add("adjust 1");
        reasonableAdjustments.add("adjust 2");
        reasonableAdjustments.add("adjust 3");
        return reasonableAdjustments;
    }

    private static List<UnavailabilityDayOfWeek> createUnavailabilityDows() {
        List<UnavailabilityDayOfWeek> unavailabilityDows = new ArrayList<>();
        UnavailabilityDayOfWeek unavailabilityDow1 = new UnavailabilityDayOfWeek();
        unavailabilityDow1.setDayOfWeek("DOW1");
        unavailabilityDow1.setDayOfWeekUnavailabilityType("TYPE1");
        unavailabilityDows.add(unavailabilityDow1);
        UnavailabilityDayOfWeek unavailabilityDow2 = new UnavailabilityDayOfWeek();
        unavailabilityDow2.setDayOfWeek("DOW1");
        unavailabilityDow2.setDayOfWeekUnavailabilityType("TYPE1");
        unavailabilityDows.add(unavailabilityDow2);
        return unavailabilityDows;
    }

    private static List<UnavailabilityRange> createUnavailableDateRanges() {
        UnavailabilityRange unavailabilityRanges1 = new UnavailabilityRange();
        unavailabilityRanges1.setUnavailableFromDate(LocalDate.parse("2021-01-01"));
        unavailabilityRanges1.setUnavailableToDate(LocalDate.parse("2021-01-15"));
        UnavailabilityRange unavailabilityRanges2 = new UnavailabilityRange();
        unavailabilityRanges2.setUnavailableFromDate(LocalDate.parse("2021-06-01"));
        unavailabilityRanges2.setUnavailableToDate(LocalDate.parse("2021-06-21"));

        List<UnavailabilityRange> listUnavailabilityRanges = new ArrayList<>();
        listUnavailabilityRanges.add(unavailabilityRanges1);
        listUnavailabilityRanges.add(unavailabilityRanges2);
        return listUnavailabilityRanges;
    }


    public static PactDslJsonBody generateValidHearingGetResponsePactDslJsonBody(LocalDateTime date) {
        PactDslJsonBody result = new PactDslJsonBody();

        result
            .object("requestDetails")
            .integerType("versionNumber", 123)
            .stringType("hearingRequestID", "hearingRequestID123")
            .stringType("status", "status123")
            .stringType("timestamp", date.toString())
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
            .stringType("dateRangeStart", date.toString())
            .stringType("dateRangeEnd", date.toString())
            .stringType("firstDateTimeMustBe", date.toString())
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
            .stringType("caseSLAStartDate", date.toString())
            .minArrayLike("caseCategories", 0, 1)
            .stringType("categoryType", "categoryType123")
            .stringType("categoryValue", "categoryValue123")
            .stringType("categoryParent", "categoryParent123")
            .closeObject().closeArray()
            .closeObject()
            .minArrayLike("partyDetails", 0, 1)
            .stringType("partyID", "partyID123")
            .stringType("partyType", "partyType123")
            .stringType("partyRole", "partyRole")
            .object("individualDetails")
            .stringType("firstName", "firstName123")
            .stringType("lastName", "lastName123")
            .stringType("preferredHearingChannel", "preferredHearingChannel123")
            .stringType("interpreterLanguage", "interpreterLanguage123")
            .stringType("vulnerabilityDetails", "vulnerabilityDetails123")
            .stringType("custodyStatus", "custodyStatus123")
            .stringType("otherReasonableAdjustmentDetails", "otherReasonableAdjustmentDetails123")
            .booleanValue("vulnerableFlag", true)
            .array("hearingChannelEmail")
            .string("hearingChannelEmail123@gmaild.com")
            .closeArray()
            .array("hearingChannelPhone")
            .string("07345960795")
            .closeArray()
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
            .stringType("unavailableFromDate", date.toString())
            .stringType("unavailableToDate", date.toString())
            .closeObject().closeArray()
            .closeObject().closeArray()
            .object("hearingResponse")
            .integerType("hearingRequestID", 123L)
            .integerType("versionNumber", 123)
            .stringType("status", ACTIVE)
            .stringType("timeStamp", date.toString())
            .stringType("listAssistTransactionID", "ListAssistTransactionID123123")
            .stringType("receivedDateTime", date.toString())
            .stringType("laCaseStatus", "Listed")
            .stringType("listingStatus", "Fixed")
            .stringType("hearingCancellationReason", "hearingCancellationReason_NO_RESULT")
            .stringType("partiesNotified", date.toString())
            .integerType("requestVersion", 321)
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
            .object("serviceData")
            .closeObject()
            .closeObject();


        return result;
    }

}
