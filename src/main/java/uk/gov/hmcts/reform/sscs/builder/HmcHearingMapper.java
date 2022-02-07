package uk.gov.hmcts.reform.sscs.builder;

import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.service.AirLookupService;
import uk.gov.hmcts.reform.sscs.service.DwpAddressLookupService;

import java.util.List;

@Component
@Slf4j
public class HmcHearingMapper {

    private final DwpAddressLookupService dwpAddressLookupService;
    private final AirLookupService airLookupService;

    @Autowired
    public HmcHearingMapper(DwpAddressLookupService dwpAddressLookupService,
                            AirLookupService airLookupService) {
        this.dwpAddressLookupService = dwpAddressLookupService;
        this.airLookupService = airLookupService;
    }

    public JSONObject map(SscsCaseData sscsCaseData, Hearing hearing) {

        JSONObject json = new JSONObject();

        json.put("requestDetails", buildRequestDetails(sscsCaseData, hearing.getRequestDetails()));

        json.put("hearingDetails", buildHearingDetails(sscsCaseData, hearing.getHearingDetails()));

        json.put("caseDetails", buildCaseDetails(sscsCaseData, hearing.getHearingDetails()));

        json.put("partyDetails", buildPartiesDetails(sscsCaseData.getParties()));

        return json;
    }

    private JSONObject buildRequestDetails(SscsCaseData sscsCaseData, HearingRequest requestDetails) {
        JSONObject json = new JSONObject();

        DateTime requestTimeStamp;
        int versionNumber;

        return json;
    }



    private JSONObject buildHearingDetails(SscsCaseData sscsCaseData, HearingDetails hearingDetails) {
        JSONObject json = new JSONObject();

        boolean autolistFlag;
        String hearingType;
        HearingWindow hearingWindow;
        int duration;
        List<String> nonStandardHearingDurationReasons;
        String hearingPriorityType;
        int numberOfPhysicalAttendees;
        boolean hearingInWelshFlag;
        List<HearingLocation> hearingLocations;
        List<String> facilitiesRequired;
        String listingComments;
        String hearingRequester;
        boolean privateHearingRequiredFlag;
        boolean leadJudgeContractType;
        PanelRequirements panelRequirements;
        boolean hearingIsLinkedFlag;


        return json;
    }

    private JSONObject buildCaseDetails(SscsCaseData sscsCaseData, HearingDetails hearingDetails) {
        JSONObject json = new JSONObject();

        String hmctsServiceCode; //4 letter code? Ask BA
        String caseRef; //I think it's pulled from SscsCaseData.ccdCaseId
        DateTime requestTimeStamp; //Same as Hearing.requestDetails.requestTimeStamp
        String externalCaseReference; // Not used by HMC or List Assist, for 'other components / Notifications'
        String caseDeepLink; // Not sure what this is, will be generated
        String hmctsInternalCaseName; // Pulled from WorkAllocationFields.caseNameHmctsInternal
        String publicCaseName; // Pulled from WorkAllocationFields.caseNamePublic
        boolean caseAdditionalSecurityFlag; // Might be the same as robotics flag OtherParty.unacceptableCustomerBehaviour, I suspect if any party has this flag, this is flagged.
        boolean caseInterpreterRequiredFlag; // If any party needs a Interpreter, this is flagged.
        List<HmcCaseCategory> caseCategories;
        String caseManagementLocationCode; // Not sure what this is or if we can generate this, BA?
        boolean caserestrictedFlag; // Might be the same as isConfidentialCase and/or OtherParty.confidentialityRequired, not sure.
        String caseSLAStartDate; // I'm pretty sure this is sscsCaseData.caseCreated, format may need refactoring, if not caseCreated Event.EventDetails.date, Event.EventDetails.type = APPEAL_RECEIVED could be used.

        return json;
    }

    private JSONArray buildPartiesDetails(List<Party> parties) {
        JSONArray jsonArray = new JSONArray();

        for (Party party : parties) {
            if (party != null) {
                jsonArray.add(buildPartyDetails(party));
            }
        }

        return jsonArray;
    }

    private JSONObject buildPartyDetails(Party party) {
        JSONObject json = new JSONObject();

        String partyID;
        String partyType;
        String partyRole;
        IndividualDetails individualDetails;
        OrganisationDetails organisationDetails;
        List<UnavailabilityDow> unavailabilityDOW;
        List<InavailabilityRange> unavailabilityRanges;


        return json;
    }

}
