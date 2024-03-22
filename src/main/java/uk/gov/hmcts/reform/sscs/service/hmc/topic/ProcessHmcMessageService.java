package uk.gov.hmcts.reform.sscs.service.hmc.topic;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.DwpState;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.service.UpdateCcdCaseService;
import uk.gov.hmcts.reform.sscs.ccd.service.UpdateCcdCaseService.UpdateResult;
import uk.gov.hmcts.reform.sscs.exception.CaseException;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.exception.MessageProcessingException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.model.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.service.CcdCaseService;
import uk.gov.hmcts.reform.sscs.service.HmcHearingApiService;

import java.util.Optional;
import java.util.function.BiFunction;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.AWAITING_LISTING;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.CANCELLED;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.EXCEPTION;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.LISTED;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.UPDATE_SUBMITTED;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.ListingStatus.CNCL;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.ListingStatus.FIXED;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessHmcMessageService {

    private final HmcHearingApiService hmcHearingApiService;

    private final CcdCaseService ccdCaseService;

    private final HearingUpdateService hearingUpdateService;
    private final UpdateCcdCaseService updateCcdCaseService;
    private final IdamService idamService;

    public void processEventMessageV2(HmcMessage hmcMessage)
        throws CaseException, MessageProcessingException {

        Long caseId = hmcMessage.getCaseId();
        String hearingId = hmcMessage.getHearingId();

        HearingGetResponse hearingResponse = hmcHearingApiService.getHearingRequest(hearingId);

        HmcStatus hmcMessageStatus = hmcMessage.getHearingUpdate().getHmcStatus();

        if (stateNotHandled(hmcMessageStatus, hearingResponse)) {
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

        SscsCaseData temporaryCaseData = ccdCaseService.getCaseDetails(caseId).getData();

        Optional<EventType> eventType = resolveEventType(hearingResponse, hmcMessageStatus, temporaryCaseData);
        if (eventType.isEmpty()) {
            return;
        }

        updateCcdCaseService.updateCaseV2(
            caseId,
            eventType.get().getCcdType(),
            idamService.getIdamTokens(),
            sscsCaseData -> mutatorFunction(sscsCaseData, hmcMessageStatus, hearingResponse, hearingId)
        );
    }

    @SneakyThrows
    private UpdateResult mutatorFunction(SscsCaseData sscsCaseData, HmcStatus hmcMessageStatus, HearingGetResponse hearingResponse, String hearingId) {
        String ccdUpdateDescription = updateCaseDataAndGetDescription(hmcMessageStatus, sscsCaseData, hearingResponse, hearingId);

        return new UpdateResult(
            hmcMessageStatus.getCcdUpdateSummary(),
            ccdUpdateDescription
        );
    }

    public void processEventMessage(HmcMessage hmcMessage)
        throws CaseException, MessageProcessingException {

        Long caseId = hmcMessage.getCaseId();
        String hearingId = hmcMessage.getHearingId();

        HearingGetResponse hearingResponse = hmcHearingApiService.getHearingRequest(hearingId);

        HmcStatus hmcMessageStatus = hmcMessage.getHearingUpdate().getHmcStatus();

        if (stateNotHandled(hmcMessageStatus, hearingResponse)) {
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

        SscsCaseData caseData = ccdCaseService.getCaseDetails(caseId).getData();

        String ccdUpdateDescription = updateCaseDataAndGetDescription(hmcMessageStatus, caseData, hearingResponse, hearingId);

        resolveEventAndUpdateCase(hearingResponse, hmcMessageStatus, caseData, ccdUpdateDescription);

        log.info(
            "Hearing message {} processed for case reference {}",
            hmcMessage.getHearingId(),
            hmcMessage.getCaseId()
        );
    }

    private String updateCaseDataAndGetDescription(HmcStatus hmcMessageStatus, SscsCaseData caseData, HearingGetResponse hearingResponse, String hearingId) throws MessageProcessingException, InvalidMappingException {
        DwpState resolvedState = hearingUpdateService.resolveDwpState(hmcMessageStatus);
        if (resolvedState != null) {
            caseData.setDwpState(resolvedState);
        }
        if (isHearingUpdated(hmcMessageStatus, hearingResponse)) {
            hearingUpdateService.updateHearing(hearingResponse, caseData);
        }

        hearingUpdateService.setHearingStatus(hearingId, caseData, hmcMessageStatus);

        hearingUpdateService.setWorkBasketFields(hearingId, caseData, hmcMessageStatus);

        return String.format(hmcMessageStatus.getCcdUpdateDescription(), hearingId);
    }

    private void resolveEventAndUpdateCase(HearingGetResponse hearingResponse, HmcStatus hmcStatus, SscsCaseData caseData,
                                           String ccdUpdateDescription) throws UpdateCaseException {

        Optional<EventType> eventType = resolveEventType(hearingResponse, hmcStatus, caseData);
        if (eventType.isEmpty()) {
            return;
        }

        ccdCaseService.updateCaseData(
            caseData,
            eventType.get(),
            hmcStatus.getCcdUpdateSummary(),
            ccdUpdateDescription
        );
    }

    private static Optional<EventType> resolveEventType(HearingGetResponse hearingResponse, HmcStatus hmcStatus, SscsCaseData caseData) {
        BiFunction<HearingGetResponse, SscsCaseData, EventType> eventMapper = hmcStatus.getEventMapper();
        log.info("PostponementRequest {}", caseData.getPostponement());

        if (isNull(eventMapper)) {
            log.info("Case has not been updated for HMC Status {} with null eventMapper for the Case ID {}",
                     hmcStatus, caseData.getCcdCaseId());
            return Optional.empty();
        }

        EventType eventType = eventMapper.apply(hearingResponse, caseData);
        if (isNull(eventType)) {
            log.info("Case has not been updated for HMC Status {} with null eventType for the Case ID {}",
                     hmcStatus, caseData.getCcdCaseId());
            return Optional.empty();
        }
        return Optional.of(eventType);
    }

    private boolean stateNotHandled(HmcStatus hmcStatus, HearingGetResponse hearingResponse) {
        return !(isHearingUpdated(hmcStatus, hearingResponse) || isHearingCancelled(hmcStatus, hearingResponse)
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
            || isNotEmpty(hearingResponse.getRequestDetails().getCancellationReasonCodes())
            || CNCL == hearingResponse.getHearingResponse().getListingStatus();
    }

    private boolean isStatusException(HmcStatus hmcStatus) {
        return hmcStatus == EXCEPTION;
    }
}
