package uk.gov.hmcts.reform.sscs.service.ccdupdate;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.State;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RequestDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CcdStateUpdateServiceTest {

    private final CcdStateUpdateService underTest = new CcdStateUpdateService();

    @ParameterizedTest
    @CsvSource({
        "Fixed,Listed,hearing",
        "Fixed,Awaiting Listing,readyToList",
        "Fixed,Case Closed,unknown",
        "Draft,Listed,unknown"
    })
    void shouldSetCcdStateForListedHearingsCorrectly(String listingStatus, String laCaseStatus, String stateId) {
        // given
        HearingGetResponse hearingGetResponse = new HearingGetResponse();
        HearingResponse hearingResponse = new HearingResponse();
        hearingResponse.setListingStatus(listingStatus);
        hearingResponse.setListingCaseStatus(laCaseStatus);
        hearingGetResponse.setHearingResponse(hearingResponse);
        SscsCaseData caseData = SscsCaseData.builder().state(State.UNKNOWN).ccdCaseId("123").build();

        // when
        underTest.updateListed(hearingGetResponse, caseData);

        // then
        assertThat(caseData.getState()).isEqualTo(State.getById(stateId));
    }

    @ParameterizedTest
    @CsvSource({
        "Withdrawn,dormantAppealState",
        "Struck Out,dormantAppealState",
        "Lapsed,dormantAppealState",
        "Party unable to attend,readyToList",
        "Exclusion,readyToList",
        "Incomplete Tribunal,readyToList",
        "Listed In Error,readyToList",
        "Other,readyToList",
        "Party Did Not Attend,readyToList"

    })
    void shouldSetCcdStateForCancelledHearingsCorrectly(String cancellationReason, String stateId) throws UpdateCaseException {
        // given
        HearingGetResponse hearingGetResponse = new HearingGetResponse();
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setStatus("Cancelled");
        hearingGetResponse.setRequestDetails(requestDetails);
        HearingResponse hearingResponse = new HearingResponse();
        hearingResponse.setHearingCancellationReason(cancellationReason);
        hearingGetResponse.setHearingResponse(hearingResponse);
        SscsCaseData caseData = SscsCaseData.builder().state(State.UNKNOWN).ccdCaseId("123").build();

        // when
        underTest.updateCancelled(hearingGetResponse, caseData);

        // then
        assertThat(caseData.getState()).isEqualTo(State.getById(stateId));
    }


    @ParameterizedTest
    @CsvSource({
        "UNKNOWN,unknown"

    })
    void shouldThrowExceptionWhenCancellationReasonNotCorrectly(String cancellationReason, String stateId)  {
        // given
        HearingGetResponse hearingGetResponse = new HearingGetResponse();
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setStatus("Cancelled");
        hearingGetResponse.setRequestDetails(requestDetails);
        HearingResponse hearingResponse = new HearingResponse();
        hearingResponse.setHearingCancellationReason(cancellationReason);
        hearingGetResponse.setHearingResponse(hearingResponse);
        SscsCaseData caseData = SscsCaseData.builder().state(State.UNKNOWN).ccdCaseId("123").build();



        // then
        UpdateCaseException updateCaseException = assertThrows(
            UpdateCaseException.class,
            () -> underTest.updateCancelled(hearingGetResponse, caseData)
        );

        AssertionsForClassTypes.assertThat(updateCaseException.getMessage()).isEqualTo("Can not map cancellation reason with null value for caseId 123");


    }



    @Test
    void shouldSetCcdStateForFailedHearingsCorrectly() {
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
