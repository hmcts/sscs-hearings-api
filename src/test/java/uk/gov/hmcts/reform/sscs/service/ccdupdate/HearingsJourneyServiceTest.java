package uk.gov.hmcts.reform.sscs.service.ccdupdate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.InvalidIdException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.model.messaging.HearingUpdate;
import uk.gov.hmcts.reform.sscs.model.messaging.HmcMessage;
import uk.gov.hmcts.reform.sscs.model.messaging.HmcStatus;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.service.CcdCaseService;
import uk.gov.hmcts.reform.sscs.service.HmcHearingService;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.sscs.model.messaging.HmcStatus.CANCELLED;
import static uk.gov.hmcts.reform.sscs.model.messaging.HmcStatus.EXCEPTION;
import static uk.gov.hmcts.reform.sscs.model.messaging.HmcStatus.LISTED;
import static uk.gov.hmcts.reform.sscs.model.messaging.HmcStatus.UPDATE_SUBMITTED;

@ExtendWith(MockitoExtension.class)
class HearingsJourneyServiceTest {

    @Mock
    private HmcHearingService hmcHearingService;

    @Mock
    private CcdCaseService ccdCaseService;

    @Mock
    private CcdStateUpdateService ccdStateUpdateService;

    @Mock
    private CcdLocationUpdateService ccdLocationUpdateService;

    @InjectMocks
    private HearingsJourneyService underTest;

    @Test
    void shouldThrowExceptionIfRequiredParameterIsMissing() {
        // given
        HmcMessage hmcMessage = HmcMessage.builder().build();

        // then
        UpdateCaseException updateCaseException = assertThrows(
            UpdateCaseException.class,
            () -> underTest.process(hmcMessage)
        );

        assertThat(updateCaseException.getMessage()).isEqualTo("HMC message field hearingID is missing");
    }


    @Test
    void shouldThrowExceptionIfHmcMessageIsMissing() {

        // then
        UpdateCaseException updateCaseException = assertThrows(
            UpdateCaseException.class,
            () -> underTest.process(null)
        );

        assertThat(updateCaseException.getMessage()).isEqualTo("HMC message must not be mull");
    }

    @Test
    void shouldThrowExceptionIfHmcStatusIsMissing() {
        // given
        HmcMessage hmcMessage = HmcMessage.builder()
            .hearingID("123")
            .hearingUpdate(HearingUpdate.builder().build())
            .build();

        // then
        assertThatExceptionOfType(UpdateCaseException.class)
                .isThrownBy(() -> underTest.process(hmcMessage))
                .withMessageContaining("HMC message field HmcStatus is missing");
    }


    @ParameterizedTest
    @CsvSource({
        "LISTED,hearingBooked,SSCS - new case sent to HMC,SSCS - new case sent to HMC",
        "UPDATE_SUBMITTED,updateCaseOnly,SSCS - updated case sent to HMC,SSCS - updated case sent to HMC",
        "CANCELLED,updateCaseOnly,SSCS - case cancelled,SSCS - case cancelled",
        "EXCEPTION,updateCaseOnly,SSCS - exception occurred,SSCS - exception occurred"
    })
    void shouldUpdateCcdStateAndLocationAndSndNewHearingBookedEvent(
        HmcStatus hmcStatus, String eventType, String summary, String description)
        throws GetCaseException, UpdateCaseException, InvalidIdException {

        // given
        final String hearingId = "123";
        HmcMessage hmcMessage = HmcMessage.builder()
            .hearingID(hearingId)
            .hearingUpdate(HearingUpdate.builder()
                               .hmcStatus(hmcStatus)
                               .build())
            .build();
        SscsCaseData caseData = SscsCaseData.builder().build();
        SscsCaseDetails caseDetails = SscsCaseDetails.builder().data(caseData).build();
        HearingGetResponse hearingResponse = new HearingGetResponse();

        when(hmcHearingService.getHearingRequest(hearingId)).thenReturn(hearingResponse);
        when(ccdCaseService.getCaseDetails(hearingId)).thenReturn(caseDetails);

        // when
        underTest.process(hmcMessage);

        // then
        if (LISTED.equals(hmcStatus)) {
            verify(ccdStateUpdateService).updateListed(hearingResponse, caseData);
            verify(ccdLocationUpdateService).updateVenue(hmcMessage, caseData);
        } else if (UPDATE_SUBMITTED.equals(hmcStatus)) {
            verify(ccdStateUpdateService).updateListed(hearingResponse, caseData);
            verify(ccdLocationUpdateService).updateVenue(hmcMessage, caseData);
        } else if (CANCELLED.equals(hmcStatus)) {
            verify(ccdStateUpdateService).updateCancelled(hearingResponse, caseData);
            verify(ccdLocationUpdateService, never()).updateVenue(hmcMessage, caseData);
        } else if (EXCEPTION.equals(hmcStatus)) {
            verify(ccdStateUpdateService).updateFailed(caseData);
            verify(ccdLocationUpdateService, never()).updateVenue(hmcMessage, caseData);
        }

        verify(ccdCaseService).updateCaseData(
            caseData,
            EventType.getEventTypeByCcdType(eventType),
            summary,
            description
        );
    }


    @Test
    void shouldThrowExceptionWhenCallToHmcFailed() {
        // given
        final String hearingId = "123";
        HmcMessage hmcMessage = HmcMessage.builder().hearingID(hearingId)
            .hearingUpdate(HearingUpdate.builder().hmcStatus(HmcStatus.EXCEPTION).build()).build();
        when(hmcHearingService.getHearingRequest(hearingId)).thenReturn(null);

        // when + then
        assertThatExceptionOfType(GetCaseException.class)
                .isThrownBy(() -> underTest.process(hmcMessage))
                .withMessageContaining("Failed to retrieve hearing with Id: 123 from HMC");
    }
}
