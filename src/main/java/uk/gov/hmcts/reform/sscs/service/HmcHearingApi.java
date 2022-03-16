package uk.gov.hmcts.reform.sscs.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.sscs.config.FeignClientConfig;
import uk.gov.hmcts.reform.sscs.model.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.model.hmc.HearingRequestPayload;
import uk.gov.hmcts.reform.sscs.model.hmc.HearingResponse;

import javax.validation.Valid;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(name = "hmc-hearing", url = "${hmc.url}", configuration = FeignClientConfig.class)
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
}
