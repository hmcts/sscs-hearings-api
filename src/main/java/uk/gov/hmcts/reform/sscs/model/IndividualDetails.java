package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IndividualDetails {

    @JsonProperty("title")
    private String title;
    @JsonProperty("firstName")
    private String firstName;
    @JsonProperty("lastName")
    private String lastName;
    @JsonProperty("preferredHearingChannel")
    private String preferredHearingChannel;
    @JsonProperty("interpreterLanguage")
    private String interpreterLanguage;
    @JsonProperty("reasonableAdjustments")
    private String reasonableAdjustments;
    @JsonProperty("vulnerableFlag")
    private  boolean vulnerableFlag;
    @JsonProperty("vulnerabilityDetails")
    private String vulnerabilityDetails;
    @JsonProperty("hearingChannelEmail")
    private  String hearingChannelEmail;
    @JsonProperty("hearingChannelPhone")
    private String hearingChannelPhone;
    @JsonProperty("relatedParties")
    private RelatedParty relatedParties;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPreferredHearingChannel() {
        return preferredHearingChannel;
    }

    public void setPreferredHearingChannel(String preferredHearingChannel) {
        this.preferredHearingChannel = preferredHearingChannel;
    }

    public String getInterpreterLanguage() {
        return interpreterLanguage;
    }

    public void setInterpreterLanguage(String interpreterLanguage) {
        this.interpreterLanguage = interpreterLanguage;
    }

    public String getReasonableAdjustments() {
        return reasonableAdjustments;
    }

    public void setReasonableAdjustments(String reasonableAdjustments) {
        this.reasonableAdjustments = reasonableAdjustments;
    }

    public boolean isVulnerableFlag() {
        return vulnerableFlag;
    }

    public void setVulnerableFlag(boolean vulnerableFlag) {
        this.vulnerableFlag = vulnerableFlag;
    }

    public String getVulnerabilityDetails() {
        return vulnerabilityDetails;
    }

    public void setVulnerabilityDetails(String vulnerabilityDetails) {
        this.vulnerabilityDetails = vulnerabilityDetails;
    }

    public String getHearingChannelEmail() {
        return hearingChannelEmail;
    }

    public void setHearingChannelEmail(String hearingChannelEmail) {
        this.hearingChannelEmail = hearingChannelEmail;
    }

    public String getHearingChannelPhone() {
        return hearingChannelPhone;
    }

    public void setHearingChannelPhone(String hearingChannelPhone) {
        this.hearingChannelPhone = hearingChannelPhone;
    }

    public RelatedParty getRelatedParties() {
        return relatedParties;
    }

    public void setRelatedParties(RelatedParty relatedParties) {
        this.relatedParties = relatedParties;
    }
}
