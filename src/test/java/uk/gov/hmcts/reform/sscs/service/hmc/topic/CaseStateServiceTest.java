package uk.gov.hmcts.reform.sscs.service.hmc.topic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.State;
import uk.gov.hmcts.reform.sscs.exception.InvalidHearingDataException;
import uk.gov.hmcts.reform.sscs.exception.InvalidHmcMessageException;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.model.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.ListAssistCaseStatus;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RequestDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.willDoNothing;
import static uk.gov.hmcts.reform.sscs.ccd.domain.State.HANDLING_ERROR;
import static uk.gov.hmcts.reform.sscs.ccd.domain.State.UNKNOWN;

@ExtendWith(MockitoExtension.class)
class CaseStateServiceTest {

    @Mock
    private CaseHearingLocationService caseHearingLocationService;

    @InjectMocks
    private CaseStateUpdateService caseStateUpdateService;

    private SscsCaseData caseData;
    private HearingGetResponse hearingGetResponse;
    private HmcMessage hmcMessage;

    @BeforeEach
    void setUp() {
        caseData = SscsCaseData.builder()
                .state(UNKNOWN)
                .ccdCaseId("123")
                .build();

        hearingGetResponse = HearingGetResponse.builder()
                .hearingResponse(HearingResponse.builder().build())
                .requestDetails(RequestDetails.builder().build())
                .build();

        hmcMessage = HmcMessage.builder().build();
    }

    @DisplayName("When valid listing Status and list assist case status is given, "
            + "updateCancelled updates the case data correctly")
    @ParameterizedTest
    @CsvSource({
        "LISTED,HEARING",
        "AWAITING_LISTING,READY_TO_LIST",
    })
    void testUpdateListed(ListAssistCaseStatus listAssistCaseStatus, State expected)
            throws UpdateCaseException, InvalidHmcMessageException, InvalidMappingException, InvalidHearingDataException {
        // given
        hearingGetResponse.getHearingResponse().setListAssistCaseStatus(listAssistCaseStatus);

        willDoNothing().given(caseHearingLocationService).updateVenue(hmcMessage, caseData);

        // when

        caseStateUpdateService.updateListed(hearingGetResponse, hmcMessage, caseData);

        // then
        assertThat(caseData.getState()).isEqualTo(expected);
    }

    @DisplayName("When an invalid listing Status is given, updateListed throws the correct error and message")
    @ParameterizedTest
    @EnumSource(
        value = ListAssistCaseStatus.class,
        mode = EnumSource.Mode.EXCLUDE,
        names = {"LISTED", "AWAITING_LISTING"})
    void testUpdateListedInvalidListingStatus(ListAssistCaseStatus listAssistCaseStatus) {
        hearingGetResponse.getHearingResponse().setListAssistCaseStatus(listAssistCaseStatus);

        HmcMessage hmcMessage = HmcMessage.builder().build();

        assertThatExceptionOfType(InvalidHmcMessageException.class)
                .isThrownBy(() -> caseStateUpdateService.updateListed(hearingGetResponse, hmcMessage, caseData))
                .withMessageContaining("Can not map HMC updated or create listing status " + listAssistCaseStatus.toString());
    }

    @DisplayName("When an null listing Status is given, updateListed throws the correct error and message")
    @Test
    void testUpdateListedNullListingStatus() {
        hearingGetResponse.getHearingResponse().setListAssistCaseStatus(null);

        assertThatExceptionOfType(InvalidHmcMessageException.class)
                .isThrownBy(() -> caseStateUpdateService.updateListed(hearingGetResponse, hmcMessage, caseData))
                .withMessageContaining("Can not map listing Case Status null for Case ID");
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
    void testShouldSetCcdStateForCancelledHearingsCorrectly(String cancellationReason, State expected)
            throws InvalidHmcMessageException {
        // given
        hearingGetResponse.getHearingResponse().setHearingCancellationReason(cancellationReason);
        hearingGetResponse.getRequestDetails().setStatus("Cancelled");

        // when
        caseStateUpdateService.updateCancelled(hearingGetResponse, caseData);

        // then
        assertThat(caseData.getState()).isEqualTo(expected);
    }

    @DisplayName("When a invalid cancellation reason is given, updateCancelled throws the correct error and message")
    @ParameterizedTest
    @ValueSource(strings = {"test"})
    @NullAndEmptySource
    void testUpdateCancelledInvalidReason(String value) {
        hearingGetResponse.getHearingResponse().setHearingCancellationReason(value);

        assertThatExceptionOfType(InvalidHmcMessageException.class)
                .isThrownBy(() -> caseStateUpdateService.updateCancelled(hearingGetResponse, caseData))
                .withMessageContaining("Can not map cancellation reason label " + value);
    }

    @DisplayName("When updateFailed is called it should return caseData with the correct state")
    @Test
    void testShouldSetCcdStateForFailedHearingsCorrectly() {
        caseStateUpdateService.updateFailed(caseData);

        assertThat(caseData.getState()).isEqualTo(HANDLING_ERROR);
    }
}
