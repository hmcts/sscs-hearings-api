package uk.gov.hmcts.reform.sscs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.InvalidIdException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.model.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.service.ccdupdate.CcdLocationUpdateService;
import uk.gov.hmcts.reform.sscs.service.ccdupdate.CcdStateUpdateService;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.sscs.ccd.domain.EventType.HEARING_BOOKED;
import static uk.gov.hmcts.reform.sscs.ccd.domain.EventType.UPDATE_CASE_ONLY;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.CANCELLED;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.EXCEPTION;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.LISTED;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.UPDATE_SUBMITTED;

@Slf4j
@Service
@RequiredArgsConstructor
public class HmcEventTopicService {

    private static final String CREATED_SUMMARY = "SSCS - new case sent to HMC";
    private static final String CREATED_DESC = "SSCS - new case sent to HMC";
    private static final String UPDATED_SUMMARY = "SSCS - updated case sent to HMC";
    private static final String UPDATED_DESC = "SSCS - updated case sent to HMC";
    private static final String CANCELLED_SUMMARY = "SSCS - case cancelled";
    private static final String CANCELLED_DESC = "SSCS - case cancelled";
    private static final String EXCEPTION_SUMMARY = "SSCS - exception occurred";
    private static final String EXCEPTION_DESC = "SSCS - exception occurred";

    private final HmcHearingService hmcHearingService;
    private final CcdCaseService ccdCaseService;
    private final CcdStateUpdateService ccdStateUpdateService;
    private final CcdLocationUpdateService locationUpdateService;

    public void processEventMessage(HmcMessage hmcMessage) throws GetCaseException, UpdateCaseException, InvalidIdException {
        validateHmcMessage(hmcMessage);

        final String hearingId = hmcMessage.getHearingId();
        HearingGetResponse hearingResponse = hmcHearingService.getHearingRequest(hearingId);
        if (isNull(hearingResponse)) {
            throw new GetCaseException(String.format("Failed to retrieve hearing with Id: %s from HMC", hearingId));
        }

        SscsCaseData caseData = ccdCaseService.getCaseDetails(hearingId).getData();

        if (hmcMessage.getHearingUpdate().getHmcStatus() == LISTED) {
            ccdStateUpdateService.updateListed(hearingResponse, caseData);
            locationUpdateService.updateVenue(hmcMessage, caseData);
            ccdCaseService.updateCaseData(caseData, HEARING_BOOKED, CREATED_SUMMARY, CREATED_DESC);
        } else if (hmcMessage.getHearingUpdate().getHmcStatus() == UPDATE_SUBMITTED) {
            ccdStateUpdateService.updateListed(hearingResponse, caseData);
            locationUpdateService.updateVenue(hmcMessage, caseData);
            ccdCaseService.updateCaseData(caseData, UPDATE_CASE_ONLY, UPDATED_SUMMARY, UPDATED_DESC);
        } else if (hmcMessage.getHearingUpdate().getHmcStatus() == CANCELLED) {
            ccdStateUpdateService.updateCancelled(hearingResponse, caseData);
            ccdCaseService.updateCaseData(caseData, UPDATE_CASE_ONLY, CANCELLED_SUMMARY, CANCELLED_DESC);
        } else if (hmcMessage.getHearingUpdate().getHmcStatus() == EXCEPTION) {
            ccdStateUpdateService.updateFailed(caseData);
            ccdCaseService.updateCaseData(caseData, UPDATE_CASE_ONLY, EXCEPTION_SUMMARY, EXCEPTION_DESC);
        }
        log.info("CCD state has not been updated for the Hearing ID {} and Case ID {}",
            hearingId,
            caseData.getCcdCaseId());
    }

    private void validateHmcMessage(HmcMessage hmcMessage) throws UpdateCaseException {
        if (isNull(hmcMessage)) {
            throw new UpdateCaseException("HMC message must not be mull");
        }

        if (isNull(hmcMessage.getHearingId())) {
            throw new UpdateCaseException("HMC message field hearingID is missing");
        }

        if (isNull(hmcMessage.getHearingUpdate().getHmcStatus())) {
            throw new UpdateCaseException("HMC message field HmcStatus is missing");
        }


    }
}
