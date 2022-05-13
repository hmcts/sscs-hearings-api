package uk.gov.hmcts.reform.sscs.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.sscs.config.FeignClientConfig;
import uk.gov.hmcts.reform.sscs.model.partiesnotified.PartiesNotified;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@SuppressWarnings({"PMD.UseObjectForClearerAPI"})
// TODO Unsuppressed lot string argument as request will have two parameter
@FeignClient(name = "hmc-hearing", url = "${hmc.url}", configuration = FeignClientConfig.class)
public interface HmcHearingPartiesNotifiedApi {

    String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    String PARTIES_NOTIFIED_ENDPOINT = "/partiesNotified";
    String ID = "id";
    String VERSION = "version";

    @PutMapping(value = PARTIES_NOTIFIED_ENDPOINT, consumes = MediaType.APPLICATION_JSON_VALUE)
    void updatePartiesNotifiedHearingRequest(
            @RequestHeader(AUTHORIZATION) String authorisation,
            @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
            @RequestParam(ID) String id,
            @RequestParam(VERSION) String responseVersion,
            @RequestBody PartiesNotified partiesNotified
    );

    @GetMapping(value = PARTIES_NOTIFIED_ENDPOINT, consumes = MediaType.APPLICATION_JSON_VALUE)
    PartiesNotified getPartiesNotifiedRequest(
            @RequestHeader(AUTHORIZATION) String authorisation,
            @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
            @RequestParam(ID) String id
    );
}
