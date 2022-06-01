package uk.gov.hmcts.reform.sscs.service.hmc.topic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.exception.CaseException;
import uk.gov.hmcts.reform.sscs.exception.MessageProcessingException;
import uk.gov.hmcts.reform.sscs.model.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.ListAssistCaseStatus;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.service.CcdCaseService;
import uk.gov.hmcts.reform.sscs.service.HmcHearingApiService;

import static java.util.Objects.nonNull;
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
    private final CaseStateUpdateService caseStateUpdateService;

    public void checkMessage(HmcMessage hmcMessage) throws CaseException, MessageProcessingException {

        Long caseId = hmcMessage.getCaseId();
        ListAssistCaseStatus listAssistCaseStatus = hmcMessage.getHearingUpdate().getListAssistCaseStatus();
        log.info("Attempting to process hearing event {} from hearings event queue for case ID {}",
                listAssistCaseStatus, caseId);


        if (isMessageNotRelevantForService(hmcMessage)) {
            log.info("Message not for this service for hearing ID {} and case reference: {}",
                    hmcMessage.getHearingId(),
                    hmcMessage.getCaseId());
            return;
        }

        processEventMessage(hmcMessage);

        log.info("Hearing message {} processed for case reference {}",
                hmcMessage.getHearingId(),
                hmcMessage.getCaseId());
    }

    public void processEventMessage(HmcMessage hmcMessage)
            throws CaseException, MessageProcessingException {


        final String hearingId = hmcMessage.getHearingId();
        HearingGetResponse hearingResponse = hmcHearingApiService.getHearingRequest(hearingId);

        long caseId = hmcMessage.getCaseId();
        SscsCaseData caseData = ccdCaseService.getCaseDetails(caseId).getData();

        HmcStatus hmcStatus = hmcMessage.getHearingUpdate().getHmcStatus();
        if (isHearingUpdated(hmcStatus, hearingResponse)) {
            caseStateUpdateService.updateListed(hearingResponse, caseData);
        } else if (isHearingCancelled(hmcStatus, hearingResponse)) {
            caseStateUpdateService.updateCancelled(hearingResponse, caseData);
        } else if (isStatusException(hmcStatus)) {
            caseStateUpdateService.updateFailed(caseData);
        } else {
            log.info("CCD state has not been updated for the Hearing ID {} and Case ID {}",
                    hearingId,
                    caseId);
            return;
        }

        String ccdUpdateDescription = String.format(hmcStatus.getCcdUpdateDescription(), hearingId);
        ccdCaseService.updateCaseData(caseData,
                hmcStatus.getCcdUpdateEventType(),
                hmcStatus.getCcdUpdateSummary(),
                ccdUpdateDescription);
    }

    public boolean isMessageNotRelevantForService(HmcMessage hmcMessage) {
        return !sscsServiceCode.equals(hmcMessage.getHmctsServiceCode());
    }

    private boolean isHearingUpdated(HmcStatus hmcStatus, HearingGetResponse hearingResponse) {
        return isHearingListedOrUpdateSubmitted(hmcStatus)
                && isStatusFixed(hearingResponse);
    }

    private boolean isHearingListedOrUpdateSubmitted(HmcStatus hmcStatus) {
        return hmcStatus == LISTED || hmcStatus == UPDATE_SUBMITTED;
    }

    private boolean isStatusFixed(HearingGetResponse hearingResponse) {
        return FIXED == hearingResponse.getHearingResponse().getListingStatus();
    }

    private boolean isHearingCancelled(HmcStatus hmcStatus, HearingGetResponse hearingResponse) {
        return hmcStatus == CANCELLED
                && isHearingGetResponseCancelled(hearingResponse);
    }

    private boolean isHearingGetResponseCancelled(HearingGetResponse hearingResponse) {
        return REQUEST_CANCELLED.equalsIgnoreCase(hearingResponse.getRequestDetails().getStatus())
                || nonNull(hearingResponse.getHearingResponse().getHearingCancellationReason());
    }

    private boolean isStatusException(HmcStatus hmcStatus) {
        return hmcStatus == EXCEPTION;
    }
}
