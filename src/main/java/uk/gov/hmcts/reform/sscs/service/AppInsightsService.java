package uk.gov.hmcts.reform.sscs.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.model.AppInsights;

@Slf4j
@Service
public class AppInsightsService {

    public void sendAppInsights(AppInsights appInsights) throws JsonProcessingException {
        log.info(appInsightsToJson(appInsights));
    }

    private String appInsightsToJson(AppInsights appInsights) throws JsonProcessingException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        try {
            return ow.writeValueAsString(appInsights);
        } catch (JsonProcessingException jpe) {
            log.error("App Insights JsonProcessingException for Case ID: {}", appInsights.getCaseID());
            throw jpe;
        }
    }
}
