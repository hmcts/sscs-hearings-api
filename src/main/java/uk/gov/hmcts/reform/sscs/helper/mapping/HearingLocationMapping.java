package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingLocations;
import uk.gov.hmcts.reform.sscs.service.AirLookupService;
import uk.gov.hmcts.reform.sscs.service.ReferenceData;

import java.util.List;
import java.util.Optional;

public final class HearingLocationMapping {

    private HearingLocationMapping() {

    }

    public static List<HearingLocations> getHearingLocations(SscsCaseData caseData,
                                                             ReferenceData referenceData) {

        AirLookupService airLookupService = referenceData.getAirLookupService();
        String venueId = String.valueOf(airLookupService.getLookupVenueIdByAirVenueName()
            .get(caseData.getProcessingVenue()));

        VenueDetails venueDetails = referenceData.getVenueDataLoader().getVenueDetailsMap()
            .get(String.valueOf(venueId));

        String epimsId = Optional.ofNullable(venueDetails)
            .map(VenueDetails::getEpimsId)
            .orElse(null);

        HearingLocations hearingLocation = new HearingLocations();
        hearingLocation.setLocationId(epimsId);
        hearingLocation.setLocationType("court");

        return List.of(hearingLocation);
    }

}
