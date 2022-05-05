package uk.gov.hmcts.reform.sscs.service.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Request.HttpMethod;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.sscs.model.HmcFailureMessage;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingRequestPayload;
import uk.gov.hmcts.reform.sscs.service.AppInsightsService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
public class FeignClientErrorDecoder implements ErrorDecoder {

    private final AppInsightsService appInsightsService;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public FeignClientErrorDecoder(AppInsightsService appInsightsService) {
        this.appInsightsService = appInsightsService;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        HmcFailureMessage failMsg = extractFailMsg(methodKey, response);

        switch (response.status()) {
            case 400:
            case 401:
            case 403:
            case 404: {
                log.error("Error in calling Feign client. Status code "
                              + response.status() + ", methodKey = " + methodKey);
                log.error("Error details: {}", response.body().toString());
                try {
                    appInsightsService.sendAppInsightsEvent(failMsg);
                } catch (JsonProcessingException e) {
                    log.error("Error sending app insight for event {}", failMsg);
                }
                return new ResponseStatusException(HttpStatus.valueOf(response.status()),
                                                   "Error in calling the client method:" + methodKey);
            }
            default:
                return new ResponseStatusException(HttpStatus.valueOf(response.status()), response.reason());
        }
    }

    private HmcFailureMessage extractFailMsg(String methodKey, Response response) {
        Request originalRequest = response.request();
        HttpMethod httpMethod = originalRequest.httpMethod();
        HmcFailureMessage failMsg = null;

        if (httpMethod.equals(HttpMethod.POST) || httpMethod.equals(HttpMethod.PUT)) {
            HearingRequestPayload payload = null;
            try {
                payload = mapToPostHearingRequest(originalRequest);
            } catch (JsonProcessingException e) {
                log.error("JsonProcessingException when mapping hearing request: "
                              + response.status() + ", methodKey = " + methodKey);
                log.error("Error details: {}", new String(originalRequest.body(), StandardCharsets.UTF_8));
            }
            if (payload != null) {
                failMsg = HmcFailureMessage.builder()
                    .requestType(httpMethod.toString())
                    .caseID(Long.valueOf(payload.getCaseDetails().getCaseId()))
                    .timeStamp(LocalDateTime.now())
                    .errorCode(String.valueOf(response.status()))
                    .errorMessage(response.reason())
                    .build();
            }
        } else {
            Long caseId = Long.parseLong(response.request().requestTemplate().queries().get("id").iterator().next());
            failMsg = HmcFailureMessage.builder()
                .requestType(httpMethod.toString())
                .caseID(caseId)
                .timeStamp(LocalDateTime.now())
                .errorCode(String.valueOf(response.status()))
                .errorMessage(response.reason())
                .build();
        }

        return failMsg;
    }

    private HearingRequestPayload mapToPostHearingRequest(Request request) throws JsonProcessingException {
        HearingRequestPayload hearingRequestPayload;
        String requestBody = new String(request.body(), StandardCharsets.UTF_8);
        try {
            hearingRequestPayload = OBJECT_MAPPER.readValue(requestBody, HearingRequestPayload.class);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException when mapping for: {}", requestBody);
            throw e;
        }
        return hearingRequestPayload;
    }

}
