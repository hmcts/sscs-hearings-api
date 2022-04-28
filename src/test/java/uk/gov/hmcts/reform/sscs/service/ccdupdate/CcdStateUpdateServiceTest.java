package uk.gov.hmcts.reform.sscs.service.ccdupdate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.State;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RequestDetails;

import static org.assertj.core.api.Assertions.assertThat;

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
        "struckOut,dormantAppealState",
        "Lapsed,dormantAppealState",
        "partyUnableToAttend,readyToList",
        "Exclusion,readyToList",
        "incompleteTribunal,readyToList",
        "listedInError,readyToList",
        "Other,readyToList",
        "partyDidNotAttend,readyToList",
        "somethingElse,unknown"
    })
    void shouldSetCcdStateForCancelledHearingsCorrectly(String cancellationReason, String stateId) {
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


    @Test
    void shouldSetCcdStateForFailedHearingsCorrectly() {
        // given
        SscsCaseData caseData = SscsCaseData.builder()
            .state(State.UNKNOWN).ccdCaseId("123").build();

        // when
        underTest.updateFailed(caseData);

        // then
        assertThat(caseData.getState()).isEqualTo(State.HANDLING_ERROR);
    }
}
