package uk.gov.hmcts.reform.sscs.helper.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import uk.gov.hmcts.reform.sscs.ccd.domain.Hearing;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus;
import uk.gov.hmcts.reform.sscs.model.multi.hearing.CaseHearing;
import uk.gov.hmcts.reform.sscs.model.multi.hearing.HearingsGetResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HmcUpdateResponse;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.sscs.helper.service.HearingsServiceHelper.getHearingId;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.CANCELLED;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.HEARING_REQUESTED;

class HearingsServiceHelperTest {

    private static final long HEARING_REQUEST_ID = 12345;

    private HearingWrapper wrapper;

    @BeforeEach
    void setup() {
        wrapper = HearingWrapper.builder()
                .caseData(SscsCaseData.builder().build())
                .build();
    }


    @DisplayName("updateHearingId Parameterised Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "1,2,2",
        "1,1,1",
        "2,1,1",
        "null,2,2",
        "1,null,1",
        "null,null,null",
    }, nullValues = {"null"})
    void updateHearingId(String original, Long updated, String expected) {
        Hearing hearing = Hearing.builder()
            .value(HearingDetails.builder()
                .hearingId(original)
                .build())
            .build();
        HmcUpdateResponse response = HmcUpdateResponse.builder()
                .hearingRequestId(updated)
                .build();

        HearingsServiceHelper.updateHearingId(hearing, response);

        assertThat(hearing.getValue().getHearingId()).isEqualTo(expected);
    }

    @DisplayName("updateVersionNumber Parameterised Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "1,2,2",
        "1,1,1",
        "2,1,1",
        "null,2,2",
        "1,null,null",
        "null,null,null",
    }, nullValues = {"null"})
    void updateVersionNumber(Long original, Long updated, Long expected) {
        Hearing hearing = Hearing.builder()
            .value(HearingDetails.builder()
                .versionNumber(original)
                .build())
            .build();
        HmcUpdateResponse response = HmcUpdateResponse.builder()
                .versionNumber(updated)
                .build();

        HearingsServiceHelper.updateVersionNumber(hearing, response);

        assertThat(hearing.getValue().getVersionNumber()).isEqualTo(expected);
    }

    @Test
    void shouldReturnHearingId_givenValidWrapper() {
        wrapper.getCaseData().setHearings(Collections.singletonList(Hearing.builder()
            .value(HearingDetails.builder()
                .hearingId("12345")
                .build())
            .build()));

        final String actualHearingId = getHearingId(wrapper);

        assertThat(actualHearingId, is("12345"));
    }

    @Test
    void shouldReturnNullHearingId_givenNullValue() {
        wrapper.getCaseData().setHearings(Collections.singletonList(Hearing.builder()
            .value(HearingDetails.builder()
                .hearingId(null)
                .build())
            .build()));

        final String actualHearingId = getHearingId(wrapper);

        assertNull(actualHearingId);
    }

    @DisplayName("getVersion Test")
    @Test
    void getVersion() {
        wrapper.getCaseData().setHearings(Collections.singletonList(Hearing.builder()
            .value(HearingDetails.builder()
                .versionNumber(1L)
                .build())
            .build()));
        Long result = HearingsServiceHelper.getVersion(wrapper);

        assertEquals(1L, result);
    }

    @DisplayName("getVersion null return ParameterisedTest Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "null",
        "0",
        "-1",
    }, nullValues = {"null"})
    void getVersion(Long version) {
        wrapper.getCaseData().setHearings(Collections.singletonList(Hearing.builder()
            .value(HearingDetails.builder()
                .versionNumber(version)
                .build())
            .build()));

        Long result = HearingsServiceHelper.getVersion(wrapper);

        assertNull(result);
    }

    @DisplayName("getVersion when hearings is null ParameterisedTest Tests")
    @Test
    void getVersionNull() {

        Long result = HearingsServiceHelper.getVersion(wrapper);

        assertNull(result);
    }

    @DisplayName("When a response with valid and invalid hearings is given getCurrentCaseHearing returns the latest valid hearing")
    @Test
    void testGetCurrentCaseHearing() {
        HearingsGetResponse hearingsGetResponse = HearingsGetResponse.builder()
            .caseHearings(List.of(
                CaseHearing.builder()
                    .hearingId(4545L)
                    .hmcStatus(CANCELLED)
                    .requestVersion(3L)
                    .hearingRequestDateTime(LocalDateTime.of(2020,1,1,10,0))
                    .build(),
                CaseHearing.builder()
                    .hearingId(6545L)
                    .hmcStatus(HEARING_REQUESTED)
                    .requestVersion(2L)
                    .hearingRequestDateTime(LocalDateTime.of(2022,12,1,10,0))
                    .build(),
                CaseHearing.builder()
                    .hearingId(HEARING_REQUEST_ID)
                    .hmcStatus(HEARING_REQUESTED)
                    .requestVersion(1L)
                    .hearingRequestDateTime(LocalDateTime.of(2022,1,1,10,0))
                    .build()))
            .build();

        CaseHearing result = HearingsServiceHelper.getCurrentCaseHearing(hearingsGetResponse);

        assertThat(result)
            .isNotNull()
            .extracting("hearingId","hmcStatus","requestVersion")
            .contains(HEARING_REQUEST_ID,HEARING_REQUESTED,1L);
    }

    @DisplayName("When a response with only invalid hearings is given getCurrentCaseHearing returns null")
    @Test
    void testGetCurrentCaseHearingInvalid() {
        HearingsGetResponse hearingsGetResponse = HearingsGetResponse.builder()
            .caseHearings(List.of(
                CaseHearing.builder()
                    .hearingId(4545L)
                    .hmcStatus(CANCELLED)
                    .requestVersion(3L)
                    .hearingRequestDateTime(LocalDateTime.of(2020,1,1,10,0))
                    .build()))
            .build();

        CaseHearing result = HearingsServiceHelper.getCurrentCaseHearing(hearingsGetResponse);

        assertThat(result).isNull();
    }

    @DisplayName("When a null response given getCurrentCaseHearing returns null")
    @Test
    void testGetCurrentCaseHearingNull() {
        HearingsGetResponse hearingsGetResponse = HearingsGetResponse.builder().build();

        CaseHearing result = HearingsServiceHelper.getCurrentCaseHearing(hearingsGetResponse);

        assertThat(result).isNull();
    }


    @DisplayName("When the status matches isCaseHearingRequestedOrAwaitingListing returns true")
    @ParameterizedTest
    @EnumSource(
        value = HmcStatus.class,
        mode = EnumSource.Mode.INCLUDE,
        names = {"HEARING_REQUESTED", "AWAITING_LISTING"})
    void testIsCaseHearingRequestedOrAwaitingListing(HmcStatus value) {
        boolean result = HearingsServiceHelper.isCaseHearingRequestedOrAwaitingListing(value);

        assertThat(result).isTrue();
    }

    @DisplayName("When the status doesnt match isCaseHearingRequestedOrAwaitingListing returns false")
    @ParameterizedTest
    @EnumSource(
        value = HmcStatus.class,
        mode = EnumSource.Mode.EXCLUDE,
        names = {"HEARING_REQUESTED", "AWAITING_LISTING"})
    @NullSource
    void testIsCaseHearingRequestedOrAwaitingListingNonMatching(HmcStatus value) {
        boolean result = HearingsServiceHelper.isCaseHearingRequestedOrAwaitingListing(value);

        assertThat(result).isFalse();
    }
}
