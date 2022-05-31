package uk.gov.hmcts.reform.sscs.helper.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.exception.InvalidHearingDataException;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;
import uk.gov.hmcts.reform.sscs.model.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.sscs.service.VenueDataLoader;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CaseHearingLocationHelperTest {

    private static final String EPIMS_ID = "123";
    private static final String HEARING_ID = "789";
    private static final String CASE_ID = "777";
    private static final String NEW_EPIMS_ID = "456";

    @Mock
    private VenueDataLoader venueData;


    @DisplayName("When updateVenue is given caseData with a hearing ID that cannot be found,"
            + "updateVenue throws UpdateCaseException with the correct message")
    @Test
    void testUpdateVenueMissingHearing() {
        SscsCaseData caseData = SscsCaseData.builder()
                .hearings(List.of())
                .build();

        HmcMessage hmcMessage = HmcMessage.builder()
                .hearingId(HEARING_ID)
                .build();

        assertThatExceptionOfType(InvalidHearingDataException.class)
                .isThrownBy(() -> CaseHearingLocationHelper.getHearingFromCaseData(hmcMessage, caseData))
                .withMessageContaining("Unable to find hearing in case data with Id " + HEARING_ID);
    }


    @DisplayName("When casedata with multiple hearings, updateHearing replaces the correct hearing")
    @Test
    void testUpdateHearing() {
        Hearing oldHearing = Hearing.builder()
                .value(HearingDetails.builder()
                        .venueId(EPIMS_ID)
                        .hearingId(HEARING_ID)
                        .build())
                .build();

        Hearing secondHearing = Hearing.builder()
                .value(HearingDetails.builder()
                        .venueId("23453")
                        .hearingId("35533")
                        .build())
                .build();

        SscsCaseData caseData = SscsCaseData.builder()
                .hearings(List.of(
                        oldHearing,
                        secondHearing))
                .ccdCaseId(CASE_ID)
                .build();

        Hearing updatedHearing = Hearing.builder()
                .value(HearingDetails.builder()
                        .venueId(NEW_EPIMS_ID)
                        .hearingId(HEARING_ID)
                        .build())
                .build();

        CaseHearingLocationHelper.updateHearing(caseData, oldHearing, updatedHearing);

        // then
        assertThat(caseData.getHearings())
                .hasSize(2)
                .containsOnly(updatedHearing, secondHearing);
    }

    @DisplayName("When an valid epims id is given to findVenue, it returns a venue")
    @Test
    void testShouldFindVenueByVenueId() throws InvalidMappingException {
        // given
        VenueDetails venueDetails = VenueDetails.builder().build();

        given(venueData.getAnActiveVenueByEpims(EPIMS_ID)).willReturn(venueDetails);

        // when
        Venue venue = CaseHearingLocationHelper.findVenue(EPIMS_ID, venueData);

        // then
        assertThat(venue).isNotNull();
    }

    @DisplayName("When an invalid epims id is given to findVenue, it throws an error with the correct message ")
    @Test
    void testShouldReturnNullIfVenueDoesNotExist() {
        given(venueData.getAnActiveVenueByEpims(anyString())).willReturn(null);

        assertThatExceptionOfType(InvalidMappingException.class)
                .isThrownBy(() -> CaseHearingLocationHelper.findVenue(EPIMS_ID, venueData))
                .withMessageContaining("Invalid epims Id " + EPIMS_ID);
    }

    @DisplayName("When an valid venue is given to findVenue, mapVenueDetailsToVenue correctly maps those venue details to venue")
    @Test
    void testMapVenueDetailsToVenue() {

        final String epimsId = "123";
        VenueDetails venueDetails = VenueDetails.builder()
                .venueId(epimsId)
                .venName("venueName")
                .url("http://test.com")
                .venAddressLine1("adrLine1")
                .venAddressLine2("adrLine2")
                .venAddressTown("adrTown")
                .venAddressCounty("adrCounty")
                .venAddressPostcode("adrPostcode")
                .regionalProcessingCentre("regionalProcessingCentre")
                .build();


        Venue venue = CaseHearingLocationHelper.mapVenueDetailsToVenue(venueDetails);

        // then
        assertThat(venue).isNotNull();
        assertThat(venue.getName()).isEqualTo(venueDetails.getVenName());
        assertThat(venue.getGoogleMapLink()).isEqualTo(venueDetails.getUrl());

        Address expectedAddress = Address.builder()
                .line1(venueDetails.getVenAddressLine1())
                .line2(venueDetails.getVenAddressLine2())
                .town(venueDetails.getVenAddressTown())
                .county(venueDetails.getVenAddressCounty())
                .postcode(venueDetails.getVenAddressPostcode())
                .postcodeLookup(venueDetails.getVenAddressPostcode())
                .postcodeAddress(venueDetails.getVenAddressPostcode())
                .build();

        assertThat(venue.getAddress()).isEqualTo(expectedAddress);
    }
}
