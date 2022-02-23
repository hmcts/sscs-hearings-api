package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OneOf {
    
    @JsonProperty("individualDetails")
    private IndividualDetails individualDetails;
    @JsonProperty("organisationDetails")
    private OrganisationDetails organisationDetails;

    public IndividualDetails getIndividualDetails() {
        return individualDetails;
    }

    public void setIndividualDetails(IndividualDetails individualDetails) {
        this.individualDetails = individualDetails;
    }

    public OrganisationDetails getOrganisationDetails() {
        return organisationDetails;
    }

    public void setOrganisationDetails(OrganisationDetails organisationDetails) {
        this.organisationDetails = organisationDetails;
    }
}
