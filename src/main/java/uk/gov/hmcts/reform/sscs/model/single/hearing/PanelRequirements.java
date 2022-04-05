package uk.gov.hmcts.reform.sscs.model.single.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
<<<<<<< HEAD
=======
import com.fasterxml.jackson.annotation.JsonProperty;
>>>>>>> 9692bd47575ca92d430a80443fd0fbc7af1611a8
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PanelRequirements {

<<<<<<< HEAD
    private List<String> roleTypes;

=======
    @JsonProperty("roleType")
    private List<String> roleTypes;

    @JsonProperty("authorisationSubType")
>>>>>>> 9692bd47575ca92d430a80443fd0fbc7af1611a8
    private List<String> authorisationSubTypes;

    private List<PanelPreference> panelPreferences;

    private List<String> panelSpecialisms;
}
