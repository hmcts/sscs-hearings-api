package uk.gov.hmcts.reform.sscs.service.hmc.topic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.State;
import uk.gov.hmcts.reform.sscs.exception.InvalidHmcMessageException;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.exception.MessageProcessingException;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.ListAssistCaseStatus;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.reference.data.model.CancellationReason;

import javax.validation.Valid;

import static java.util.Objects.isNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseStateUpdateService {

    private final HearingUpdateService hearingUpdateService;

    public void updateListed(HearingGetResponse hearingResponse, @Valid SscsCaseData sscsCaseData)
            throws MessageProcessingException, InvalidMappingException {
        ListAssistCaseStatus listAssistCaseStatus = hearingResponse.getHearingResponse().getListAssistCaseStatus();

        State state = mapHmcCreatedOrUpdatedToCcd(listAssistCaseStatus, sscsCaseData.getCcdCaseId());
        setState(sscsCaseData, state);

        hearingUpdateService.updateHearing(hearingResponse, sscsCaseData);
    }

    public void updateCancelled(HearingGetResponse hearingResponse, @Valid SscsCaseData sscsCaseData)
            throws InvalidHmcMessageException {
        CancellationReason cancellationReason = hearingResponse.getHearingResponse().getHearingCancellationReason();

        State state = mapHmcCancelledToCcdState(cancellationReason, sscsCaseData.getCcdCaseId());
        setState(sscsCaseData, state);
    }

    public void updateFailed(@Valid SscsCaseData sscsCaseData) {
        setState(sscsCaseData, State.HANDLING_ERROR);
    }

    private void setState(SscsCaseData sscsCaseData, State state) {
        sscsCaseData.setState(state);
        log.info("CCD state has been updated to {} for caseId {}", state, sscsCaseData.getCcdCaseId());
    }

    private State mapHmcCreatedOrUpdatedToCcd(ListAssistCaseStatus listAssistCaseStatus, String caseId)
            throws InvalidHmcMessageException {
        if (isNull(listAssistCaseStatus) || isNull(listAssistCaseStatus.getCaseStateUpdate())) {
            throw new InvalidHmcMessageException(String.format("Can not map listing Case Status %s for Case ID %s",
                    listAssistCaseStatus, caseId));
        }
        return listAssistCaseStatus.getCaseStateUpdate();
    }

    private State mapHmcCancelledToCcdState(CancellationReason cancellationReason, String caseId)
            throws InvalidHmcMessageException {
        if (isNull(cancellationReason)) {
            throw new InvalidHmcMessageException(String.format("Can not map cancellation reason %s for Case ID %s",
                    cancellationReason, caseId));
        }

        return cancellationReason.getCaseStateUpdate();
    }
}
