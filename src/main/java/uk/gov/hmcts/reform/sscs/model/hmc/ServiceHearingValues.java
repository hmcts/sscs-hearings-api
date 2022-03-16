package uk.gov.hmcts.reform.sscs.model.hmc;

import lombok.Builder;
import lombok.Data;

@SuppressWarnings("PMD.TooManyFields")
@Data
@Builder
public class ServiceHearingValues {

    //This is just a skeleton to be expanded on further along with wrapper objects
    private String caseName;
    private boolean autoListFlag;
    private String hearingType;
    private String caseType;
    private String[] caseSubTypes;
    private HearingWindow hearingWindow;
    private Number duration;
    private String hearingPriorityType;
    private Number numberOfPhysicalAttendees;
    private boolean hearingInWelshFlag;
    private HearingLocation[] hearingLocations;
    private boolean caseAdditionalSecurityFlag;
    private String[] facilitiesRequired;
    private String listingComments;
    private String hearingRequester;
    private boolean privateHearingRequiredFlag;
    private String leadJudgeContractType;
    private Judiciary judiciary;
    private boolean hearingIsLinkedFlag;
    private PartyDetails[] parties;
    private CaseFlags caseFlags;
    private ScreenNavigation[] screenFlow;
    private Vocabulary[] vocabulary;
}
