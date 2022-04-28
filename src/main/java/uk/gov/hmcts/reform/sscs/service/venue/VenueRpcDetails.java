package uk.gov.hmcts.reform.sscs.service.venue;

import lombok.Getter;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;

@Getter
public class VenueRpcDetails {

    private final VenueDetails venueDetails;

    // rpc = RegionalProcessingCentre
    private final String rpc;

    public VenueRpcDetails(VenueDetails venueDetails) {
        this.venueDetails = venueDetails;
        this.rpc = venueDetails.getRegionalProcessingCentre().substring(5);
    }

    public String getEpimsId() {
        return venueDetails.getEpimsId();
    }
}
