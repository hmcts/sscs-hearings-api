package uk.gov.hmcts.reform.sscs.jms.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingRoute;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingState;
import uk.gov.hmcts.reform.sscs.exception.*;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequest;
import uk.gov.hmcts.reform.sscs.service.HearingsService;

import java.util.stream.Stream;

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

    @ParameterizedTest
    @DisplayName("When an invalid request comes in make sure exception is thrown")
    @MethodSource("throwableParameters")
    void whenAnInvalidRequestComesIn_makeSureExceptionIsThrown(Class<? extends Throwable> throwable) throws Exception {

        HearingRequest hearingRequest = new HearingRequest();

        doThrow(throwable).when(hearingsService).processHearingRequest(eq(hearingRequest));

        assertThrows(TribunalsEventProcessingException.class, () -> tribunalsHearingsEventQueueListener.handleIncomingMessage(hearingRequest));
    }

    private static Stream<Arguments> throwableParameters() {
        return Stream.of(
            Arguments.of(GetCaseException.class),
            Arguments.of(UnhandleableHearingStateException.class),
            Arguments.of(UpdateCaseException.class),
            Arguments.of(InvalidMappingException.class)
        );
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
