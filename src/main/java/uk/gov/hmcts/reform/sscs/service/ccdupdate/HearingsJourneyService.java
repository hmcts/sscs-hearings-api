package uk.gov.hmcts.reform.sscs.service.ccdupdate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.model.messaging.HmcMessage;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.service.CcdCaseService;
import uk.gov.hmcts.reform.sscs.service.HmcHearingService;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.sscs.ccd.domain.EventType.NEW_HEARING_BOOKED;
import static uk.gov.hmcts.reform.sscs.ccd.domain.EventType.UPDATE_CASE_ONLY;

@Slf4j
@Service
@RequiredArgsConstructor
public class HearingsJourneyService {

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

    public void process(HmcMessage hmcMessage) throws GetCaseException, UpdateCaseException {
        validateHmcMessage(hmcMessage);

        final String hearingId = hmcMessage.getHearingID();
        HearingGetResponse hearingResponse = hmcHearingService.getHearingRequest(hearingId);
        if (isNull(hearingResponse)) {
            throw new GetCaseException(String.format("Failed to retrieve hearing with Id: %s from HMC", hearingId));
        }

        SscsCaseData caseData = ccdCaseService.getCaseDetails(Long.parseLong(hearingId)).getData();

        switch (hmcMessage.getHearingUpdate().getHmcStatus()) {
            case LISTED:
                ccdStateUpdateService.updateListed(hearingResponse, caseData);
                locationUpdateService.updateVenue(hmcMessage, caseData);
                ccdCaseService.updateCaseData(caseData, NEW_HEARING_BOOKED, CREATED_SUMMARY, CREATED_DESC);
                break;
            case UPDATE_SUBMITTED:
                ccdStateUpdateService.updateListed(hearingResponse, caseData);
                locationUpdateService.updateVenue(hmcMessage, caseData);
                ccdCaseService.updateCaseData(caseData, UPDATE_CASE_ONLY, UPDATED_SUMMARY, UPDATED_DESC);
                break;
            case CANCELLED:
                ccdStateUpdateService.updateCancelled(hearingResponse, caseData);
                ccdCaseService.updateCaseData(caseData, UPDATE_CASE_ONLY, CANCELLED_SUMMARY, CANCELLED_DESC);
                break;
            case EXCEPTION:
                ccdStateUpdateService.updateFailed(caseData);
                ccdCaseService.updateCaseData(caseData, UPDATE_CASE_ONLY, EXCEPTION_SUMMARY, EXCEPTION_DESC);
                break;
            default:
                log.info("CCD state has not been updated for the hearing Id: {}", hearingId);
                break;
        }
    }

    private void validateHmcMessage(HmcMessage hmcMessage) {
        requireNonNull(hmcMessage, "HMC message must not be mull");
        requireNonNull(hmcMessage.getHearingID(), "HMC message field hearingID is missing");
        requireNonNull(hmcMessage.getHearingUpdate().getHmcStatus(), "HMC message field HmcStatus is missing");
    }
}
