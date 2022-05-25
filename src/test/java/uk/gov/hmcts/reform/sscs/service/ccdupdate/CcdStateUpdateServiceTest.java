package uk.gov.hmcts.reform.sscs.service.ccdupdate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.State;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RequestDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.sscs.ccd.domain.State.DORMANT_APPEAL_STATE;
import static uk.gov.hmcts.reform.sscs.ccd.domain.State.UNKNOWN;

@ExtendWith(MockitoExtension.class)
class CcdStateUpdateServiceTest {

    @InjectMocks
    private CcdStateUpdateService underTest;

    @DisplayName("When valid listing Status and list assist case status is given, "
            + "updateCancelled updates the case data correctly")
    @ParameterizedTest
    @CsvSource({
        "Fixed,Listed,HEARING",
        "Fixed,Awaiting Listing,READY_TO_LIST",
    })
    void testUpdateListed(String listingStatus, String laCaseStatus, State expected) throws UpdateCaseException {
        // given
        HearingGetResponse hearingGetResponse = HearingGetResponse.builder()
                .hearingResponse(HearingResponse.builder()
                        .listingStatus(listingStatus)
                        .listingCaseStatus(laCaseStatus)
                        .build())
                .build();

        SscsCaseData caseData = SscsCaseData.builder()
                .state(UNKNOWN)
                .ccdCaseId("123")
                .build();

        // when
        underTest.updateListed(hearingGetResponse, caseData);

        // then
        assertThat(caseData.getState()).isEqualTo(expected);
    }

    @DisplayName("When listing Status is not Fixed, updateCancelled does not update the case state")
    @ParameterizedTest
    @CsvSource({
        "Draft,Listed",
    })
    void testUpdateListed(String listingStatus, String laCaseStatus) throws UpdateCaseException {
        // given
        HearingGetResponse hearingGetResponse = HearingGetResponse.builder()
                .hearingResponse(HearingResponse.builder()
                        .listingStatus(listingStatus)
                        .listingCaseStatus(laCaseStatus)
                        .build())
                .build();

        SscsCaseData caseData = SscsCaseData.builder()
                .state(UNKNOWN)
                .ccdCaseId("123")
                .build();

        // when
        underTest.updateListed(hearingGetResponse, caseData);

        // then
        assertThat(caseData.getState()).isEqualTo(UNKNOWN);


    }

    @DisplayName("When an invalid listing Status  is given, updateCancelled throws the correct error and message")
    @ParameterizedTest
    @CsvSource({
        "Fixed,Case Closed",
    })
    void testUpdateListedInvalidListingStatus(String listingStatus, String laCaseStatus) throws UpdateCaseException {
        // given
        HearingGetResponse hearingGetResponse = HearingGetResponse.builder()
                .hearingResponse(HearingResponse.builder()
                        .listingStatus(listingStatus)
                        .listingCaseStatus(laCaseStatus)
                        .build())
                .build();

        SscsCaseData caseData = SscsCaseData.builder()
                .state(UNKNOWN)
                .ccdCaseId("123")
                .build();

        assertThatExceptionOfType(UpdateCaseException.class)
                .isThrownBy(() -> underTest.updateListed(hearingGetResponse, caseData))
                .withMessageContaining("Can not map HMC updated or create listing status");
    }



    @ParameterizedTest
    @CsvSource({
        "Withdrawn,DORMANT_APPEAL_STATE",
        "Struck Out,DORMANT_APPEAL_STATE",
        "Lapsed,DORMANT_APPEAL_STATE",
        "Party unable to attend,READY_TO_LIST",
        "Exclusion,READY_TO_LIST",
        "Incomplete Tribunal,READY_TO_LIST",
        "Listed In Error,READY_TO_LIST",
        "Other,READY_TO_LIST",
        "Party Did Not Attend,READY_TO_LIST",
    })
    void testShouldSetCcdStateForCancelledHearingsCorrectly(String cancellationReason, State expected) throws UpdateCaseException {
        // given
        HearingGetResponse hearingGetResponse = HearingGetResponse.builder()
                .requestDetails(RequestDetails.builder()
                        .status("Cancelled")
                        .build())
                .hearingResponse(HearingResponse.builder()
                        .hearingCancellationReason(cancellationReason)
                        .build())
                .build();

        SscsCaseData caseData = SscsCaseData.builder()
                .state(UNKNOWN)
                .ccdCaseId("123")
                .build();

        // when
        underTest.updateCancelled(hearingGetResponse, caseData);

        // then
        assertThat(caseData.getState()).isEqualTo(expected);
    }

