package uk.gov.hmcts.reform.sscs.model.single.hearing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.sscs.validator.EnumPattern;

import java.util.List;

@Data
@RequiredArgsConstructor
@Builder
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
