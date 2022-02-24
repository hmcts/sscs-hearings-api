package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PanelRequirements {

    private String roleType;
    private String authorisationSubType;
    private PanelPreference panelPreferences;
    private String panelSpecialisms;
    private String panelSpecialism;
}
