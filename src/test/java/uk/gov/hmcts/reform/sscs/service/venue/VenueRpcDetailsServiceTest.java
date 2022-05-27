package uk.gov.hmcts.reform.sscs.service.venue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;
import uk.gov.hmcts.reform.sscs.model.VenueDetails.VenueDetailsBuilder;
import uk.gov.hmcts.reform.sscs.model.venue.VenueRpcDetails;
import uk.gov.hmcts.reform.sscs.service.VenueDataLoader;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class VenueRpcDetailsServiceTest {

    public static final String VALID_EPIMS_ID = "560788";
    public static final String INVALID_EPIMS_ID = "abcdef";
    public static final String VENUE_ID = "60";

    @Mock
    private VenueDataLoader venueDataLoader;

    @InjectMocks
    private VenueRpcDetailsService venueRpcDetailsService;

    private VenueDetailsBuilder venueDetailsBuilder;
    private Map<String, VenueDetails> venueDetailsMap;

    @BeforeEach
    void setUp() {
        venueDetailsBuilder = VenueDetails.builder()
                .venueId(VENUE_ID)
                .threeDigitReference("SC944")
                .regionalProcessingCentre("SSCS Liverpool")
                .venName("Stockport Law Courts")
                .venAddressLine1("The Court House")
                .venAddressLine2("Edward Street")
                .venAddressTown("Stockport")
                .venAddressCounty("Cheshire")
                .venAddressPostcode("SK1 3DQ")
                .venAddressTelNo("")
                .districtId("601")
                .url("https://www.google.co.uk/maps/etc")
                .active("Yes")
                .gapsVenName("Stockport")
                .comments("Comments")
                .epimsId(VALID_EPIMS_ID);

        venueDetailsMap = Map.of(VENUE_ID, venueDetailsBuilder.venueId(VENUE_ID).build());
    }

    @DisplayName("When a valid epims ID is searched, getVenue return the correct Venue Rpc Details")
    @Test
    void testGetVenue() {
        given(venueDataLoader.getVenueDetailsMap()).willReturn(venueDetailsMap);

        VenueDetails result = venueRpcDetailsService.getVenue(VALID_EPIMS_ID);

        assertThat(result)
                .isNotNull()
                .isEqualTo(venueDetailsBuilder.build());
    }

    @DisplayName("When a invalid epims ID is searched, getVenue returns null")
    @Test
    void testGetVenueInvalidEpims() {

        given(venueDataLoader.getVenueDetailsMap()).willReturn(venueDetailsMap);

        VenueRpcDetails result = venueRpcDetailsService.getVenueRpcDetails(INVALID_EPIMS_ID);

        assertThat(result).isNull();
    }

    @DisplayName("When a valid epims ID is searched, getVenue return the correct Venue Rpc Details")
    @Test
    void testVenueRpcDetails() {
        venueDetailsMap = Map.of(VENUE_ID, venueDetailsBuilder.venueId(VENUE_ID).build());
        given(venueDataLoader.getVenueDetailsMap()).willReturn(venueDetailsMap);

        VenueRpcDetails result = venueRpcDetailsService.getVenueRpcDetails(VALID_EPIMS_ID);

        assertThat(result).isNotNull();
        assertThat(result.getVenueDetails()).isEqualTo(venueDetailsBuilder.build());
        assertThat(result.getRegionalProcessingCentre()).isEqualTo("Liverpool");
        assertThat(result.getEpimsId()).isEqualTo(VALID_EPIMS_ID);
    }

    @DisplayName("When a invalid epims ID is searched, getVenue returns null")
    @Test
    void testGetVenueRpcDetailsInvalidEpims() {
        given(venueDataLoader.getVenueDetailsMap()).willReturn(venueDetailsMap);

        VenueRpcDetails result = venueRpcDetailsService.getVenueRpcDetails(INVALID_EPIMS_ID);

        assertThat(result).isNull();
    }

    @DisplayName("When a Venue with the field Active is Yes is given, isActiveVenue returns true")
    @Test
    void testIsActiveVenue() {
        VenueDetails venueDetails = venueDetailsBuilder.active("Yes").build();
        boolean result = venueRpcDetailsService.isActiveVenue(venueDetails);

        assertThat(result).isTrue();
    }

    @DisplayName("When a Venue with the field Active is not Yes is given, isActiveVenue returns false")
    @ParameterizedTest
    @ValueSource(strings = {"No"})
    @NullAndEmptySource
    void testIsActiveVenue(String isActive) {
        VenueDetails venueDetails = venueDetailsBuilder.active(isActive).build();
        boolean result = venueRpcDetailsService.isActiveVenue(venueDetails);

        assertThat(result).isFalse();
    }

    @DisplayName("When a null Venue is given, isActiveVenue returns false")
    @Test
    void testIsActiveVenueNull() {
        boolean result = venueRpcDetailsService.isActiveVenue(null);

        assertThat(result).isFalse();
    }
}
