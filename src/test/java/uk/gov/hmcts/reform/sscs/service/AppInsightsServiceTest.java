package uk.gov.hmcts.reform.sscs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.model.HmcFailureMessage;
import uk.gov.hmcts.reform.sscs.model.Message;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatNoException;

@ExtendWith(MockitoExtension.class)
public class AppInsightsServiceTest {

    private static final String REQUEST_TYPE = "request";
    private static final Long CASE_ID = 1000000000L;
    private static final LocalDateTime TIME_STAMP = LocalDateTime.now();
    private static final String ERROR_CODE = "error code";
    private static final String ERROR_MESSAGE = "error message";

    private AppInsightsService appInsightsService;

    @BeforeEach
    public void setUp() {
        appInsightsService = new AppInsightsService();
    }

    @Test
    public void testAppInsightsServiceDoesNotThrowJPE() {
        Message message = messageInit();

        assertThatNoException().isThrownBy(() -> appInsightsService.sendAppInsightsEvent(message));
    }

    private Message messageInit() {
        return new HmcFailureMessage(REQUEST_TYPE, CASE_ID, TIME_STAMP, ERROR_CODE, ERROR_MESSAGE);
    }
}
