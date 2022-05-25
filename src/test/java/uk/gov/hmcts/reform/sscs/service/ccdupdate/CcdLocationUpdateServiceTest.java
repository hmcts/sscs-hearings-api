package uk.gov.hmcts.reform.sscs.service.ccdupdate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
import uk.gov.hmcts.reform.sscs.service.venue.VenueRpcDetailsService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CcdLocationUpdateServiceTest {

    public static final String HEARING_ID = "789";
    public static final String EPIMS_ID = "123";
    public static final String NEW_EPIMS_ID = "456";
    public static final String CASE_ID = "777";
    public static final String VENUE_NAME = "VenueName";

    @Mock
    private VenueRpcDetailsService venueRpcDetailsService;

    @InjectMocks
    private CcdLocationUpdateService underTest;

    @Test
    void testShouldFindVenueByVenueId() {
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

        when(venueRpcDetailsService.getVenue(any())).thenReturn(venueDetails);

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
    void testShouldReturnNullIfVenueDoesNotExist() {
        // given
        when(venueRpcDetailsService.getVenue(any())).thenReturn(null);

        // when
        Venue venue = underTest.findVenue(EPIMS_ID);

        // then
        assertThat(venue).isNull();
    }

    @Test
    void testShouldUpdateVenueSuccessfully() throws UpdateCaseException {
        // given

        HmcMessage hmcMessage = HmcMessage.builder()
                .hearingID(HEARING_ID)
                .hearingUpdate(HearingUpdate.builder()
                        .hearingVenueID(NEW_EPIMS_ID)
                        .build())
                .build();

        SscsCaseData caseData = SscsCaseData.builder()
                .hearings(List.of(
                        Hearing.builder()
                                .value(HearingDetails.builder()
                                        .venueId(EPIMS_ID)
                                        .hearingId(HEARING_ID)
                                        .build())
                                .build(),
                        Hearing.builder()
                                .value(HearingDetails.builder()
                                        .venueId("23453")
                                        .hearingId("35533")
                                        .build())
                                .build()))
                .ccdCaseId(CASE_ID)
                .build();

        VenueDetails venueDetails = VenueDetails.builder()
                .venueId(NEW_EPIMS_ID)
                .venName(VENUE_NAME)
                .regionalProcessingCentre("regionalProcessingCentre")
                .build();

        when(venueRpcDetailsService.getVenue(any())).thenReturn(venueDetails);

        // when
        underTest.updateVenue(hmcMessage, caseData);

        // then
        List<Hearing> hearings = caseData.getHearings();
        assertThat(hearings)
                .hasSize(2)
                .extracting(Hearing::getValue)
                .filteredOn("hearingId", HEARING_ID)
                .hasSize(1)
                .allSatisfy(x -> assertThat(x.getVenueId()).isEqualTo(NEW_EPIMS_ID))
                .extracting(HearingDetails::getVenue)
                .extracting("name")
                .containsOnly(VENUE_NAME);
    }

    @DisplayName("When updateVenue is given caseData with a hearing ID that cannot be found,"
            + "updateVenue throws UpdateCaseException with the correct message")
    @Test
    void testUpdateVenueMissingHearing() {
        SscsCaseData caseData = SscsCaseData.builder()
                .hearings(List.of())
                .build();

        assertThatExceptionOfType(UpdateCaseException.class)
                .isThrownBy(() -> underTest.updateVenue(HmcMessage.builder().build(), caseData))
                .withMessageContaining("Could not find hearing");
    }

    @DisplayName("When updateVenue is given caseData with a epims ID that cannot be found,"
            + "updateVenue throws UpdateCaseException with the correct message")
    @Test
    void testUpdateVenueMissingVenue() {
        HmcMessage hmcMessage = HmcMessage.builder()
                .hearingID(HEARING_ID)
                .hearingUpdate(HearingUpdate.builder()
                        .hearingVenueID(NEW_EPIMS_ID)
                        .build())
                .build();

        SscsCaseData caseData = SscsCaseData.builder()
                .hearings(List.of(Hearing.builder()
                        .value(HearingDetails.builder()
                                .venueId(EPIMS_ID)
                                .hearingId(HEARING_ID)
                                .build())
                        .build()))
                .ccdCaseId(CASE_ID)
                .build();

        when(venueRpcDetailsService.getVenue(any())).thenReturn(null);

        assertThatExceptionOfType(UpdateCaseException.class)
                .isThrownBy(() -> underTest.updateVenue(hmcMessage, caseData))
                .withMessageContaining("Could not find venue");
    }

}
