package uk.gov.hmcts.reform.sscs.model.single.hearing;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
@Builder
public class PanelRequirements {

    private List<String> roleTypes;

    private List<String> authorisationSubTypes;

    private List<PanelPreference> panelPreferences;

    private List<String> panelSpecialisms;
}
