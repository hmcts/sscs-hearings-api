package uk.gov.hmcts.reform.sscs.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.service.HearingDurationsService;
import uk.gov.hmcts.reform.sscs.service.SessionCategoryMapService;

@Data
@AllArgsConstructor
@Component
public class ReferenceData {
    private HearingDurationsService hearingDurations;
    private SessionCategoryMapService sessionCategoryMaps;
}
