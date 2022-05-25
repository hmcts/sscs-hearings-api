package uk.gov.hmcts.reform.sscs.model.venue;

import lombok.Getter;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;

@Getter
public class VenueRpcDetails {

    public static final int SSCS_PREFIX_END_INDEX = 5;

    private final VenueDetails venueDetails;

    private final String regionalProcessingCentre;

    public VenueRpcDetails(VenueDetails venueDetails) {
        this.venueDetails = venueDetails;
        this.regionalProcessingCentre = venueDetails.getRegionalProcessingCentre().substring(SSCS_PREFIX_END_INDEX);
    }

    public String getEpimsId() {
        return venueDetails.getEpimsId();
    }
}
