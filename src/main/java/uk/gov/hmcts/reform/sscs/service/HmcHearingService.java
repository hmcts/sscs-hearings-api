package uk.gov.hmcts.reform.sscs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;

@Slf4j
@RequiredArgsConstructor
@Service
public class HmcHearingService {

    private final HmcHearingApi hmcHearingApi;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final IdamService idamService;

    public HearingGetResponse getHearingRequest(String hearingId) {

        return hmcHearingApi.getHearingRequest(
            serviceAuthTokenGenerator.generate(),
            idamService.generateServiceAuthorization(),
            hearingId
        );
    }

}
