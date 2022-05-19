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

import java.util.HashMap;
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

    @Test
    void multipleHearingLocations() {
        setupMultipleVenueMaps();
        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder().build())
                        .build())
            .processingVenue("Plymouth")
            .build();

        final String courtLocation = "court";

        List<HearingLocations> result = HearingLocationMapping.getHearingLocations(caseData, referenceData);

        if (!courtLocation.equalsIgnoreCase(result.get(0).getLocationType())) {
            List<String> epimIdList = result.get(0).getMultipleLocationId();
            String epimId1 = epimIdList.get(0);
            String epimId2 = epimIdList.get(1);

            assertThat(epimId1).isEqualTo("764728");
            assertThat(epimId2).isEqualTo("235590");
            assertThat(result.get(0).getLocationType()).isEqualTo("Plymouth");
        }
    }

    private void setupMultipleVenueMaps() {
        Map<String, Integer> venueIdMap = new HashMap<>();
        venueIdMap.put("Plymouth", 200);

        Map<String, VenueDetails> venueDetailsMap = new HashMap<>();
        venueDetailsMap.put("200", VenueDetails.builder()
            .epimsId("764728")
            .build());

        when(referenceData.getAirLookupService()).thenReturn(airLookupService);
        when(referenceData.getVenueDataLoader()).thenReturn(venueDataLoader);

        when(airLookupService.getLookupVenueIdByAirVenueName()).thenReturn(venueIdMap);
        when(venueDataLoader.getVenueDetailsMap()).thenReturn(venueDetailsMap);
    }

    private void setupVenueMaps() {
        Map<String, Integer> venueIdMap = new HashMap<>();
        venueIdMap.put(PROCESSING_VENUE_1, 68);
        venueIdMap.put(PROCESSING_VENUE_2, 2);

        Map<String, VenueDetails> venueDetailsMap = new HashMap<>();
        venueDetailsMap.put("68", VenueDetails.builder()
            .epimsId("9876")
            .build());
        venueDetailsMap.put("2", VenueDetails.builder()
            .epimsId("1111")
            .build());

        when(referenceData.getAirLookupService()).thenReturn(airLookupService);
        when(referenceData.getVenueDataLoader()).thenReturn(venueDataLoader);

        when(airLookupService.getLookupVenueIdByAirVenueName()).thenReturn(venueIdMap);
        when(venueDataLoader.getVenueDetailsMap()).thenReturn(venueDetailsMap);
    }

}
