package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingLocations;
import uk.gov.hmcts.reform.sscs.service.AirLookupService;
import uk.gov.hmcts.reform.sscs.service.ReferenceData;

import java.util.ArrayList;
import java.util.List;

public final class HearingLocationMapping {

    private HearingLocationMapping() {

    }

    public static List<HearingLocations> getHearingLocations(SscsCaseData caseData,
                                                             ReferenceData referenceData) {
        String epimsId = null;

        AirLookupService airLookupService = referenceData.getAirLookupService();
        String venueId = String.valueOf(airLookupService.getLookupVenueIdByAirVenueName()
            .get(caseData.getProcessingVenue()));

        VenueDetails venueDetails = referenceData.getVenueDataLoader().getVenueDetailsMap()
            .get(String.valueOf(venueId));

        if (venueDetails != null) {
            epimsId = venueDetails.getEpimsId();
        }

        HearingLocations hearingLocations = new HearingLocations();
        hearingLocations.setLocationId(epimsId);
        hearingLocations.setLocationType("court");

        List<HearingLocations> hearingLocationsList = new ArrayList<>();
        hearingLocationsList.add(hearingLocations);

        return hearingLocationsList;
    }

}
