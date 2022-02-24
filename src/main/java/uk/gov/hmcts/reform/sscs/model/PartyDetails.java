package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder(toBuilder = true)
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartyDetails {

    private String partyID;
    private String partyType;
    private String partyRole;
    @JsonProperty("unavailabilityDOW")
    private UnavailabilityDoW unavailabilityDoW;
    private UnavailabilityRange unavailabilityRanges;
    private List<OneOf> oneOf;
}
