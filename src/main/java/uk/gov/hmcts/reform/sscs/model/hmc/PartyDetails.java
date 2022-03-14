package uk.gov.hmcts.reform.sscs.model.hmc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.exceptions.ValidationError;
import uk.gov.hmcts.reform.sscs.validator.EnumPattern;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartyDetails {

    @NotEmpty(message = ValidationError.PARTY_DETAILS_NULL_EMPTY)
    @Size(max = 40, message = ValidationError.PARTY_DETAILS_MAX_LENGTH)
    private String partyID;

    @NotEmpty(message = ValidationError.PARTY_TYPE_EMPTY)
    @EnumPattern(enumClass = PartyType.class, fieldName = "partyType")
    private String partyType;

    @NotEmpty(message = ValidationError.PARTY_ROLE_EMPTY)
    @Size(max = 6, message = ValidationError.PARTY_ROLE_MAX_LENGTH)
    private String partyRole;

    @Valid
    private IndividualDetails individualDetails;

    @Valid
    private OrganisationDetails organisationDetails;

    @JsonProperty("unavailabilityDOW")
    @Valid
    private List<UnavailabilityDoW> unavailabilityDoW;

    @Valid
    private List<UnavailabilityRange> unavailabilityRanges;
}
