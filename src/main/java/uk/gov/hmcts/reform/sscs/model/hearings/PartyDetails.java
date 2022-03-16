package uk.gov.hmcts.reform.sscs.model.hearings;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartyDetails {

    private String partyID;

    private String partyType;

    private String partyRole;

    private IndividualDetails individualDetails;

    private OrganisationDetails organisationDetails;

    @JsonProperty("unavailabilityDOW")
    private List<UnavailabilityDayOfWeek> unavailabilityDoW;

    private List<UnavailabilityRange> unavailabilityRanges;
}
