package uk.gov.hmcts.reform.sscs.service.holder;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.reference.data.service.HearingDurationsService;
import uk.gov.hmcts.reform.sscs.reference.data.service.SessionCategoryMapService;
import uk.gov.hmcts.reform.sscs.reference.data.service.SignLanguagesService;
import uk.gov.hmcts.reform.sscs.reference.data.service.VerbalLanguagesService;
import uk.gov.hmcts.reform.sscs.service.VenueService;

@Data
@Component
public class ReferenceDataServiceHolder {

    @Autowired
    private final HearingDurationsService hearingDurations;
    
    @Autowired
    private final SessionCategoryMapService sessionCategoryMaps;

    @Autowired
    private final VerbalLanguagesService verbalLanguages;

    @Autowired
    private final SignLanguagesService signLanguages;

    @Autowired
    private final VenueService venueService;

    @Value("${exui.url}")
    private String exUiUrl;

    @Value("${sscs.serviceCode}")
    private String sscsServiceCode;

}
