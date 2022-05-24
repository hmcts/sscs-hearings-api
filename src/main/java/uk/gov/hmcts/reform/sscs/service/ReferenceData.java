package uk.gov.hmcts.reform.sscs.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@Component
public class ReferenceData {
    private HearingDurationsService hearingDurations;
    private SessionCategoryMapService sessionCategoryMaps;
}
