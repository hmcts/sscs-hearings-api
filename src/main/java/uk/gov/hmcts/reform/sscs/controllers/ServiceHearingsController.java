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
import uk.gov.hmcts.reform.sscs.exception.AuthorisationException;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.InvalidHeaderException;
import uk.gov.hmcts.reform.sscs.mappers.ServiceHearingValuesMapper;
import uk.gov.hmcts.reform.sscs.model.servicehearingvalues.ServiceHearingValues;
import uk.gov.hmcts.reform.sscs.model.single.hearing.ServiceHearingValues;
import uk.gov.hmcts.reform.sscs.service.AuthorisationService;
import uk.gov.hmcts.reform.sscs.service.CcdCaseService;

import static org.springframework.http.ResponseEntity.status;
import static uk.gov.hmcts.reform.sscs.service.AuthorisationService.SERVICE_AUTHORISATION_HEADER;

@RestController
@Slf4j
public class ServiceHearingsController {

    private final AuthorisationService authorisationService;
    private final CcdCaseService ccdCaseService;
    private final ServiceHearingValuesMapper serviceHearingValuesMapper;

    @Autowired
    public ServiceHearingsController(
        AuthorisationService authorisationService,
        CcdCaseService ccdCaseService,
        ServiceHearingValuesMapper serviceHearingValuesMapper) {
        this.authorisationService = authorisationService;
        this.ccdCaseService = ccdCaseService;
        this.serviceHearingValuesMapper = serviceHearingValuesMapper;
    }

    @PostMapping("/serviceHearingValues")
    public ResponseEntity<ServiceHearingValues> serviceHearingValues(
        @RequestHeader(SERVICE_AUTHORISATION_HEADER) String serviceAuthHeader,
        @RequestParam("caseReference") String caseReference) {
        try {
            // This is just the skeleton for the serviceHearingValues endpoint and will need to be
            // implemented fully along with this endpoint

            authorisationService.authorise(serviceAuthHeader);

            long caseId = Long.parseLong(caseReference);

            log.info("Retrieving case details using Case id : {}, for use in generating Service Hearing Values",
                    caseId);

            SscsCaseDetails caseDetails = ccdCaseService.getCaseDetails(caseId);

            ServiceHearingValues model = serviceHearingValuesMapper.mapServiceHearingValues(caseDetails);

            return status(HttpStatus.OK).body(model);
            // the following errors are temporary and will need to be implemented fully along with this endpoint
        } catch (GetCaseException exc) {
            log.error("Case not found for case id {}, {}", caseReference, exc);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (InvalidHeaderException exc) {
            log.error("Incorrect service authorisation header format for case id {}, {}", caseReference, exc);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (AuthorisationException exc) {
            log.error("Incorrect service authorisation for case case id {}, {}", caseReference, exc);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (NumberFormatException exc) {
            log.error("Invalid case id format case id case id {}, {}", caseReference, exc);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
