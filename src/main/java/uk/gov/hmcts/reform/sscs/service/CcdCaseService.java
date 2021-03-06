package uk.gov.hmcts.reform.sscs.service;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;

@Slf4j
@Service
public class CcdCaseService {

    private final CcdService ccdService;
    private final IdamService idamService;

    @Autowired
    public CcdCaseService(CcdService ccdService, IdamService idamService) {
        this.ccdService = ccdService;
        this.idamService = idamService;
    }

    public SscsCaseDetails getCaseDetails(String caseId) throws GetCaseException {
        return getCaseDetails(parseCaseId(caseId));
    }

    public SscsCaseDetails getCaseDetails(long caseId) throws GetCaseException {

        log.info("Retrieving case details using Case id : {}",
                caseId);

        IdamTokens idamTokens = idamService.getIdamTokens();

        SscsCaseDetails caseDetails = ccdService.getByCaseId(caseId, idamTokens);

        if (caseDetails == null) {
            String cause = String.format("The case data for Case id: %s could not be found", caseId);
            GetCaseException exc = new GetCaseException(cause);
            log.error(cause, exc);
            throw exc;
        }
        return caseDetails;
    }

    public SscsCaseDetails updateCaseData(SscsCaseData caseData, EventType event, String summary, String description)
        throws UpdateCaseException {

        long caseId = parseCaseId(caseData.getCcdCaseId());

        log.info("Updating case data using Case id : {}", caseId);

        IdamTokens idamTokens = idamService.getIdamTokens();

        try {
            return ccdService.updateCase(caseData, caseId, event.getType(), summary, description, idamTokens);
        } catch (FeignException e) {
            UpdateCaseException exc = new UpdateCaseException(
                    String.format("The case with Case id: %s could not be updated with status %s, %s",
                            caseId, e.status(), e));
            log.error(exc.getMessage(), exc);
            throw exc;
        }
    }

    private long parseCaseId(String caseId) {
        try {
            return Long.parseLong(caseId);
        } catch (NumberFormatException e) {
            log.error("Invalid case id {} should be in long format", caseId);
            throw e;
        }
    }
}
