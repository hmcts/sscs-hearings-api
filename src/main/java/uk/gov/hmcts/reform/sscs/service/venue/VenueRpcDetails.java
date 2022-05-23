package uk.gov.hmcts.reform.sscs.service.venue;

import lombok.Getter;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;

@Getter
public class VenueRpcDetails {

    private final VenueDetails venueDetails;

    private final String regionalProcessingCentre;

    public VenueRpcDetails(VenueDetails venueDetails) {
        this.venueDetails = venueDetails;
        this.regionalProcessingCentre = venueDetails.getRegionalProcessingCentre().substring(5);
    }

    public String getEpimsId() {
        return venueDetails.getEpimsId();
    }
}
