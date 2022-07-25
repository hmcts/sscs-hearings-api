package uk.gov.hmcts.reform.sscs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus;
import uk.gov.hmcts.reform.sscs.model.multi.hearing.HearingsGetResponse;

@Slf4j
@RequiredArgsConstructor
@Service
public class HmcHearingsApiService {

    private final HmcHearingsApi hmcHearingsApi;
    private final IdamService idamService;

    public HearingsGetResponse getHearingsRequest(String caseId, HmcStatus hmcStatus) {
        log.debug("Sending Get Hearings Request for Case ID {}", caseId);
        return hmcHearingsApi.getHearingsRequest(
            getIdamTokens().getIdamOauth2Token(),
            getIdamTokens().getServiceAuthorization(),
            caseId,
            hmcStatus);
    }

    private IdamTokens getIdamTokens() {
        return idamService.getIdamTokens();
    }
}
