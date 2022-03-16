package uk.gov.hmcts.reform.sscs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.sscs.model.hearings.CaseCategory;
import uk.gov.hmcts.reform.sscs.model.hearings.CaseDetails;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingDetails;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingLocations;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequestPayload;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingWindow;
import uk.gov.hmcts.reform.sscs.model.hearings.IndividualDetails;
import uk.gov.hmcts.reform.sscs.model.hearings.OrganisationDetails;
import uk.gov.hmcts.reform.sscs.model.hearings.PanelPreference;
import uk.gov.hmcts.reform.sscs.model.hearings.PanelRequirements;
import uk.gov.hmcts.reform.sscs.model.hearings.PartyDetails;
import uk.gov.hmcts.reform.sscs.model.hearings.RelatedParty;
import uk.gov.hmcts.reform.sscs.model.hearings.RequestDetails;
import uk.gov.hmcts.reform.sscs.model.hearings.UnavailabilityDayOfWeek;
import uk.gov.hmcts.reform.sscs.model.hearings.UnavailabilityRange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BasePactTesting {

    protected static final Logger logger = LoggerFactory.getLogger(BasePactTesting.class);

    public static final String CONSUMER_NAME = "hmcHearingServiceConsumer";

    protected static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    protected static final String IDAM_OAUTH2_TOKEN = "pact-test-idam-token";
    protected static final String SERVICE_AUTHORIZATION_TOKEN = "pact-test-s2s-token";

    public static final String MSG_200_POST_HEARING = "Success (with content)";
    public static final String MSG_400_POST_HEARING = "Invalid request";

    protected static final String CONTENT_TYPE = "Content-Type";
    protected static final String APPLICATION_JSON = "application/json";

    protected static final Map<String, String> headers = Map.of(
        HttpHeaders.AUTHORIZATION, IDAM_OAUTH2_TOKEN,
        SERVICE_AUTHORIZATION, SERVICE_AUTHORIZATION_TOKEN,
        CONTENT_TYPE, APPLICATION_JSON
    );

    protected HearingRequestPayload generateHearingRequest() {
        HearingRequestPayload request = new HearingRequestPayload();
        request.setRequestDetails(requestDetails());
        request.setHearingDetails(hearingDetails());
        request.setCaseDetails(caseDetails());
        request.setPartyDetails(partyDetails1());

        return request;
    }

    protected HearingRequestPayload generateInvalidHearingRequest() {
        HearingRequestPayload request = new HearingRequestPayload();
        request.setHearingDetails(hearingDetails());
        request.setPartyDetails(partyDetails2());
        request.setRequestDetails(requestDetails());
        return request;
    }

    protected String toJsonString(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String jsonString = "";
        try {
            jsonString = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        logger.info("toJsonString: {}", jsonString);
        return jsonString;
    }

    protected RequestDetails requestDetails() {
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setRequestTimeStamp("030-08-20T12:40:00.000Z");
        requestDetails.setVersionNumber(123);
        return requestDetails;
    }

    protected HearingDetails hearingDetails() {
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
        return hearingDetails;
    }


    protected HearingWindow hearingWindow() {
        HearingWindow hearingWindow = new HearingWindow();
        hearingWindow.setDateRangeStart("01/02/2020");
        hearingWindow.setDateRangeEnd("12/02/2020");

        return hearingWindow;
    }

    protected CaseDetails caseDetails() {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setHmctsServiceCode("ABBA1");
        caseDetails.setCaseRef("ba12");
        caseDetails.setRequestTimeStamp("2030-08-20T12:40:00.000Z");
        caseDetails.setCaseDeepLink("https://www.google.com");
        caseDetails.setHmctsInternalCaseName("Internal case name");
        caseDetails.setPublicCaseName("Public case name");
        caseDetails.setCaseManagementLocationCode("CMLC123");
        caseDetails.setCaseRestrictedFlag(false);
        caseDetails.setCaseSlaStartDate("2030-08-20");
        CaseCategory category = new CaseCategory();
        category.setCategoryType("caseType");
        category.setCategoryValue("PROBATE");
        List<CaseCategory> caseCategories = new ArrayList<>();
        caseCategories.add(category);
        caseDetails.setCaseCategories(caseCategories);
        return caseDetails;
    }

    protected PanelRequirements panelRequirements1() {
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
        panelRequirements.setRoleType(roleType);
        panelRequirements.setAuthorisationSubType(authorisationSubType);
        panelRequirements.setPanelPreferences(panelPreferences);
        panelRequirements.setPanelSpecialisms(panelSpecialisms);

        return panelRequirements;
    }

    protected List<PartyDetails> partyDetails1() {
        ArrayList<PartyDetails> partyDetailsArrayList = new ArrayList<>();
        partyDetailsArrayList.add(createPartyDetails("P1", "IND", "DEF", null, createOrganisationDetails()));
        partyDetailsArrayList.add(createPartyDetails("P2", "IND", "DEF2", createIndividualDetails(), null));
        partyDetailsArrayList.add(createPartyDetails("P3", "IND", "DEF3", createIndividualDetails(),
                                                     createOrganisationDetails()
        ));
        return partyDetailsArrayList;
    }

    private List<PartyDetails> partyDetails2() {
        ArrayList<PartyDetails> partyDetailsArrayList = new ArrayList<>();
        partyDetailsArrayList.add(createPartyDetails("P1", "IND", "DEF", null, createOrganisationDetails()));
        partyDetailsArrayList.add(createPartyDetails("P2", "IND2", "DEF2", createIndividualDetails(), null));
        partyDetailsArrayList.add(createPartyDetails("P3", "IND3", "DEF3", null, createOrganisationDetails()));
        partyDetailsArrayList.add(createPartyDetails("P4", "IND4", "DEF4", createIndividualDetails(),
                                                     createOrganisationDetails()
        ));
        return partyDetailsArrayList;
    }

    private OrganisationDetails createOrganisationDetails() {
        OrganisationDetails organisationDetails = new OrganisationDetails();
        organisationDetails.setName("name");
        organisationDetails.setOrganisationType("organisationType");
        organisationDetails.setCftOrganisationID("cftOrganisationId01001");
        return organisationDetails;
    }

    private IndividualDetails createIndividualDetails() {
        IndividualDetails individualDetails = new IndividualDetails();
        individualDetails.setTitle("Master");
        individualDetails.setFirstName("Harry");
        individualDetails.setLastName("Styles");
        individualDetails.setHearingChannelEmail("harry.styles.neveragin@gmailsss.com");
        individualDetails.setInterpreterLanguage("German");
        individualDetails.setPreferredHearingChannel("CBeebies");
        individualDetails.setReasonableAdjustments(createReasonableAdjustments());
        individualDetails.setRelatedParties(createRelatedParties());
        individualDetails.setVulnerableFlag(false);
        individualDetails.setVulnerabilityDetails("Vulnerability details 1");
        return individualDetails;
    }

    private List<RelatedParty> createRelatedParties() {
        RelatedParty relatedParty1 = new RelatedParty();
        relatedParty1.setRelatedPartyID("relatedParty1111");
        relatedParty1.setRelationshipType("Family");
        RelatedParty relatedParty2 = new RelatedParty();
        relatedParty2.setRelatedPartyID("relatedParty3333");
        relatedParty2.setRelationshipType("Blood Brother");

        List<RelatedParty> relatedParties = new ArrayList<>();
        relatedParties.add(relatedParty1);
        relatedParties.add(relatedParty2);
        return relatedParties;
    }

    private PartyDetails createPartyDetails(String partyID, String partyType, String partyRole,
                                            IndividualDetails individualDetails,
                                            OrganisationDetails organisationDetails) {
        PartyDetails partyDetails = new PartyDetails();
        partyDetails.setPartyID(partyID);
        partyDetails.setPartyType(partyType);
        partyDetails.setPartyRole(partyRole);
        if (null != individualDetails) {
            partyDetails.setIndividualDetails(individualDetails);
        }
        if (null != organisationDetails) {
            partyDetails.setOrganisationDetails(organisationDetails);
        }
        partyDetails.setUnavailabilityRanges(createUnavailableDateRanges());
        partyDetails.setUnavailabilityDoW(createUnavailabilityDows());
        return partyDetails;
    }

    private List<String> createReasonableAdjustments() {
        List<String> reasonableAdjustments = new ArrayList<>();
        reasonableAdjustments.add("adjust 1");
        reasonableAdjustments.add("adjust 2");
        reasonableAdjustments.add("adjust 3");
        return reasonableAdjustments;
    }

    private List<UnavailabilityDayOfWeek> createUnavailabilityDows() {
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

    private List<UnavailabilityRange> createUnavailableDateRanges() {
        UnavailabilityRange unavailabilityRanges1 = new UnavailabilityRange();
        unavailabilityRanges1.setUnavailableFromDate("2021-01-01");
        unavailabilityRanges1.setUnavailableToDate("2021-01-15");
        UnavailabilityRange unavailabilityRanges2 = new UnavailabilityRange();
        unavailabilityRanges2.setUnavailableFromDate("2021-06-01");
        unavailabilityRanges2.setUnavailableToDate("2021-06-21");

        List<UnavailabilityRange> listUnavailabilityRanges = new ArrayList<>();
        listUnavailabilityRanges.add(unavailabilityRanges1);
        listUnavailabilityRanges.add(unavailabilityRanges2);
        return listUnavailabilityRanges;
    }

}
