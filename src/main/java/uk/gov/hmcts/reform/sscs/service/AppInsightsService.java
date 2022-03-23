package uk.gov.hmcts.reform.sscs.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.telemetry.EventTelemetry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.model.HmcFailureMessage;

@Slf4j
@Service
public final class AppInsightsService {

    static final TelemetryClient telemetryClient = new TelemetryClient();

    private AppInsightsService() {
        // Gradle style check
    }

    public static void sendAppInsightsEvent(HmcFailureMessage hmcFailureMessage) throws JsonProcessingException {
        String message = messageToJson(hmcFailureMessage);

        telemetryClient.trackEvent(new EventTelemetry(message));
        log.info(message);
    }

    private static String messageToJson(HmcFailureMessage hmcFailureMessage) throws JsonProcessingException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        try {
            return ow.writeValueAsString(hmcFailureMessage);
        } catch (JsonProcessingException jpe) {
            log.error("HMC failure message JsonProcessingException for Case ID: {}", hmcFailureMessage.getCaseID());
            throw jpe;
        }
    }
}
