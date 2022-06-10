package uk.gov.hmcts.reform.sscs.service.hmc.topic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.exception.CaseException;
import uk.gov.hmcts.reform.sscs.exception.InvalidHmcMessageException;
import uk.gov.hmcts.reform.sscs.exception.MessageProcessingException;
import uk.gov.hmcts.reform.sscs.model.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RequestDetails;
import uk.gov.hmcts.reform.sscs.service.CcdCaseService;
import uk.gov.hmcts.reform.sscs.service.HmcHearingApiService;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.AWAITING_LISTING;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.CANCELLED;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.EXCEPTION;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.LISTED;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.UPDATE_SUBMITTED;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.ListingStatus.FIXED;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessHmcMessageService {

    private static final String REQUEST_CANCELLED = "Cancelled";

    @Value("${sscs.serviceCode}")
    private String sscsServiceCode;

    private final HmcHearingApiService hmcHearingApiService;

    private final CcdCaseService ccdCaseService;

    private final HearingUpdateService hearingUpdateService;

    public void processEventMessage(HmcMessage hmcMessage)
            throws CaseException, MessageProcessingException {

        Long caseId = hmcMessage.getCaseId();
        String hearingId = hmcMessage.getHearingId();

        HmcStatus hmcMessageStatus = hmcMessage.getHearingUpdate().getHmcStatus();

        HearingGetResponse hearingResponse = hmcHearingApiService.getHearingRequest(hearingId);

        HmcStatus hmcStatus = hearingResponse.getRequestDetails().getStatus();

        checkStatuses(caseId, hearingId, hmcMessageStatus, hmcStatus);

        SscsCaseData caseData = ccdCaseService.getCaseDetails(caseId).getData();

        if (stateNotHandled(hmcStatus, hearingResponse)) {
            log.info("CCD state has not been updated for the Hearing ID {} and Case ID {}",
                hearingId, caseId);
            return;
        }

        if (isHearingUpdated(hmcStatus, hearingResponse)) {
            hearingUpdateService.updateHearing(hearingResponse, caseData);
        }

        String ccdUpdateDescription = String.format(hmcStatus.getCcdUpdateDescription(), hearingId);

        ccdCaseService.updateCaseData(caseData,
                hmcStatus.getCcdUpdateEventType(),
                hmcStatus.getCcdUpdateSummary(),
                ccdUpdateDescription);

        log.info("Hearing message {} processed for case reference {}",
                hmcMessage.getHearingId(),
                hmcMessage.getCaseId());
    }

    private void checkStatuses(Long caseId, String hearingId, HmcStatus hmcMessageStatus, HmcStatus hmcStatus)
        throws InvalidHmcMessageException {
        if (hmcMessageStatus != hmcStatus) {
            throw new InvalidHmcMessageException(String.format("HMC Message Status '%s' does not match the GET request status '%s' "
                    + "for Case ID %s and Hearing ID %s",
                hmcMessageStatus, hmcStatus, caseId, hearingId));
        }
    }


    private boolean stateNotHandled(HmcStatus hmcStatus, HearingGetResponse hearingResponse) {
        return !(isHearingUpdated(hmcStatus, hearingResponse) ||isHearingCancelled(hmcStatus, hearingResponse)
            || isStatusException(hmcStatus));
    }

    private boolean isHearingUpdated(HmcStatus hmcStatus, HearingGetResponse hearingResponse) {
        return isHearingListedOrUpdateSubmitted(hmcStatus)
                && isStatusFixed(hearingResponse);
    }

    private boolean isHearingListedOrUpdateSubmitted(HmcStatus hmcStatus) {
        return hmcStatus == LISTED || hmcStatus == AWAITING_LISTING || hmcStatus == UPDATE_SUBMITTED;
    }

    private boolean isStatusFixed(HearingGetResponse hearingResponse) {
        return FIXED == hearingResponse.getHearingResponse().getListingStatus();
    }

    private boolean isHearingCancelled(HmcStatus hmcStatus, HearingGetResponse hearingResponse) {
        return hmcStatus == CANCELLED
                || nonNull(hearingResponse.getHearingResponse().getHearingCancellationReason());
    }

    private boolean isStatusException(HmcStatus hmcStatus) {
        return hmcStatus == EXCEPTION;
    }
}
