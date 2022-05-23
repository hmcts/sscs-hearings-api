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
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HearingLocationMappingTest {

    private static final List<String> VENUE_NAMES_TEST = List.of("test_place", "test_other_place");
    private static final List<Integer> VENUE_TEST_IDS = List.of(68, 2);
    private static final List<String> VENUE_TEST_EPIM_ID = List.of("9876", "1111");
    private static final List<String> VENUE_NAMES = List.of("Chester", "Manchester");
    private static final List<Integer> VENUE_IDS = List.of(65, 200);
    private static final List<String> VENUEPIM_ID = List.of("9876", "701411");

    @Mock
    private ReferenceData referenceData;

    @Mock
    private AirLookupService airLookupService;

    @Mock
    private VenueDataLoader venueDataLoader;

    @Test
    void getHearingLocations_shouldReturnCorrespondingEpimsIdForVenue() {

        multipleVenueMaps(VENUE_NAMES_TEST, VENUE_TEST_IDS, VENUE_TEST_EPIM_ID);

        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder().build())
                .build())
            .processingVenue(VENUE_NAMES_TEST.get(0))
            .build();

        List<HearingLocations> result = HearingLocationMapping.getHearingLocations(caseData, referenceData);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLocationId()).isEqualTo("9876");
        assertThat(result.get(0).getLocationType()).isEqualTo("court");
    }

    @Test
    void getMultipleHearingLocations() {
        multipleVenueMaps(VENUE_NAMES, VENUE_IDS, VENUEPIM_ID);
        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder().build())
                        .build())
            .processingVenue(VENUE_NAMES.get(0))
            .build();

        final String courtLocation = "court";

        List<HearingLocations> result = HearingLocationMapping.getHearingLocations(caseData, referenceData);

        if (!courtLocation.equalsIgnoreCase(result.get(0).getLocationType())) {
            List<String> epimIdList = result.get(0).getMultipleLocationId();
            String epimId1 = epimIdList.get(0);
            String epimId2 = epimIdList.get(1);

            assertThat(epimId1).isEqualTo("226511");
            assertThat(epimId2).isEqualTo("443014");
            assertThat(result.get(0).getLocationType()).isEqualTo(VENUE_NAMES.get(0));
        }
    }

    private void multipleVenueMaps(List<String> venueName, List<Integer> venueId, List<String> venueEpimId) {
        ConcurrentHashMap<String, Integer> venueIdMap = new ConcurrentHashMap<>();
        venueIdMap.put(venueName.get(0), venueId.get(0));

        ConcurrentHashMap<String, VenueDetails> venueDetailsMap = new ConcurrentHashMap<>();
        venueDetailsMap.put(String.valueOf(venueId.get(0)), VenueDetails.builder()
            .epimsId(venueEpimId.get(0))
            .build());
        venueDetailsMap.put(String.valueOf(venueId.get(1)), VenueDetails.builder()
            .epimsId(venueEpimId.get(0))
            .build());

        when(referenceData.getAirLookupService()).thenReturn(airLookupService);
        when(referenceData.getVenueDataLoader()).thenReturn(venueDataLoader);

        when(airLookupService.getLookupVenueIdByAirVenueName()).thenReturn(venueIdMap);
        when(venueDataLoader.getVenueDetailsMap()).thenReturn(venueDetailsMap);
    }

}
