package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;

@Builder(toBuilder = true)
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartyDetails {

    @JsonProperty("partyID")
    private String partyID;
    @JsonProperty("partyType")
    private String partyType;
    @JsonProperty("partyRole")
    private String partyRole;
    @JsonProperty("unavailabilityDOW")
    private UnavailabilityDOW unavailabilityDOW;
    @JsonProperty("unavailabilityRanges")
    private UnavailabilityRange unavailabilityRanges;
    @JsonProperty("oneOf")
    private ArrayList<OneOf> oneOf;

    public ArrayList<OneOf> getOneOf() {
        return oneOf;
    }

    public void setOneOf(ArrayList<OneOf> oneOf) {
        this.oneOf = oneOf;
    }

    public String getPartyID() {
        return partyID;
    }

    public void setPartyID(String partyID) {
        this.partyID = partyID;
    }

    public String getPartyType() {
        return partyType;
    }

    public void setPartyType(String partyType) {
        this.partyType = partyType;
    }

    public String getPartyRole() {
        return partyRole;
    }

    public void setPartyRole(String partyRole) {
        this.partyRole = partyRole;
    }

    public UnavailabilityDOW getUnavailabilityDOW() {
        return unavailabilityDOW;
    }

    public void setUnavailabilityDOW(UnavailabilityDOW unavailabilityDOW) {
        this.unavailabilityDOW = unavailabilityDOW;
    }

    public UnavailabilityRange getUnavailabilityRanges() {
        return unavailabilityRanges;
    }

    public void setUnavailabilityRanges(UnavailabilityRange unavailabilityRanges) {
        this.unavailabilityRanges = unavailabilityRanges;
    }
}
