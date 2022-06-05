package uk.gov.hmcts.reform.sscs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sscs.exception.GetHearingException;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingCancelRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HmcUpdateResponse;

import static java.util.Objects.isNull;

@Slf4j
@RequiredArgsConstructor
@Service
public class HmcHearingApiService {

    private final HmcHearingApi hmcHearingApi;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final IdamService idamService;

    public HearingGetResponse getHearingRequest(String hearingId) throws GetHearingException {
        log.debug("Sending Get Hearing Request for Hearing ID {}", hearingId);
        HearingGetResponse hearingResponse = hmcHearingApi.getHearingRequest(
                serviceAuthTokenGenerator.generate(),
                idamService.generateServiceAuthorization(),
                hearingId);
        if (isNull(hearingResponse)) {
            throw new GetHearingException(String.format("Failed to retrieve hearing with Id: %s from HMC", hearingId));
        }
        return hearingResponse;
    }

    public HmcUpdateResponse sendCreateHearingRequest(HearingRequestPayload hearingPayload) {
        log.debug("Sending Create Hearing Request for Case ID {} and request:\n{}",
                hearingPayload.getCaseDetails().getCaseId(),
                hearingPayload);
        return hmcHearingApi.createHearingRequest(
                serviceAuthTokenGenerator.generate(),
                idamService.generateServiceAuthorization(),
                hearingPayload);
    }

    public HmcUpdateResponse sendUpdateHearingRequest(HearingRequestPayload hearingPayload, String hearingId) {
        log.debug("Sending Update Hearing Request for Case ID {}, HearingId {} and request:\n{}",
                hearingPayload.getCaseDetails().getCaseId(),
                hearingId,
                hearingPayload);
        return hmcHearingApi.updateHearingRequest(
                serviceAuthTokenGenerator.generate(),
                idamService.generateServiceAuthorization(),
                hearingId,
                hearingPayload);
    }

    public HmcUpdateResponse sendCancelHearingRequest(HearingCancelRequestPayload hearingPayload, String hearingId) {
        log.debug("Sending Update Hearing Request for Hearing ID {} and request:\n{}",
                hearingId,
                hearingPayload);
        return hmcHearingApi.cancelHearingRequest(
                serviceAuthTokenGenerator.generate(),
                idamService.generateServiceAuthorization(),
                hearingId,
                hearingPayload);
    }
}
