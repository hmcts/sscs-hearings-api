package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PanelRequirements {

    @JsonProperty("roleType")
    private String roleType;
    @JsonProperty("authorisationSubType")
    private String authorisationSubType;
    @JsonProperty("panelPreferences")
    private PanelPreference panelPreferences;
    @JsonProperty("panelSpecialisms")
    private String panelSpecialisms;
    @JsonProperty("panelSpecialism")
    private String panelSpecialism;

    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }

    public String getAuthorisationSubType() {
        return authorisationSubType;
    }

    public void setAuthorisationSubType(String authorisationSubType) {
        this.authorisationSubType = authorisationSubType;
    }

    public PanelPreference getPanelPreferences() {
        return panelPreferences;
    }

    public void setPanelPreferences(PanelPreference panelPreferences) {
        this.panelPreferences = panelPreferences;
    }

    public String getPanelSpecialisms() {
        return panelSpecialisms;
    }

    public void setPanelSpecialisms(String panelSpecialisms) {
        this.panelSpecialisms = panelSpecialisms;
    }

    public String getPanelSpecialism() {
        return panelSpecialism;
    }

    public void setPanelSpecialism(String panelSpecialism) {
        this.panelSpecialism = panelSpecialism;
    }
}
