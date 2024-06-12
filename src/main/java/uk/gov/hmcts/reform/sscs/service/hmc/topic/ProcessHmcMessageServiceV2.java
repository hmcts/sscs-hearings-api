package uk.gov.hmcts.reform.sscs.service.hmc.topic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.DwpState;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.UpdateCcdCaseService;
import uk.gov.hmcts.reform.sscs.ccd.service.UpdateCcdCaseService.DynamicEventUpdateResult;
import uk.gov.hmcts.reform.sscs.exception.CaseException;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.exception.MessageProcessingException;
import uk.gov.hmcts.reform.sscs.helper.processing.ProcessHmcMessageHelper;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.model.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.service.HmcHearingApiService;

import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Objects.isNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessHmcMessageServiceV2 {

    private final HmcHearingApiService hmcHearingApiService;
    private final HearingUpdateService hearingUpdateService;
    private final UpdateCcdCaseService updateCcdCaseService;
    private final IdamService idamService;

    public void processEventMessageV2(HmcMessage hmcMessage)
        throws CaseException, MessageProcessingException {

        Long caseId = hmcMessage.getCaseId();
        String hearingId = hmcMessage.getHearingId();

        HearingGetResponse hearingResponse = hmcHearingApiService.getHearingRequest(hearingId);

        HmcStatus hmcMessageStatus = hmcMessage.getHearingUpdate().getHmcStatus();

        if (ProcessHmcMessageHelper.stateNotHandled(hmcMessageStatus, hearingResponse)) {
            log.info("CCD state has not been updated for the Hearing ID {} and Case ID {}",
                     hearingId, caseId
            );
            return;
        }

        log.info("Processing message for HMC status {} with cancellation reasons {} for the Hearing ID {} and Case ID"
                     + " {}",
                 hmcMessageStatus, hearingResponse.getRequestDetails().getCancellationReasonCodes(),
                 hearingId, caseId
        );

        Function<SscsCaseDetails, DynamicEventUpdateResult> mutator = sscsCaseDetails -> {

            SscsCaseData sscsCaseData = sscsCaseDetails.getData();

            try {
                updateCaseDataMutator(sscsCaseData, hmcMessageStatus, hearingResponse, hearingId);
            } catch (MessageProcessingException | InvalidMappingException e) {
                throw new RuntimeException(e);
            }

            return resolveEventType(sscsCaseData, hmcMessageStatus, hearingResponse, hearingId);

        };

        IdamTokens idamTokens = idamService.getIdamTokens();

        updateCcdCaseService.updateCaseV2DynamicEvent(caseId, idamTokens, mutator);

        log.info(
            "Hearing message {} processed for case reference {}",
            hmcMessage.getHearingId(),
            hmcMessage.getCaseId()
        );
    }

    private void updateCaseDataMutator(SscsCaseData caseData, HmcStatus hmcMessageStatus, HearingGetResponse hearingResponse, String hearingId) throws MessageProcessingException, InvalidMappingException {
        DwpState resolvedState = hearingUpdateService.resolveDwpState(hmcMessageStatus);
        if (resolvedState != null) {
            caseData.setDwpState(resolvedState);
        }
        if (ProcessHmcMessageHelper.isHearingUpdated(hmcMessageStatus, hearingResponse)) {
            hearingUpdateService.updateHearing(hearingResponse, caseData);
        }

        hearingUpdateService.setHearingStatus(hearingId, caseData, hmcMessageStatus);

        hearingUpdateService.setWorkBasketFields(hearingId, caseData, hmcMessageStatus);
    }

    private static DynamicEventUpdateResult resolveEventType(SscsCaseData caseData, HmcStatus hmcMessageStatus, HearingGetResponse hearingResponse, String hearingId) {
        BiFunction<HearingGetResponse, SscsCaseData, EventType> eventMapper = hmcMessageStatus.getEventMapper();
        log.info("PostponementRequest {}", caseData.getPostponement());

        DynamicEventUpdateResult noEventResult = new DynamicEventUpdateResult("", "", false, null);

        if (isNull(eventMapper)) {
            log.info("Case has not been updated for HMC Status {} with null eventMapper for the Case ID {}",
                     hmcMessageStatus, caseData.getCcdCaseId());
            return noEventResult;
        }

        EventType eventType = eventMapper.apply(hearingResponse, caseData);
        if (isNull(eventType)) {
            log.info("Case has not been updated for HMC Status {} with null eventType for the Case ID {}",
                     hmcMessageStatus, caseData.getCcdCaseId());
            return noEventResult;
        }

        String ccdUpdateDescription = String.format(hmcMessageStatus.getCcdUpdateDescription(), hearingId);

        return new DynamicEventUpdateResult(hmcMessageStatus.getCcdUpdateSummary(), ccdUpdateDescription, true, eventType.getCcdType());
    }

//    private boolean stateNotHandled(HmcStatus hmcStatus, HearingGetResponse hearingResponse) {
//        return !(isHearingUpdated(hmcStatus, hearingResponse) || isHearingCancelled(hmcStatus, hearingResponse)
//            || isStatusException(hmcStatus));
//    }
//
//    private boolean isHearingUpdated(HmcStatus hmcStatus, HearingGetResponse hearingResponse) {
//        return isHearingListedOrUpdateSubmitted(hmcStatus)
//            && isStatusFixed(hearingResponse);
//    }
//
//    private boolean isHearingListedOrUpdateSubmitted(HmcStatus hmcStatus) {
//        return hmcStatus == LISTED || hmcStatus == AWAITING_LISTING || hmcStatus == UPDATE_SUBMITTED;
//    }
//
//    private boolean isStatusFixed(HearingGetResponse hearingResponse) {
//        return FIXED == hearingResponse.getHearingResponse().getListingStatus();
//    }
//
//    private boolean isHearingCancelled(HmcStatus hmcStatus, HearingGetResponse hearingResponse) {
//        return hmcStatus == CANCELLED
//            || isNotEmpty(hearingResponse.getRequestDetails().getCancellationReasonCodes())
//            || CNCL == hearingResponse.getHearingResponse().getListingStatus();
//    }
//
//    private boolean isStatusException(HmcStatus hmcStatus) {
//        return hmcStatus == EXCEPTION;
//    }
}
