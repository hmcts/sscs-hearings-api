package uk.gov.hmcts.reform.sscs.jms.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingRoute;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingState;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.TribunalsEventProcessingException;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequest;
import uk.gov.hmcts.reform.sscs.service.HearingsService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TribunalsHearingsEventTopicListenerTest {

    private TribunalsHearingsEventQueueListener tribunalsHearingsEventQueueListener;

    @Mock
    private HearingsService hearingsService;

    @BeforeEach
    void setup() {
        tribunalsHearingsEventQueueListener = new TribunalsHearingsEventQueueListener(hearingsService);
    }

    @Test
    @DisplayName("When a valid request comes in make sure processHearingRequest is hit")
    void whenAValidRequestComesIn_makeSureProcessHearingRequestIsHit() throws Exception {

        HearingRequest hearingRequest = createHearingRequest();

        tribunalsHearingsEventQueueListener.handleIncomingMessage(hearingRequest);

        verify(hearingsService, times(1)).processHearingRequest((eq(hearingRequest)));
    }

    @Test
    @DisplayName("When an invalid request comes in make sure exception is thrown")
    void whenAnInvalidRequestComesIn_makeSureExceptionIsThrown() throws Exception {

        HearingRequest hearingRequest = new HearingRequest();

        doThrow(GetCaseException.class).when(hearingsService).processHearingRequest(eq(hearingRequest));

        assertThrows(TribunalsEventProcessingException.class, () -> tribunalsHearingsEventQueueListener.handleIncomingMessage(hearingRequest));
    }

    @Test
    @DisplayName("When an null request comes in make sure exception is thrown")
    void whenAnNullRequestComesIn_makeSureExceptionIsThrown() {
        assertThrows(TribunalsEventProcessingException.class, () -> tribunalsHearingsEventQueueListener.handleIncomingMessage(null));
    }

    private HearingRequest createHearingRequest() {
        return HearingRequest.builder("1001")
            .hearingRoute(HearingRoute.LIST_ASSIST)
            .hearingState(HearingState.CREATE_HEARING)
            .build();
    }
}
