package uk.gov.hmcts.reform.sscs.model.service.hearingvalues;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class ScreenNavigation {
    private String screenName;
    private String conditionKey;
    private List<Navigation> navigation;
}
