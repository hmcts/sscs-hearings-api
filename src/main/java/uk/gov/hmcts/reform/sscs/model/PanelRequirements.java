package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.reform.sscs.exception.ValidationError.AUTHORISATION_SUB_TYPE_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.sscs.exception.ValidationError.PANEL_SPECIALISMS_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.sscs.exception.ValidationError.ROLE_TYPE_MAX_LENGTH_MSG;

@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PanelRequirements {

    private List<@Size(max = 70, message = ROLE_TYPE_MAX_LENGTH_MSG) String> roleType;
    private List<@Size(max = 70, message = AUTHORISATION_SUB_TYPE_MAX_LENGTH_MSG) String> authorisationSubType;

    @Valid
    private List<PanelPreference> panelPreferences;

    private List<@Size(max = 70, message = PANEL_SPECIALISMS_MAX_LENGTH_MSG) String> panelSpecialisms;
}
