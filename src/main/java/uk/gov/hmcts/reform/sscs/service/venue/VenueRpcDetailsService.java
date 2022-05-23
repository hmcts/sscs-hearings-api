package uk.gov.hmcts.reform.sscs.service.venue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;
import uk.gov.hmcts.reform.sscs.model.venue.VenueRpcDetails;
import uk.gov.hmcts.reform.sscs.service.VenueDataLoader;

import java.util.Objects;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;

@Service
@Slf4j
@RequiredArgsConstructor
public class VenueRpcDetailsService {

    private final VenueDataLoader venueDataLoader;

    public VenueDetails getVenue(String epimsId) {
        return venueDataLoader.getVenueDetailsMap()
                .values().stream()
                .filter(Objects::nonNull)
                .filter(v -> v.getEpimsId().equalsIgnoreCase(epimsId))
                .filter(this::isActiveVenue)
                .findAny()
                .orElse(null);
    }

    public VenueRpcDetails getVenueRpcDetails(String epimsId) {
        VenueDetails venueDetails = getVenue(epimsId);
        if (isNull(venueDetails)) {
            return null;
        }
        return new VenueRpcDetails(venueDetails);
    }

    public boolean isActiveVenue(VenueDetails venueDetails) {
        return nonNull(venueDetails)
                && isYes(venueDetails.getActive());
    }
}
