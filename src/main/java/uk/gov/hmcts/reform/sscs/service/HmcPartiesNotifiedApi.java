package uk.gov.hmcts.reform.sscs.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.sscs.config.FeignClientConfig;
import uk.gov.hmcts.reform.sscs.model.partiesnotified.GetPartiesNotifiedResponse;
import uk.gov.hmcts.reform.sscs.model.partiesnotified.PartiesNotifiedRequestPayload;

import java.time.LocalDateTime;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(name = "hmc-hearing", url = "${hmc.url}", configuration = FeignClientConfig.class)
public interface HmcPartiesNotifiedApi {

    String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    String PARTIES_NOTIFIED_ENDPOINT = "/partiesNotified";
    String VERSION = "version";
    String RECEIVED = "received";

    @GetMapping(value = PARTIES_NOTIFIED_ENDPOINT + "/{hearingId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    GetPartiesNotifiedResponse getPartiesNotifiedRequest(
            @RequestHeader(AUTHORIZATION) String authorisation,
            @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
            @PathVariable Long hearingId
    );

    @PutMapping(value = PARTIES_NOTIFIED_ENDPOINT + "/{hearingId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity updatePartiesNotifiedRequest(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @PathVariable Long hearingId,
        @RequestParam(VERSION) Long version,
        @RequestParam(RECEIVED) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime received,
        @RequestBody PartiesNotifiedRequestPayload partiesNotifiedPayload
    );
}
