package uk.gov.hmcts.reform.sscs.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.sscs.model.HmcFailureMessage;
import uk.gov.hmcts.reform.sscs.model.Message;

import static org.mockito.BDDMockito.willThrow;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("integration")
public class AppInsightsServiceTest {

    @Mock
    private AppInsightsService appInsightsService;

    @Test
    public void test() throws JsonProcessingException {
        Message message = badMessageInit();

        willThrow(JsonProcessingException.class).given(appInsightsService).sendAppInsightsEvent(message);
    }

    private Message badMessageInit() {
        return new HmcFailureMessage(null, null, null, null, null);
    }
}
