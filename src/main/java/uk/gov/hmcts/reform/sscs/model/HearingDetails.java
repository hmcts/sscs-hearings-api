package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;

@Builder(toBuilder = true)
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingDetails {

    @JsonProperty("autolistFlag")
    private boolean autolistFlag;
    @JsonProperty("hearingType")
    private String hearingType;
    @JsonProperty("hearingWindow")
    private HearingWindow hearingWindow;
    @JsonProperty("duration")
    private Integer duration;
    @JsonProperty("nonStandardHearingDurationReasons")
    private ArrayList<String> nonStandardHearingDurationReasons;
    @JsonProperty("hearingPriorityType")
    private String hearingPriorityType;
    @JsonProperty("numberOfPhysicalAttendees")
    private Integer numberOfPhysicalAttendees;
    @JsonProperty("hearingInWelshFlag")
    private boolean hearingInWelshFlag;
    @JsonProperty("hearingLocation")
    private ArrayList<HearingLocations> hearingLocation;
    @JsonProperty("facilitiesRequired")
    private ArrayList<String> facilityType;
    @JsonProperty("listingComments")
    private ArrayList<String> listingComments;
    @JsonProperty("hearingRequester")
    private String hearingRequester;
    @JsonProperty("privateHearingRequiredFlag")
    private boolean privateHearingRequiredFlag;
    @JsonProperty("leadJudgeContractType")
    private String leadJudgeContractType;
    @JsonProperty("panelRequirements")
    private PanelRequirements panelRequirements;
    @JsonProperty("hearingIsLinkedFlag")
    private boolean hearingIsLinkedFlag;

    public boolean isAutolistFlag() {
        return autolistFlag;
    }

    public void setAutolistFlag(boolean autolistFlag) {
        this.autolistFlag = autolistFlag;
    }

    public String getHearingType() {
        return hearingType;
    }

    public void setHearingType(String hearingType) {
        this.hearingType = hearingType;
    }

    public HearingWindow getHearingWindow() {
        return hearingWindow;
    }

    public void setHearingWindow(HearingWindow hearingWindow) {
        this.hearingWindow = hearingWindow;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public ArrayList<String> getNonStandardHearingDurationReasons() {
        return nonStandardHearingDurationReasons;
    }

    public void setNonStandardHearingDurationReasons(ArrayList<String> nonStandardHearingDurationReasons) {
        this.nonStandardHearingDurationReasons = nonStandardHearingDurationReasons;
    }

    public String getHearingPriorityType() {
        return hearingPriorityType;
    }

    public void setHearingPriorityType(String hearingPriorityType) {
        this.hearingPriorityType = hearingPriorityType;
    }

    public Integer getNumberOfPhysicalAttendees() {
        return numberOfPhysicalAttendees;
    }

    public void setNumberOfPhysicalAttendees(Integer numberOfPhysicalAttendees) {
        this.numberOfPhysicalAttendees = numberOfPhysicalAttendees;
    }

    public boolean isHearingInWelshFlag() {
        return hearingInWelshFlag;
    }

    public void setHearingInWelshFlag(boolean hearingInWelshFlag) {
        this.hearingInWelshFlag = hearingInWelshFlag;
    }

    public ArrayList<HearingLocations> getHearingLocation() {
        return hearingLocation;
    }

    public void setHearingLocation(ArrayList<HearingLocations> hearingLocation) {
        this.hearingLocation = hearingLocation;
    }

    public ArrayList<String> getFacilityType() {
        return facilityType;
    }

    public void setFacilityType(ArrayList<String> facilityType) {
        this.facilityType = facilityType;
    }

    public ArrayList<String> getListingComments() {
        return listingComments;
    }

    public void setListingComments(ArrayList<String> listingComments) {
        this.listingComments = listingComments;
    }

    public String getHearingRequester() {
        return hearingRequester;
    }

    public void setHearingRequester(String hearingRequester) {
        this.hearingRequester = hearingRequester;
    }

    public boolean getPrivateHearingRequiredFlag() {
        return privateHearingRequiredFlag;
    }

    public void setPrivateHearingRequiredFlag(boolean privateHearingRequiredFlag) {
        this.privateHearingRequiredFlag = privateHearingRequiredFlag;
    }

    public String getLeadJudgeContractType() {
        return leadJudgeContractType;
    }

    public void setLeadJudgeContractType(String leadJudgeContractType) {
        this.leadJudgeContractType = leadJudgeContractType;
    }

    public PanelRequirements getPanelRequirements() {
        return panelRequirements;
    }

    public void setPanelRequirements(PanelRequirements panelRequirements) {
        this.panelRequirements = panelRequirements;
    }

    public boolean isHearingIsLinkedFlag() {
        return hearingIsLinkedFlag;
    }

    public void setHearingIsLinkedFlag(boolean hearingIsLinkedFlag) {
        this.hearingIsLinkedFlag = hearingIsLinkedFlag;
    }
}
