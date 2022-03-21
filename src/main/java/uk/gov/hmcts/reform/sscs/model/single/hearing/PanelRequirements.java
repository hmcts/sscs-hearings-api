package uk.gov.hmcts.reform.sscs.model.single.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PanelRequirements {

    private List<String> roleType;

    private List<String> authorisationSubType;

    private List<PanelPreference> panelPreferences;

    private List<String> panelSpecialisms;
}
