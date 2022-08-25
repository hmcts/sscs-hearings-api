package uk.gov.hmcts.reform.sscs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.model.partiesnotified.GetPartiesNotifiedResponse;
import uk.gov.hmcts.reform.sscs.model.partiesnotified.PartiesNotifiedRequestPayload;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class HmcPartiesNotifiedApiService {

    private final HmcPartiesNotifiedApi hmcPartiesNotifiedApi;
    private final IdamService idamService;

    public GetPartiesNotifiedResponse getPartiesNotifiedRequest(Long hearingId) {
        log.debug("Sending Get Parties Notified Request for Hearing ID {}", hearingId);
        return hmcPartiesNotifiedApi.getPartiesNotifiedRequest(
            getIdamTokens().getIdamOauth2Token(),
            getIdamTokens().getServiceAuthorization(),
            hearingId);
    }

    public ResponseEntity sendUpdatePartiesNotifiedRequest(PartiesNotifiedRequestPayload partiesNotifiedPayload,
                                                           Long hearingId, Long version,
                                                           LocalDateTime responseReceived) {
        log.debug("Sending Update Parties Notified Request for HearingId {} and request:\n{}",
            hearingId,
            partiesNotifiedPayload);
        return hmcPartiesNotifiedApi.updatePartiesNotifiedRequest(
            getIdamTokens().getIdamOauth2Token(),
            getIdamTokens().getServiceAuthorization(),
            hearingId,
            version,
            responseReceived,
            partiesNotifiedPayload);
    }

    private IdamTokens getIdamTokens() {
        return idamService.getIdamTokens();
    }
}
