package uk.gov.hmcts.reform.sscs.service;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ReferenceData {

    private VenueDataLoader venueDataLoader;

    private AirLookupService airLookupService;

}
