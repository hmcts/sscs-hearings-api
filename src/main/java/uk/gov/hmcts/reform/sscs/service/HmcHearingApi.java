package uk.gov.hmcts.reform.sscs.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.codec.Decoder;
import feign.jackson.JacksonDecoder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.sscs.model.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.model.hmc.HearingRequestPayload;
import uk.gov.hmcts.reform.sscs.model.hmc.HearingResponse;

import javax.validation.Valid;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(name = "hmc-hearing", url = "${hmc.url}", configuration = HmcHearingApi.Config.class)
public interface HmcHearingApi {

    String SERVICE_AUTHORIZATION = "ServiceAuthorization";


    @PostMapping(value = "/hearing", consumes = MediaType.APPLICATION_JSON_VALUE)
    HearingResponse createHearingRequest(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody @Valid HearingRequestPayload hearingPayload
    );

    @GetMapping(value = "/hearing", consumes = MediaType.APPLICATION_JSON_VALUE)
    HearingGetResponse getHearingRequest(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestParam(value = "id") String id
    );

    class Config {
        @Bean
        @Primary
        Decoder feignDecoder(ObjectMapper objectMapper) {
            return new JacksonDecoder(objectMapper);
        }

    }


}
