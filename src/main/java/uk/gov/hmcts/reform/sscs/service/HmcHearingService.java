package uk.gov.hmcts.reform.sscs.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;

@Slf4j
@Service
public class HmcHearingService {

    private final HmcHearingApi hmcHearingApi;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final IdamService idamService;

    public HmcHearingService(HmcHearingApi hmcHearingApi,
                             AuthTokenGenerator serviceAuthTokenGenerator,
                             IdamService idamService) {
        this.hmcHearingApi = hmcHearingApi;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.idamService = idamService;
    }

    public HearingGetResponse getHearingRequest(String hearingId) {

        return hmcHearingApi.getHearingRequest(
            serviceAuthTokenGenerator.generate(),
            idamService.generateServiceAuthorization(),
            hearingId
        );
    }

}
