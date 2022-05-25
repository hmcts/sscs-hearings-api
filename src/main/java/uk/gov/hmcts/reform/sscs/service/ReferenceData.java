package uk.gov.hmcts.reform.sscs.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class ReferenceData {
    private HearingDurationsService hearingDurations;
    private SessionCategoryMapService sessionCategoryMaps;
    @Value("${exui.url}")
    private String exUiUrl;
    @Value("${sscs.serviceCode}")
    private String sscsServiceCode;


}
