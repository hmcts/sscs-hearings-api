package uk.gov.hmcts.reform.sscs.logger;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDeleteRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingResponse;

@Slf4j
@Component
public class TraceLogger {

    public void createHearingRequestLogger(HearingRequestPayload hearingRequestPayload, HearingResponse hearingResponse) {
        log.trace(
            "Create Hearing Request payload: {}, Create Hearing Response payload: {} ",
            hearingRequestPayload,
            hearingResponse
        );

    }

    public void updateHearingRequestLogger(String hearingId, HearingRequestPayload hearingRequestPayload, HearingResponse hearingResponse) {
        log.trace(
            "Update Hearing Details for Hearing Id: {}, Request payload: {}, Update Hearing Response payload: {} ",
            hearingId,
            hearingRequestPayload,
            hearingResponse
        );
    }

    public void getHearingRequestDetailsLogger(String hearingId, HearingResponse hearingResponse) {
        log.trace(
            "Get Hearing Details for Hearing Id : {}, Get Hearing Request Details Response payload: {} ",
            hearingId,
            hearingResponse
        );
    }

    public void cancelHearingRequestLogger(String hearingId, HearingDeleteRequestPayload RequestPayload, HearingResponse hearingResponse) {
        log.trace(
            " Cancel Hearing Request for Hearing Id : {}, and cancellation ReasonCode for Hearing: {}, Hearing request cancellation response : {} ",
            hearingId,
            RequestPayload,
            hearingResponse
        );
    }

    public void updatePartyNotifiedRequestLogger(String hearingId, HearingRequestPayload RequestPayload, HearingResponse hearingResponse) {
        log.trace(
            "Update PartyNotified Request details for Hearing Id: {}, Request payload: {}, Update PartyNotified Response payload: {} ",
            hearingId,
            RequestPayload,
            hearingResponse
        );
    }

}
