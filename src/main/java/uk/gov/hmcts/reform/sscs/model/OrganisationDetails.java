package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganisationDetails {

    @JsonProperty("name")
    private String name;
    @JsonProperty("organisationType")
    private String organisationType;
    @JsonProperty("cftOrganisationID")
    private String cftOrganisationID;
}
