package uk.gov.hmcts.reform.sscs.service.venue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;
import uk.gov.hmcts.reform.sscs.service.VenueDataLoader;

import java.util.Optional;

@Service
@Slf4j
public class VenueRpcDetailsService {

    private final VenueDataLoader venueDataLoader;

    @Autowired
    public VenueRpcDetailsService(VenueDataLoader venueDataLoader) {
        this.venueDataLoader = venueDataLoader;
    }

    public Optional<VenueRpcDetails> getVenue(String epimsId) {
        return venueDataLoader.getVenueDetailsMap().values().stream().filter(this::isActiveVenue)
            .map(VenueRpcDetails::new).filter(v -> v.getEpimsId().equalsIgnoreCase(epimsId)).findAny();
    }

    private boolean isActiveVenue(VenueDetails venueDetails) {
        return venueDetails != null && "Yes".equalsIgnoreCase(venueDetails.getActive());
    }
}
