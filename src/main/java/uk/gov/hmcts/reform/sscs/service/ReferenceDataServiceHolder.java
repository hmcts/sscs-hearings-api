package uk.gov.hmcts.reform.sscs.service;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
@AllArgsConstructor
public class ReferenceDataServiceHolder {

    @Autowired
    private HearingDurationsService hearingDurations;
    
    @Autowired
    private SessionCategoryMapService sessionCategoryMaps;

    @Autowired
    private VenueService venueService;
    
    @Value("${exui.url}")
    private String exUiUrl;
    
    @Value("${sscs.serviceCode}")
    private String sscsServiceCode; 

}
