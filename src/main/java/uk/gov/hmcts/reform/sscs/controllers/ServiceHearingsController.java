package uk.gov.hmcts.reform.sscs.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.exception.AuthorisationException;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.InvalidHeaderException;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.service.AuthorisationService;

import static org.springframework.http.ResponseEntity.status;
import static uk.gov.hmcts.reform.sscs.service.AuthorisationService.SERVICE_AUTHORISATION_HEADER;

@RestController
@Slf4j
public class ServiceHearingsController {

    private final AuthorisationService authorisationService;
    private final CcdService ccdService;
    private final IdamService idamService;

    @Autowired
    public ServiceHearingsController(
        AuthorisationService authorisationService,
        CcdService ccdService,
        IdamService idamService) {
        this.authorisationService = authorisationService;
        this.ccdService = ccdService;
        this.idamService = idamService;
    }

    @PostMapping("/serviceHearingValues")
    public ResponseEntity<String> serviceHearingValues(
        @RequestHeader(SERVICE_AUTHORISATION_HEADER) String serviceAuthHeader,
        @RequestParam("caseReference") String caseReference) {
        try {
            authorisationService.authorise(serviceAuthHeader);

            long caseId = Long.parseLong(caseReference);

            log.info("Retrieving case details using Case id : {}, for use in generating Service Hearing Values",
                    caseId);

            IdamTokens idamTokens = idamService.getIdamTokens();

            SscsCaseDetails caseDetails = ccdService.getByCaseId(caseId, idamTokens);

            if (caseDetails == null) {
                String cause = String.format("The case data for Case id: %s could not be found", caseId);
                throw new GetCaseException(cause);
            }

            return status(HttpStatus.OK).body(caseDetails.getData().getCaseCreated());

        } catch (GetCaseException exc) {
            log.error("Case not found for case id {}, {}", caseReference, exc);
            return status(HttpStatus.NOT_FOUND).body(exc.getMessage());
        } catch (InvalidHeaderException exc) {
            log.error("Incorrect service authorisation header format for case id {}, {}", caseReference, exc);
            return status(HttpStatus.BAD_REQUEST).body(exc.getMessage());
        } catch (AuthorisationException exc) {
            log.error("Incorrect service authorisation for case case id {}, {}", caseReference, exc);
            return status(HttpStatus.FORBIDDEN).body(exc.getMessage());
        } catch (NumberFormatException exc) {
            log.error("Invalid case id format case id case id {}, {}", caseReference, exc);
            return status(HttpStatus.BAD_REQUEST).body(exc.getMessage());
        }
    }



}