    @DisplayName("When non Cancelled status given in but hearingCancellationReason is valid, "
            + "updateCancelled doesn't change the case state")
    @Test
    void testUpdateCancelledNonCancelledStatusRequest() throws UpdateCaseException {
        HearingGetResponse hearingGetResponse = HearingGetResponse.builder()
                .requestDetails(RequestDetails.builder()
                        .status("test")
                        .build())
                .hearingResponse(HearingResponse.builder()
                        .hearingCancellationReason("Withdrawn")
                        .build())
                .build();

        SscsCaseData caseData = SscsCaseData.builder()
                .state(UNKNOWN)
                .ccdCaseId("123")
                .build();

        // when
        underTest.updateCancelled(hearingGetResponse, caseData);

        // then
        assertThat(caseData.getState()).isEqualTo(DORMANT_APPEAL_STATE);
    }

    @DisplayName("When no cancellation reason is given but status is Cancelled, "
            + "updateCancelled doesn't change the case state")
    @Test
    void testUpdateCancelledNullReason() throws UpdateCaseException {
        HearingGetResponse hearingGetResponse = HearingGetResponse.builder()
                .requestDetails(RequestDetails.builder()
                        .status("Cancelled")
                        .build())
                .hearingResponse(HearingResponse.builder().build())
                .build();

        SscsCaseData caseData = SscsCaseData.builder()
                .state(UNKNOWN)
                .ccdCaseId("123")
                .build();

        assertThatExceptionOfType(UpdateCaseException.class)
                .isThrownBy(() -> underTest.updateCancelled(hearingGetResponse, caseData))
                .withMessageContaining("Can not map cancellation reason null");
    }


    @DisplayName("When invalid status given in request and no cancellation reason is given, "
            + "updateCancelled doesn't change the case state")
    @ParameterizedTest
    @ValueSource(strings = {"test"})
    @NullAndEmptySource
    void testUpdateCancelledInvalidStatusNullReason(String status) throws UpdateCaseException {
        HearingGetResponse hearingGetResponse = HearingGetResponse.builder()
                .requestDetails(RequestDetails.builder()
                        .status(status)
                        .build())
                .hearingResponse(HearingResponse.builder().build())
                .build();

        SscsCaseData caseData = SscsCaseData.builder()
                .state(UNKNOWN)
                .ccdCaseId("123")
                .build();

        // when
        underTest.updateCancelled(hearingGetResponse, caseData);

        // then
        assertThat(caseData.getState()).isEqualTo(UNKNOWN);
    }

    @DisplayName("When an invalid cancellation reason or status is given and a known case state is given, "
            + "updateCancelled throws an UpdateCaseException with the correct message")
    @Test
    void testUpdateCancelledUnknownReason() {
        HearingGetResponse hearingGetResponse = HearingGetResponse.builder()
                .requestDetails(RequestDetails.builder().build())
                .hearingResponse(HearingResponse.builder()
                        .hearingCancellationReason("test")
                        .build())
                .build();

        SscsCaseData caseData = SscsCaseData.builder()
                .state(State.READY_TO_LIST)
                .ccdCaseId("123")
                .build();

        assertThatExceptionOfType(UpdateCaseException.class)
                .isThrownBy(() -> underTest.updateCancelled(hearingGetResponse, caseData))
                .withMessageContaining("Can not map cancellation reason test");
    }

    @Test
    void testShouldSetCcdStateForFailedHearingsCorrectly() {
        // given
        SscsCaseData caseData = SscsCaseData.builder()
            .state(UNKNOWN)
            .ccdCaseId("123")
            .build();

        // when
        underTest.updateFailed(caseData);

        // then
        assertThat(caseData.getState()).isEqualTo(State.HANDLING_ERROR);
    }
}
