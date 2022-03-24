package uk.gov.hmcts.reform.sscs.model.single.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.validator.EnumPattern;

import java.util.List;

@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartyDetails {

    private String partyID;

    @EnumPattern(enumClass = PartyType.class, fieldName = "partyType")
    private String partyType;

    private String partyRole;

    private IndividualDetails individualDetails;

    private OrganisationDetails organisationDetails;

    @JsonProperty("unavailabilityDOW")
    private List<UnavailabilityDayOfWeek> unavailabilityDayOfWeek;

    private List<UnavailabilityRange> unavailabilityRanges;
}
