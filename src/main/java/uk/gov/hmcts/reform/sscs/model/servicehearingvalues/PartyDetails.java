package uk.gov.hmcts.reform.sscs.model.servicehearingvalues;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class PartyDetails {

    private String partyID;
    private PartyType partyType;
    private String partyName;
    private String partyChannel;
    private String partyRole;
    private IndividualDetails individualDetails;
    private OrganisationDetails organisationDetails;
    @JsonProperty("unavailabilityDOW")
    private List<UnavailabilityDOW> unavailabilityDow;
    private List<UnavailabilityRange> unavailabilityRanges;
}
