package uk.gov.hmcts.reform.sscs.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.model.HmcFailureMessage;

@Slf4j
@Service
public final class AppInsightsService {

    private AppInsightsService() {
        // Gradle style check
    }

    public static void sendAppInsightsLog(HmcFailureMessage hmcFailureMessage) throws JsonProcessingException {
        log.info(messageToJson(hmcFailureMessage));
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
