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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrganisationType() {
        return organisationType;
    }

    public void setOrganisationType(String organisationType) {
        this.organisationType = organisationType;
    }

    public String getCftOrganisationID() {
        return cftOrganisationID;
    }

    public void setCftOrganisationID(String cftOrganisationID) {
        this.cftOrganisationID = cftOrganisationID;
    }
}
