package uk.gov.hmcts.reform.sscs.service.hmc.topic;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.ccd.domain.Hearing;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.exception.InvalidHearingDataException;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;
import uk.gov.hmcts.reform.sscs.model.hmc.message.HearingUpdate;
import uk.gov.hmcts.reform.sscs.model.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.sscs.service.VenueDataLoader;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseHearingLocationServiceTest {

    private static final String HEARING_ID = "789";
    private static final String EPIMS_ID = "123";
    private static final String NEW_EPIMS_ID = "456";
    private static final String CASE_ID = "777";
    private static final String VENUE_NAME = "VenueName";

    @Mock
    private VenueDataLoader venueDataLoader;

    @InjectMocks
    private CaseHearingLocationService caseHearingLocationService;

    @Test
    void testShouldUpdateVenueSuccessfully() throws InvalidMappingException, InvalidHearingDataException {
        HmcMessage hmcMessage = HmcMessage.builder()
                .hearingId(HEARING_ID)
                .hearingUpdate(HearingUpdate.builder()
                        .hearingVenueId(NEW_EPIMS_ID)
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

        when(venueDataLoader.getAnActiveVenueByEpims(NEW_EPIMS_ID)).thenReturn(venueDetails);

        // when
        caseHearingLocationService.updateVenue(hmcMessage, caseData);

        // then
        List<Hearing> hearings = caseData.getHearings();
        assertThat(hearings)
                .hasSize(2)
                .extracting(Hearing::getValue)
                .filteredOn("hearingId", HEARING_ID)
                .hasSize(1)
                .allSatisfy(hearing -> assertThat(hearing.getVenueId()).isEqualTo(NEW_EPIMS_ID))
                .extracting(HearingDetails::getVenue)
                .extracting("name")
                .containsOnly(VENUE_NAME);
    }
}
