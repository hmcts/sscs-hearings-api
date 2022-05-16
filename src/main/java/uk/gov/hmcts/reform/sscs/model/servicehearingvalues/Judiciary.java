package uk.gov.hmcts.reform.sscs.model.servicehearingvalues;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class Judiciary {

    private List<String> roleType;
    private List<String> authorisationTypes;
    private List<String> authorisationSubType;
    @JsonProperty("panelComposition")
    private List<PanelComposition> shvPanelComposition;
    @JsonProperty("judiciaryPreferences")
    private List<PanelPreference> shvJudiciaryPreferences;
    private List<String> judiciarySpecialisms;
}
