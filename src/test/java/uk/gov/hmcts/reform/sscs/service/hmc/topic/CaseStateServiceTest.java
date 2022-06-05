package uk.gov.hmcts.reform.sscs.service.hmc.topic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.exception.InvalidHmcMessageException;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.ListAssistCaseStatus;
import uk.gov.hmcts.reform.sscs.model.single.hearing.CaseDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RequestDetails;
import uk.gov.hmcts.reform.sscs.reference.data.model.CancellationReason;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.willDoNothing;
import static uk.gov.hmcts.reform.sscs.ccd.domain.State.HANDLING_ERROR;
import static uk.gov.hmcts.reform.sscs.ccd.domain.State.UNKNOWN;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.CANCELLED;


@ExtendWith(MockitoExtension.class)
class CaseStateServiceTest {

    @Mock
    private HearingUpdateService hearingUpdateService;

    @InjectMocks
    private CaseStateUpdateService caseStateUpdateService;

    private SscsCaseData caseData;
    private HearingGetResponse hearingGetResponse;

    @BeforeEach
    void setUp() {
        caseData = SscsCaseData.builder()
                .state(UNKNOWN)
                .ccdCaseId("123")
                .build();

        hearingGetResponse = HearingGetResponse.builder()
                .requestDetails(RequestDetails.builder().build())
                .hearingDetails(HearingDetails.builder().build())
                .caseDetails(CaseDetails.builder().build())
                .partyDetails(new ArrayList<>())
                .hearingResponse(HearingResponse.builder().build())
                .build();
    }

    @DisplayName("When valid listing Status and list assist case status is given, "
            + "updateCancelled updates the case data correctly")
    @ParameterizedTest
    @EnumSource(
            value = ListAssistCaseStatus.class,
            mode = EnumSource.Mode.INCLUDE,
            names = {"LISTED", "AWAITING_LISTING"})
    void testUpdateListed(ListAssistCaseStatus listAssistCaseStatus) throws Exception {
        // given
        hearingGetResponse.getHearingResponse().setListAssistCaseStatus(listAssistCaseStatus);

        willDoNothing().given(hearingUpdateService).updateHearing(hearingGetResponse, caseData);

        // when

        caseStateUpdateService.updateListed(hearingGetResponse, caseData);

        // then
        assertThat(caseData.getState()).isEqualTo(listAssistCaseStatus.getCaseStateUpdate());
    }

    @DisplayName("When an invalid listing Status is given, updateListed throws the correct error and message")
    @ParameterizedTest
    @EnumSource(
        value = ListAssistCaseStatus.class,
        mode = EnumSource.Mode.EXCLUDE,
        names = {"LISTED", "AWAITING_LISTING"})
    void testUpdateListedInvalidListingStatus(ListAssistCaseStatus listAssistCaseStatus) {
        hearingGetResponse.getHearingResponse().setListAssistCaseStatus(listAssistCaseStatus);

        assertThatExceptionOfType(InvalidHmcMessageException.class)
                .isThrownBy(() -> caseStateUpdateService.updateListed(hearingGetResponse, caseData))
                .withMessageContaining("Can not map listing Case Status %s for Case ID", listAssistCaseStatus.toString());
    }

    @DisplayName("When an null listing Status is given, updateListed throws the correct error and message")
    @Test
    void testUpdateListedNullListingStatus() {
        hearingGetResponse.getHearingResponse().setListAssistCaseStatus(null);

        assertThatExceptionOfType(InvalidHmcMessageException.class)
                .isThrownBy(() -> caseStateUpdateService.updateListed(hearingGetResponse, caseData))
                .withMessageContaining("Can not map listing Case Status null for Case ID");
    }

    @DisplayName("When the cancellation reason is valid, updateCancelled updates the case state correctly")
    @ParameterizedTest
    @EnumSource(value = CancellationReason.class)
    void testUpdateCancelled(CancellationReason value)
            throws InvalidHmcMessageException {
        // given
        hearingGetResponse.getHearingResponse().setHearingCancellationReason(value);
        hearingGetResponse.getRequestDetails().setStatus(CANCELLED);

        // when
        caseStateUpdateService.updateCancelled(hearingGetResponse, caseData);

        // then
        assertThat(caseData.getState()).isEqualTo(value.getCaseStateUpdate());
    }

    @DisplayName("When a invalid cancellation reason is given, updateCancelled throws the correct error and message")
    @Test
    void testUpdateCancelledInvalidReason() {
        hearingGetResponse.getHearingResponse().setHearingCancellationReason(null);

        assertThatExceptionOfType(InvalidHmcMessageException.class)
                .isThrownBy(() -> caseStateUpdateService.updateCancelled(hearingGetResponse, caseData))
                .withMessageContaining("Can not map cancellation reason null");
    }

    @DisplayName("When updateFailed is called it should return caseData with the correct state")
    @Test
    void testShouldSetCcdStateForFailedHearingsCorrectly() {
        caseStateUpdateService.updateFailed(caseData);

        assertThat(caseData.getState()).isEqualTo(HANDLING_ERROR);
    }
}
