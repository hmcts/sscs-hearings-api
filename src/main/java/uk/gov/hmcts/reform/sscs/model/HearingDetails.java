package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder(toBuilder = true)
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingDetails {

    private boolean autolistFlag;
    private String hearingType;
    private HearingWindow hearingWindow;
    private Integer duration;
    private List<String> nonStandardHearingDurationReasons;
    private String hearingPriorityType;
    private Integer numberOfPhysicalAttendees;
    private boolean hearingInWelshFlag;
    private List<HearingLocations> hearingLocation;
    private List<String> facilityType;
    private List<String> listingComments;
    private String hearingRequester;
    private boolean privateHearingRequiredFlag;
    private String leadJudgeContractType;
    private PanelRequirements panelRequirements;
    private boolean hearingIsLinkedFlag;
}
