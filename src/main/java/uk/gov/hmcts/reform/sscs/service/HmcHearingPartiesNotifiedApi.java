package uk.gov.hmcts.reform.sscs.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.sscs.config.FeignClientConfig;
import uk.gov.hmcts.reform.sscs.model.partiesnotified.PartiesNotified;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@FeignClient(name = "hmc-hearing", url = "${hmc.url}", configuration = FeignClientConfig.class)
public interface HmcHearingPartiesNotifiedApi {

    String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @PutMapping(value = "/partiesNotified", consumes = MediaType.APPLICATION_JSON_VALUE)
    void updatePartiesNotifiedHearingRequest(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestParam("id") String id,
        @RequestParam("version") String responseVersion,
        @RequestBody PartiesNotified partiesNotified
    );
}
