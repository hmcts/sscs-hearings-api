package uk.gov.hmcts.reform.sscs.model.hmc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.exceptions.ValidationError;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganisationDetails {

    @NotEmpty(message = ValidationError.NAME_NULL_EMPTY)
    @Size(max = 2000, message = ValidationError.NAME_MAX_LENGTH)
    private String name;

    @NotEmpty(message = ValidationError.ORGANISATION_TYPE_NULL_EMPTY)
    @Size(max = 60, message = ValidationError.ORGANISATION_TYPE_MAX_LENGTH)
    private String organisationType;

    @NotEmpty(message = ValidationError.CFT_ORG_ID_NULL_EMPTY)
    @Size(max = 60, message = ValidationError.CFT_ORG_ID_MAX_LENGTH)
    private String cftOrganisationID;
}
