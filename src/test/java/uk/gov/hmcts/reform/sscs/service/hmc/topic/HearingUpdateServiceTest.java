package uk.gov.hmcts.reform.sscs.service.hmc.topic;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RequestDetails;
import uk.gov.hmcts.reform.sscs.service.VenueService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HearingUpdateServiceTest {

    public static final LocalDateTime HEARING_START_DATE_TIME = LocalDateTime.of(2022, 10, 1, 11, 0, 0);
    public static final LocalDateTime HEARING_END_DATE_TIME = LocalDateTime.of(2022, 10, 1, 13, 0, 0);
    private static final String HEARING_ID = "789";
    private static final String EPIMS_ID = "123";
    private static final String NEW_EPIMS_ID = "456";
    private static final String CASE_ID = "777";
    private static final String VENUE_NAME = "VenueName";

    private VenueDetails venueDetails;
    private SscsCaseData caseData;
    private HearingGetResponse hearingGetResponse;

    @Mock
    private VenueService venueService;

    @InjectMocks
    private HearingUpdateService hearingUpdateService;

    @BeforeEach
    void setUp() {

        hearingGetResponse = HearingGetResponse.builder()
                .requestDetails(RequestDetails.builder().build())
                .hearingResponse(HearingResponse.builder()
                        .hearingRequestId(Long.valueOf(HEARING_ID))
                        .build())
                .build();

        caseData = SscsCaseData.builder()
                .ccdCaseId(CASE_ID)
                .build();

        venueDetails = VenueDetails.builder()
                .venueId(NEW_EPIMS_ID)
                .venName(VENUE_NAME)
                .regionalProcessingCentre("regionalProcessingCentre")
                .build();
    }

    @DisplayName("When caseData with a hearing that matches one from hearingGetResponse is given,"
            + "updateHearing updates the correct hearing")
    @Test
    void testUpdateHearing() throws Exception {
        hearingGetResponse.getHearingResponse().setHearingSessions(List.of(
                        HearingDaySchedule.builder()
                                .hearingStartDateTime(HEARING_START_DATE_TIME)
                                .hearingEndDateTime(HEARING_END_DATE_TIME)
                                .hearingVenueEpimsId(NEW_EPIMS_ID)
                                .build()));

        caseData.setHearings(Lists.newArrayList(
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
                        .build()));


        when(venueService.getVenueDetailsForActiveVenueByEpimsId(NEW_EPIMS_ID)).thenReturn(venueDetails);

        // when
        hearingUpdateService.updateHearing(hearingGetResponse, caseData);

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

    @DisplayName("When caseData with no hearing that matches one from hearingGetResponse is given,"
            + "updateHearing adds the correct hearing")
    @Test
    void testUpdateHearingNewHearing() throws Exception {
        hearingGetResponse.getHearingResponse().setHearingSessions(List.of(
                HearingDaySchedule.builder()
                        .hearingStartDateTime(HEARING_START_DATE_TIME)
                        .hearingEndDateTime(HEARING_END_DATE_TIME)
                        .hearingVenueEpimsId(NEW_EPIMS_ID)
                        .build()));

        caseData.setHearings(Lists.newArrayList(
                Hearing.builder()
                        .value(HearingDetails.builder()
                                .venueId("23453")
                                .hearingId("35533")
                                .build())
                        .build()));


        when(venueService.getVenueDetailsForActiveVenueByEpimsId(NEW_EPIMS_ID)).thenReturn(venueDetails);

        // when
        hearingUpdateService.updateHearing(hearingGetResponse, caseData);

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

    @DisplayName("When hearingGetResponse with multiple Hearing Sessions are given,"
            + "updateHearing throws the correct error and message")
    @Test
    void testUpdateHearingMultipleHearingSessions() {
        hearingGetResponse.getHearingResponse().setHearingSessions(List.of(
                HearingDaySchedule.builder().build(),
                HearingDaySchedule.builder().build()));

        assertThatExceptionOfType(InvalidHearingDataException.class)
                .isThrownBy(() -> hearingUpdateService.updateHearing(hearingGetResponse, caseData))
                .withMessageContaining("Invalid HearingDaySchedule, should have 1 session but instead has 2 sessions");
    }

    @DisplayName("When a invalid Epims ID is given, "
            + "updateHearing throws the correct error and message")
    @Test
    void testUpdateHearingVenueNull() {
        hearingGetResponse.getHearingResponse().setHearingSessions(List.of(
                HearingDaySchedule.builder()
                        .hearingStartDateTime(HEARING_START_DATE_TIME)
                        .hearingEndDateTime(HEARING_END_DATE_TIME)
                        .hearingVenueEpimsId(NEW_EPIMS_ID)
                        .build()));

        caseData.setHearings(Lists.newArrayList(
                Hearing.builder()
                        .value(HearingDetails.builder()
                                .venueId("23453")
                                .hearingId("35533")
                                .build())
                        .build()));


        when(venueService.getVenueDetailsForActiveVenueByEpimsId(NEW_EPIMS_ID)).thenReturn(null);

        assertThatExceptionOfType(InvalidMappingException.class)
                .isThrownBy(() -> hearingUpdateService.updateHearing(hearingGetResponse, caseData))
                .withMessageContaining("Invalid epims Id %s, unable to find active venue with that id, regarding Case Id %s", NEW_EPIMS_ID, CASE_ID);
    }
}
