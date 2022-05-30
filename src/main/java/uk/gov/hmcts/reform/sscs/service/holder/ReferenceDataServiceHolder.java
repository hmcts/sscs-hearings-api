package uk.gov.hmcts.reform.sscs.service.holder;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.service.HearingDurationsService;
import uk.gov.hmcts.reform.sscs.service.SessionCategoryMapService;
import uk.gov.hmcts.reform.sscs.service.VenueService;

@Data
@Component
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
