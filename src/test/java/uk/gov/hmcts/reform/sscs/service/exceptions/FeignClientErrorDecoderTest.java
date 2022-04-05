package uk.gov.hmcts.reform.sscs.service.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.sscs.model.HmcFailureMessage;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Slf4j
@ExtendWith(MockitoExtension.class)
class FeignClientErrorDecoderTest {

    private static final String ID = "id";
    private static final Long CASE_ID = 1000000000L;
    private static final String ERROR_MSG = " \"Error in calling the client method:someMethod\"";
    private static final Map<String, Collection<String>> headers = new HashMap<>();

    private FeignClientErrorDecoder feignClientErrorDecoder;
    private HearingRequestPayload hearingRequestPayload;
    private ArgumentCaptor<HmcFailureMessage> argument;

    @Mock
    private AppInsightsService appInsightsService;

    @BeforeEach
    void setUp() {
        feignClientErrorDecoder = new FeignClientErrorDecoder(appInsightsService);
        hearingRequestPayload = new HearingRequestPayload();
        hearingRequestPayload.setCaseDetails(new CaseDetails());
        hearingRequestPayload.getCaseDetails().setCaseRef(String.valueOf(CASE_ID));
        argument = ArgumentCaptor.forClass(HmcFailureMessage.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 403, 404})
    void should_handle_4xx_post_put_error(int statusCode) throws JsonProcessingException {
        Request request =
            Request.create(Request.HttpMethod.POST, "url",
                           headers, Request.Body.create(toJsonString(hearingRequestPayload)), null);

        Response response = buildResponse(request, statusCode);

        Throwable throwable = feignClientErrorDecoder.decode("someMethod", response);
        verify(appInsightsService, times(1)).sendAppInsightsEvent(argument.capture());

        assertThat(throwable).isInstanceOf(ResponseStatusException.class);
        assertEquals(request.httpMethod().toString(), argument.getValue().getRequestType());
        assertEquals(CASE_ID, argument.getValue().getCaseID());
        assertEquals(String.valueOf(statusCode), argument.getValue().getErrorCode());
        assertEquals(response.reason(), argument.getValue().getErrorMessage());

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
        Map<String, Collection<String>> queries = new HashMap<>();
        queries.computeIfAbsent(ID, k -> new ArrayList<>()).add(String.valueOf(CASE_ID));
        RequestTemplate requestTemplate = new RequestTemplate();
        requestTemplate.queries(queries);

        Request request =
            Request.create(Request.HttpMethod.GET, "url",
                           headers, Request.Body.empty(), requestTemplate);
        Response response = buildResponse(request, statusCode);

        Throwable throwable = feignClientErrorDecoder.decode("someMethod", response);
        verify(appInsightsService, times(1)).sendAppInsightsEvent(argument.capture());

        assertThat(throwable).isInstanceOf(ResponseStatusException.class);
        assertEquals(request.httpMethod().toString(), argument.getValue().getRequestType());
        assertEquals(CASE_ID, argument.getValue().getCaseID());
        assertEquals(String.valueOf(statusCode), argument.getValue().getErrorCode());
        assertEquals(response.reason(), argument.getValue().getErrorMessage());

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

        assertThat(throwable).isInstanceOf(ResponseStatusException.class);
        String test = throwable.getMessage();
        assertThat(throwable.getMessage()).contains("Internal server error");
    }

    @Test
    void testSendAppInsight() throws JsonProcessingException {
        Request request =
            Request.create(Request.HttpMethod.POST, "url",
                           headers, Request.Body.create(toJsonString(hearingRequestPayload)), null);
        Response response = buildResponse(request, 400);

        doThrow(mock(JsonProcessingException.class)).when(appInsightsService).sendAppInsightsEvent(any(Message.class));

        Throwable throwable = feignClientErrorDecoder.decode("someMethod", response);
        assertThat(throwable).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void testShouldThrowMappingException() throws JsonProcessingException {
        Request request =
            Request.create(Request.HttpMethod.POST, "url",
                           headers, Request.Body.create(toJsonString(new CaseDetails())), null);
        Response response = buildResponse(request, 400);

        Throwable throwable = feignClientErrorDecoder.decode("someMethod", response);
        assertThat(throwable).isInstanceOf(ResponseStatusException.class);
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
            log.error("JsonProcessingException when mapping for: {}", object);
        }
        return jsonString;
    }

    private Response buildResponse(Request req, int statusCode) {
        String reason = "";
        String bodyMsg = "";

        if (statusCode == 400) {
            reason = HttpStatus.BAD_REQUEST.name();
            bodyMsg = "Bad Request data";
        } else if (statusCode == 401) {
            reason = HttpStatus.UNAUTHORIZED.name();
            bodyMsg = "Authorization failed";
        } else if (statusCode == 403) {
            reason = HttpStatus.FORBIDDEN.name();
            bodyMsg = "Forbidden access";
        } else if (statusCode == 404) {
            reason = HttpStatus.NOT_FOUND.name();
            bodyMsg = "No data found";
        }

        return Response.builder()
            .request(req)
            .status(statusCode)
            .reason(reason)
            .body(bodyMsg, StandardCharsets.UTF_8)
            .build();
    }
}
