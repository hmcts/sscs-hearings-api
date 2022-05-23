package uk.gov.hmcts.reform.sscs.service.ccdupdate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.model.messaging.HearingUpdate;
import uk.gov.hmcts.reform.sscs.model.messaging.HmcMessage;
import uk.gov.hmcts.reform.sscs.model.messaging.HmcStatus;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.service.CcdCaseService;
import uk.gov.hmcts.reform.sscs.service.HmcHearingService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.sscs.model.messaging.HmcStatus.CANCELLED;
import static uk.gov.hmcts.reform.sscs.model.messaging.HmcStatus.EXCEPTION;
import static uk.gov.hmcts.reform.sscs.model.messaging.HmcStatus.LISTED;
import static uk.gov.hmcts.reform.sscs.model.messaging.HmcStatus.UPDATE_SUBMITTED;

class HearingsJourneyServiceTest {

    private final HmcHearingService hmcHearingService = mock(HmcHearingService.class);
    private final CcdCaseService ccdCaseService = mock(CcdCaseService.class);
    private final CcdStateUpdateService ccdStateUpdateService = mock(CcdStateUpdateService.class);
    private final CcdLocationUpdateService ccdLocationUpdateService = mock(CcdLocationUpdateService.class);

    private final HearingsJourneyService underTest = new HearingsJourneyService(
        hmcHearingService,
        ccdCaseService,
        ccdStateUpdateService,
        ccdLocationUpdateService
    );

    @Test
    void shouldThrowExceptionIfRequiredParameterIsMissing() {
        // given
        HmcMessage hmcMessage = HmcMessage.builder().build();

        // then
        NullPointerException nullPointerException = assertThrows(
            NullPointerException.class,
            () -> underTest.process(hmcMessage)
        );

        assertThat(nullPointerException.getMessage()).isEqualTo("HMC message field hearingID is missing");
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
        throws GetCaseException, UpdateCaseException {

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
        when(ccdCaseService.getCaseDetails(Long.parseLong(hearingId))).thenReturn(caseDetails);

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

        // then
        GetCaseException getCaseException = assertThrows(
            GetCaseException.class,
            () -> underTest.process(hmcMessage)
        );

        assertThat(getCaseException.getMessage()).isEqualTo("Failed to retrieve hearing with Id: 123 from HMC");
    }
}
