package uk.gov.hmcts.reform.sscs.controllers;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.sscs.model.HearingPayload;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.sscs.model.HearingRequestDetail;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;


public interface HearingApi {

    String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @PostMapping(
        value = "/hearing",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    HearingRequestDetail createHearingRequest(@RequestHeader(AUTHORIZATION) String authorisation,
                                              @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
                                              @RequestBody HearingPayload hearingPayload
    );

}
