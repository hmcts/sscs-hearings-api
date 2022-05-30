package uk.gov.hmcts.reform.sscs.service;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class ReferenceData {
    @Autowired
    private HearingDurationsService hearingDurations;
    
    @Autowired
    private SessionCategoryMapService sessionCategoryMaps;
    
    @Value("${exui.url}")
    private String exUiUrl;
    
    @Value("${sscs.serviceCode}")
    private String sscsServiceCode;


}
