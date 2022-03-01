package uk.gov.hmcts.reform.sscs.service;

import feign.codec.Decoder;
import feign.codec.StringDecoder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.sscs.model.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.model.HearingPayload;
import uk.gov.hmcts.reform.sscs.model.HearingRequestDetail;

import java.util.Optional;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(name = "hmc-hearing", url = "${hmc.url}", configuration = HmcHearingApi.Config.class)
public interface HmcHearingApi {

    String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    class Config {
        @Bean
        Decoder stringDecoder() {
            return new StringDecoder();
        }
    }


    @PostMapping(value = "/hearing", consumes = MediaType.APPLICATION_JSON_VALUE)
    HearingRequestDetail createHearingRequest(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody HearingPayload hearingPayload
    );


    @GetMapping(value = "/hearing", consumes = MediaType.APPLICATION_JSON_VALUE)
    HearingGetResponse getHearingRequest(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestParam(name = "id") String id,
        @RequestParam Optional<String> isValid
    );

}
