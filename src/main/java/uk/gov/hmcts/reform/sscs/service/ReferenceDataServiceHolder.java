package uk.gov.hmcts.reform.sscs.service;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ReferenceDataServiceHolder {
    private VenueService venueService;
}
