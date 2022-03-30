package uk.gov.hmcts.reform.sscs.service.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.sscs.model.Message;
import uk.gov.hmcts.reform.sscs.model.single.hearing.CaseDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingRequestPayload;
import uk.gov.hmcts.reform.sscs.service.AppInsightsService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FeignClientErrorDecoderTest {

    private static final String ID = "id";
    private static final String CASE_ID = "CASE-123";
    private static final String ERROR_MSG = " \"Error in calling the client method:someMethod\"";

    private FeignClientErrorDecoder feignClientErrorDecoder;
    private HearingRequestPayload hearingRequestPayload;

    @Mock
    private AppInsightsService appInsightsService;

    @BeforeEach
    void setUp() {
        feignClientErrorDecoder = new FeignClientErrorDecoder(appInsightsService);
        hearingRequestPayload = new HearingRequestPayload();
        hearingRequestPayload.setCaseDetails(new CaseDetails());
        hearingRequestPayload.getCaseDetails().setCaseRef(CASE_ID);
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 403, 404})
    void should_handle_4xx_post_put_error(int statusCode) throws JsonProcessingException {
        Map<String, Collection<String>> headers = new HashMap<>();
        Request request =
            Request.create(Request.HttpMethod.POST, "url",
                           headers, Request.Body.create(toJsonString(hearingRequestPayload)), null);

        Response response = Response.builder()
            .request(request)
            .status(statusCode)
            .reason("Error")
            .body("Some body", StandardCharsets.UTF_8)
            .build();

        Throwable throwable = feignClientErrorDecoder.decode("someMethod", response);
        verify(appInsightsService, times(1)).sendAppInsightsEvent(any(Message.class));
        assertThat(throwable).isInstanceOf(ResponseStatusException.class);


        if (statusCode == 400) {
            assertThat(throwable.getMessage())
                .contains(HttpStatus.BAD_REQUEST + ERROR_MSG);
        } else if (statusCode == 401) {
            assertThat(throwable.getMessage())
                .contains(HttpStatus.UNAUTHORIZED + ERROR_MSG);
        } else if (statusCode == 403) {
            assertThat(throwable.getMessage())
                .contains(HttpStatus.FORBIDDEN + ERROR_MSG);
        } else if (statusCode == 404) {
            assertThat(throwable.getMessage())
                .contains(HttpStatus.NOT_FOUND + ERROR_MSG);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 403, 404})
    void should_handle_4xx_get_delete_error(int statusCode) throws JsonProcessingException {
        Map<String, Collection<String>> headers = new HashMap<>();

        Map<String, Collection<String>> queries = new HashMap<>();
        queries.computeIfAbsent(ID, k -> new ArrayList<>()).add(CASE_ID);
        RequestTemplate requestTemplate = new RequestTemplate();
        requestTemplate.queries(queries);

        Request request =
            Request.create(Request.HttpMethod.GET, "url",
                           headers, Request.Body.empty(), requestTemplate);
        Response response = Response.builder()
            .request(request)
            .status(statusCode)
            .reason("Error")
            .body("Some body", StandardCharsets.UTF_8)
            .build();

        Throwable throwable = feignClientErrorDecoder.decode("someMethod", response);
        verify(appInsightsService, times(1)).sendAppInsightsEvent(any(Message.class));
        assertThat(throwable).isInstanceOf(ResponseStatusException.class);

        if (statusCode == 400) {
            assertThat(throwable.getMessage())
                .contains(HttpStatus.BAD_REQUEST + ERROR_MSG);
        } else if (statusCode == 401) {
            assertThat(throwable.getMessage())
                .contains(HttpStatus.UNAUTHORIZED + ERROR_MSG);
        } else if (statusCode == 403) {
            assertThat(throwable.getMessage())
                .contains(HttpStatus.FORBIDDEN + ERROR_MSG);
        } else if (statusCode == 404) {
            assertThat(throwable.getMessage())
                .contains(HttpStatus.NOT_FOUND + ERROR_MSG);
        }
    }

    @Test
    void should_handle_500_error() {

        Map<String, Collection<String>> headers = new HashMap<>();
        Request request =
            Request.create(Request.HttpMethod.PUT, "url",
                           headers, Request.Body.create(toJsonString(hearingRequestPayload)), null);
        Response response = Response.builder()
            .request(request)
            .status(500)
            .reason("Internal server error")
            .body("Some body", StandardCharsets.UTF_8)
            .build();

        Throwable throwable = feignClientErrorDecoder.decode("someMethod", response);

        assertThat(throwable).isInstanceOf(Exception.class);
        assertThat(throwable.getMessage()).contains("Internal server error");
    }

    private String toJsonString(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String jsonString = "";
        try {
            jsonString = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonString;
    }
}
