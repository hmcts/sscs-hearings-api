package uk.gov.hmcts.reform.sscs.model.servicehearingvalues;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.model.single.hearing.IndividualDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.OrganisationDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PartyType;
import uk.gov.hmcts.reform.sscs.validator.EnumPattern;

import java.util.List;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class ShvPartyDetails {

    private String partyID;
    //@JsonProperty("SvhPartyType")
    @EnumPattern(enumClass = PartyType.class, fieldName = "partyType")
    private PartyType partyType;
    private String partyName;
    private String partyChannel;
    private String partyRole;
    //@JsonProperty("individualDetails")
    private IndividualDetails individualDetails;
    //@JsonProperty("organisationDetails")
    private OrganisationDetails organisationDetails;
    //@JsonProperty("unavailabilityDow")
    private List<ShvUnavailabilityDayOfWeek> unavailabilityDow;
    //@JsonProperty("unavailabilityRanges")
    private List<UnavailabilityRange> unavailabilityRanges;
}
