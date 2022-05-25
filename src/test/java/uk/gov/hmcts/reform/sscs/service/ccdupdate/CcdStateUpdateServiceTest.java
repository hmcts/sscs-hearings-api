package uk.gov.hmcts.reform.sscs.service.ccdupdate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

@ExtendWith(MockitoExtension.class)
class CcdStateUpdateServiceTest {

    @InjectMocks
    private CcdStateUpdateService underTest;

    @ParameterizedTest
    @CsvSource({
        "Fixed,Listed,hearing",
        "Fixed,Awaiting Listing,readyToList",
        "Fixed,Case Closed,unknown",
        "Draft,Listed,unknown",
    })
    void testShouldSetCcdStateForListedHearingsCorrectly(String listingStatus, String laCaseStatus, String stateId) {
        // given
        HearingGetResponse hearingGetResponse = HearingGetResponse.builder()
                .hearingResponse(HearingResponse.builder()
                        .listingStatus(listingStatus)
                        .listingCaseStatus(laCaseStatus)
                        .build())
                .build();

        SscsCaseData caseData = SscsCaseData.builder()
                .state(State.UNKNOWN)
                .ccdCaseId("123")
                .build();

        // when
        underTest.updateListed(hearingGetResponse, caseData);

        // then
        assertThat(caseData.getState()).isEqualTo(State.getById(stateId));
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
                .state(State.UNKNOWN)
                .ccdCaseId("123")
                .build();

        // when
        underTest.updateCancelled(hearingGetResponse, caseData);

        // then
        assertThat(caseData.getState()).isEqualTo(expected);
    }

    @DisplayName("When no cancellation reason is given, "
            + "updateCancelled throws an UpdateCaseException with the correct message")
    @Test
    void testUpdateCancelledNullReason() {
        HearingGetResponse hearingGetResponse = HearingGetResponse.builder()
                .requestDetails(RequestDetails.builder()
                        .status("Cancelled")
                        .build())
                .hearingResponse(HearingResponse.builder().build())
                .build();

        SscsCaseData caseData = SscsCaseData.builder()
                .state(State.UNKNOWN)
                .ccdCaseId("123")
                .build();

        assertThatExceptionOfType(UpdateCaseException.class)
                .isThrownBy(() -> underTest.updateCancelled(hearingGetResponse, caseData))
                .withMessageContaining("Can not map cancellation reason null");
    }

    @Test
    void testShouldThrowExceptionWhenCancellationReasonNotCorrectly()  {
        // given
        HearingGetResponse hearingGetResponse = HearingGetResponse.builder()
                .requestDetails(RequestDetails.builder()
                        .status("unknown")
                        .build())
                .hearingResponse(HearingResponse.builder()
                        .hearingCancellationReason("UNKNOWN")
                        .build())
                .build();

        SscsCaseData caseData = SscsCaseData.builder()
                .state(State.UNKNOWN)
                .ccdCaseId("123")
                .build();

        // then
        assertThatExceptionOfType(UpdateCaseException.class)
                .isThrownBy(() -> underTest.updateCancelled(hearingGetResponse, caseData))
                .withMessageContaining("Can not map cancellation reason UNKNOWN");
    }

    @DisplayName("When no cancellation reason or status is given, "
            + "updateCancelled throws an UpdateCaseException with the correct message")
    @Test
    void testUpdateCancelledNullStatusReason() throws UpdateCaseException {
        HearingGetResponse hearingGetResponse = HearingGetResponse.builder()
                .requestDetails(RequestDetails.builder().build())
                .hearingResponse(HearingResponse.builder().build())
                .build();

        SscsCaseData caseData = SscsCaseData.builder()
                .state(State.UNKNOWN)
                .ccdCaseId("123")
                .build();

        // when
        underTest.updateCancelled(hearingGetResponse, caseData);

        // then
        assertThat(caseData.getState()).isEqualTo(State.UNKNOWN);
    }

    @DisplayName("When no cancellation reason or status is given and a known case state is given, "
            + "updateCancelled does not change the case's state")
    @Test
    void testUpdateCancelledKnownState() throws UpdateCaseException {
        HearingGetResponse hearingGetResponse = HearingGetResponse.builder()
                .requestDetails(RequestDetails.builder().build())
                .hearingResponse(HearingResponse.builder().build())
                .build();

        SscsCaseData caseData = SscsCaseData.builder()
                .state(State.READY_TO_LIST)
                .ccdCaseId("123")
                .build();

        // when
        underTest.updateCancelled(hearingGetResponse, caseData);

        // then
        assertThat(caseData.getState()).isEqualTo(State.READY_TO_LIST);
    }



    @Test
    void testShouldSetCcdStateForFailedHearingsCorrectly() {
        // given
        SscsCaseData caseData = SscsCaseData.builder()
            .state(State.UNKNOWN)
            .ccdCaseId("123")
            .build();

        // when
        underTest.updateFailed(caseData);

        // then
        assertThat(caseData.getState()).isEqualTo(State.HANDLING_ERROR);
    }
}
