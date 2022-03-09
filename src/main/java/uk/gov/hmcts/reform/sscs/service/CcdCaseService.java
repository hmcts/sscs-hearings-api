package uk.gov.hmcts.reform.sscs.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
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
}
