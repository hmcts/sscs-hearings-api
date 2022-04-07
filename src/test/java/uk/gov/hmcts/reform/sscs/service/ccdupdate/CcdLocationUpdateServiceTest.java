package uk.gov.hmcts.reform.sscs.service.ccdupdate;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.Address;
import uk.gov.hmcts.reform.sscs.ccd.domain.Hearing;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.Venue;
import uk.gov.hmcts.reform.sscs.messaging.HearingUpdate;
import uk.gov.hmcts.reform.sscs.messaging.HmcMessage;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;
import uk.gov.hmcts.reform.sscs.service.venue.VenueRpcDetails;
import uk.gov.hmcts.reform.sscs.service.venue.VenueRpcDetailsService;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CcdLocationUpdateServiceTest {

    private final VenueRpcDetailsService venueRpcDetailsService = mock(VenueRpcDetailsService.class);
    private final CcdLocationUpdateService underTest = new CcdLocationUpdateService(venueRpcDetailsService);

    @Test
    void shouldFindVenueByVenueId() {
        // given
        final String venueId = "123";
        VenueDetails venueDetails = VenueDetails.builder()
            .venueId(venueId)
            .venAddressLine1("adrLine1")
            .venAddressLine2("adrLine2")
            .venAddressTown("adrTown")
            .venAddressCounty("adrCounty")
            .venAddressPostcode("adrPostcode")
            .regionalProcessingCentre("regionalProcessingCentre")
            .build();
        VenueRpcDetails venueRpcDetails = new VenueRpcDetails(venueDetails);
        List<VenueRpcDetails> venueRpcDetailsList = List.of(venueRpcDetails);
        when(venueRpcDetailsService.getVenues(any())).thenReturn(venueRpcDetailsList);

        // when
        Venue venue = underTest.findVenue(venueId);

        // then
        assertThat(venue).isNotNull();

        Address address = venue.getAddress();
        assertThat(address.getCounty()).isEqualTo(venueDetails.getVenAddressCounty());
        assertThat(address.getLine1()).isEqualTo(venueDetails.getVenAddressLine1());
        assertThat(address.getLine2()).isEqualTo(venueDetails.getVenAddressLine2());
        assertThat(address.getTown()).isEqualTo(venueDetails.getVenAddressTown());
        assertThat(address.getPostcodeAddress()).isEqualTo(venueDetails.getVenAddressPostcode());
    }


    @Test
    void shouldReturnNullIfVenueDoesNotExist() {
        // given
        final String venueId = "987";
        when(venueRpcDetailsService.getVenues(any())).thenReturn(Collections.emptyList());

        // when
        Venue venue = underTest.findVenue(venueId);

        // then
        assertThat(venue).isNull();
    }


    @Test
    void shouldUpdateVenueSuccessfully() {
        // given
        final String hearingId = "789";
        final String oldVenueId = "123";
        final String newVenueId = "456";

        HmcMessage hmcMessage = HmcMessage.builder()
            .hearingID(hearingId)
            .hearingUpdate(HearingUpdate.builder()
                               .hearingVenueID(newVenueId)
                               .build())
            .build();

        HearingDetails hearingDetailsWithOldVenue = HearingDetails.builder()
            .venueId(oldVenueId)
            .hearingId(hearingId)
            .build();
        Hearing hearingWithOldVenue = Hearing.builder().value(hearingDetailsWithOldVenue).build();
        List<Hearing> hearingListWithOldVenue = List.of(hearingWithOldVenue);
        SscsCaseData caseData = SscsCaseData.builder().hearings(hearingListWithOldVenue).build();

        VenueDetails venueDetails = VenueDetails.builder()
            .venueId(newVenueId)
            .regionalProcessingCentre("regionalProcessingCentre")
            .build();
        VenueRpcDetails venueRpcDetails = new VenueRpcDetails(venueDetails);
        List<VenueRpcDetails> venueRpcDetailsList = List.of(venueRpcDetails);
        when(venueRpcDetailsService.getVenues(any())).thenReturn(venueRpcDetailsList);

        // when
        underTest.updateVenue(hmcMessage, caseData);

        // then
        List<Hearing> hearings = caseData.getHearings();
        assertThat(hearings).isNotNull();
        assertThat(hearings.get(0).getValue().getVenueId()).isEqualTo(newVenueId);
        assertThat(hearings.get(0).getValue().getVenue()).isNotNull();
    }
}
