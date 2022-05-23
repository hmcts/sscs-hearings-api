package uk.gov.hmcts.reform.sscs.service.ccdupdate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.ccd.domain.Address;
import uk.gov.hmcts.reform.sscs.ccd.domain.Hearing;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.Venue;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;
import uk.gov.hmcts.reform.sscs.model.messaging.HearingUpdate;
import uk.gov.hmcts.reform.sscs.model.messaging.HmcMessage;
import uk.gov.hmcts.reform.sscs.service.venue.VenueRpcDetails;
import uk.gov.hmcts.reform.sscs.service.venue.VenueRpcDetailsService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CcdLocationUpdateServiceTest {

    @Mock
    private VenueRpcDetailsService venueRpcDetailsService;
    private CcdLocationUpdateService underTest;

    @BeforeEach
    void setUp() {
        underTest = new CcdLocationUpdateService(venueRpcDetailsService);
    }

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
        when(venueRpcDetailsService.getVenue(any())).thenReturn(Optional.of(venueRpcDetails));

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
        when(venueRpcDetailsService.getVenue(any())).thenReturn(Optional.empty());

        // when
        Venue venue = underTest.findVenue(venueId);

        // then
        assertThat(venue).isNull();
    }


    @Test
    void shouldUpdateVenueSuccessfully() throws UpdateCaseException {
        // given
        final String hearingId = "789";
        final String oldVenueId = "123";
        final String newVenueId = "456";
        final String caseId = "777";

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
        SscsCaseData caseData = SscsCaseData.builder().hearings(hearingListWithOldVenue).ccdCaseId(caseId).build();

        VenueDetails venueDetails = VenueDetails.builder()
            .venueId(newVenueId)
            .regionalProcessingCentre("regionalProcessingCentre")
            .build();
        VenueRpcDetails venueRpcDetails = new VenueRpcDetails(venueDetails);
        when(venueRpcDetailsService.getVenue(any())).thenReturn(Optional.of(venueRpcDetails));

        // when
        underTest.updateVenue(hmcMessage, caseData);

        // then
        List<Hearing> hearings = caseData.getHearings();
        assertThat(hearings).isNotNull();
        assertThat(hearings.get(0).getValue().getVenueId()).isEqualTo(newVenueId);
        assertThat(hearings.get(0).getValue().getVenue()).isNotNull();
    }
}
