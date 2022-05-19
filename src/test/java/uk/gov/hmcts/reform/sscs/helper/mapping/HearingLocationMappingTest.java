package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingLocations;
import uk.gov.hmcts.reform.sscs.service.AirLookupService;
import uk.gov.hmcts.reform.sscs.service.ReferenceData;
import uk.gov.hmcts.reform.sscs.service.VenueDataLoader;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HearingLocationMappingTest {

    public static final String PROCESSING_VENUE_1 = "test_place";
    public static final String PROCESSING_VENUE_2 = "test_other_place";

    @Mock
    private ReferenceData referenceData;

    @Mock
    private AirLookupService airLookupService;

    @Mock
    private VenueDataLoader venueDataLoader;

    @Test
    void getHearingLocations_shouldReturnCorrespondingEpimsIdForVenue() {

        setupVenueMaps();

        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder().build())
                .build())
            .processingVenue(PROCESSING_VENUE_1)
            .build();

        List<HearingLocations> result = HearingLocationMapping.getHearingLocations(caseData, referenceData);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLocationId()).isEqualTo("9876");
        assertThat(result.get(0).getLocationType()).isEqualTo("court");
    }

    private void setupVenueMaps() {
        Map<String, Integer> venueIdMap = Map.of(PROCESSING_VENUE_1,
            68, PROCESSING_VENUE_2, 2);


        Map<String, VenueDetails> venueDetailsMap = Map.of(
            "68", VenueDetails.builder()
                .epimsId("9876")
                .build(),
            "2", VenueDetails.builder()
                .epimsId("1111")
                .build());

        when(referenceData.getAirLookupService()).thenReturn(airLookupService);
        when(referenceData.getVenueDataLoader()).thenReturn(venueDataLoader);

        when(airLookupService.getLookupVenueIdByAirVenueName()).thenReturn(venueIdMap);
        when(venueDataLoader.getVenueDetailsMap()).thenReturn(venueDetailsMap);
    }

}
