package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.helper.mappingutils.GetVenueMultipleEpims;
import uk.gov.hmcts.reform.sscs.model.HearingLocation;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.LocationType;
import uk.gov.hmcts.reform.sscs.service.AirLookupService;
import uk.gov.hmcts.reform.sscs.service.ReferenceData;

import java.util.ArrayList;
import java.util.List;

public final class HearingLocationMapping {

    private HearingLocationMapping() {

    }

    public static List<HearingLocation> getHearingLocations(SscsCaseData caseData,
                                                            ReferenceData referenceData) {
        String epimsId = null;

        AirLookupService airLookupService = referenceData.getAirLookupService();
        String venueId = String.valueOf(airLookupService.getLookupVenueIdByAirVenueName()
                                            .get(caseData.getProcessingVenue()));
        VenueDetails venueDetails = referenceData.getVenueDataLoader().getVenueDetailsMap()
            .get(String.valueOf(venueId));

        List<HearingLocation> locations = new ArrayList<>();

        if (venueDetails != null) {
            epimsId = venueDetails.getEpimsId();
            locations = GetVenueMultipleEpims.getMultipleLocationDetails(venueDetails, caseData);
        }

        HearingLocation hearingLocations = new HearingLocation();
        List<HearingLocation> hearingLocationsList = new ArrayList<>();

        if (locations.isEmpty()) {
            hearingLocations.setLocationType(LocationType.COURT);
            hearingLocations.setLocationId(epimsId);
            hearingLocationsList.add(hearingLocations);
        } else {
            hearingLocationsList.addAll(locations);
        }
        return hearingLocationsList;
    }
}
