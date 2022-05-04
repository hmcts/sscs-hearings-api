package uk.gov.hmcts.reform.sscs.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.exception.AuthorisationException;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.InvalidHeaderException;
import uk.gov.hmcts.reform.sscs.exception.InvalidIdException;
import uk.gov.hmcts.reform.sscs.helper.mapping.LinkedCasesMapping;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.ServiceHearingValues;
import uk.gov.hmcts.reform.sscs.model.service.linkedcases.LinkedCase;
import uk.gov.hmcts.reform.sscs.model.service.linkedcases.ServiceLinkedCases;
import uk.gov.hmcts.reform.sscs.service.AuthorisationService;
import uk.gov.hmcts.reform.sscs.service.CcdCaseService;

import java.util.List;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY;
import static org.springframework.http.ResponseEntity.status;
import static uk.gov.hmcts.reform.sscs.service.AuthorisationService.SERVICE_AUTHORISATION_HEADER;

@RestController
@Slf4j
public class ServiceHearingsController {

    private final AuthorisationService authorisationService;
    private final CcdCaseService ccdCaseService;

    @Autowired
    public ServiceHearingsController(
        AuthorisationService authorisationService,
        CcdCaseService ccdCaseService) {
        this.authorisationService = authorisationService;
        this.ccdCaseService = ccdCaseService;
    }

    @PostMapping("/serviceHearingValues")
    @Operation(description = "Get Hearing Values for a case")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Hearing Values Generated", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = ServiceHearingValues.class)) }),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "403", description = "Incorrect authorisation", content = @Content),
        @ApiResponse(responseCode = "404", description = "Case not found", content = @Content),
    })
    public ResponseEntity<ServiceHearingValues> serviceHearingValues(
            @Parameter(name = "Service Authorisation Token", description = "Service authorisation token to authorise access, must be prefixed with 'Bearer '", in = HEADER, example = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdW", required = true)
            @RequestHeader(SERVICE_AUTHORISATION_HEADER) String serviceAuthHeader,
            @Parameter(name = "Case ID", description = "CCD Case ID of the case the Hearing Values will be generated for", in = QUERY, example = "99250807409918", required = true)
            @RequestParam("caseReference") String caseId)
            throws AuthorisationException, InvalidHeaderException, GetCaseException, InvalidIdException {
        try {
            // TODO This is just the skeleton for the serviceHearingValues endpoint and will need to be implemented fully along with this endpoint

            authorisationService.authorise(serviceAuthHeader);

            log.info("Retrieving case details using Case id : {}, for use in generating Service Hearing Values",
                    caseId);

            SscsCaseDetails caseDetails = ccdCaseService.getCaseDetails(caseId);

            ServiceHearingValues model = ServiceHearingValues.builder()
                    .caseName(caseDetails.getData().getWorkAllocationFields().getCaseNamePublic())
                    .build();

            return status(HttpStatus.OK).body(model);
            // TODO the following errors are temporary and will need to be implemented fully along with this endpoint
        } catch (Exception exc) {
            logException(exc, caseId);
            throw exc;
        }
    }

    @PostMapping("/serviceLinkedCases")
    @Operation(description = "Get linked cases for a Case and it's Hearing")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Linked Cases Returned", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = ServiceLinkedCases.class)) }),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "403", description = "Incorrect authorisation", content = @Content),
        @ApiResponse(responseCode = "404", description = "Case not found", content = @Content),
    })
    public ResponseEntity<ServiceLinkedCases> serviceLinkedCases(
            @Parameter(name = "Service Authorisation Token", description = "Service authorisation token to authorise access, must be prefixed with 'Bearer '", in = HEADER, example = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdW", required = true)
            @RequestHeader(SERVICE_AUTHORISATION_HEADER) String serviceAuthHeader,
            @Parameter(name = "Case ID", description = "CCD Case ID of the case for which linked cases are to be found", in = QUERY, example = "99250807409918", required = true)
            @RequestParam("caseReference") String caseId,
            @Parameter(name = "Hearing ID", description = "Hearing ID related to the Case", in = QUERY, example = "1234", required = false)
            @RequestParam("hearingId") String hearingId)
            throws AuthorisationException, InvalidHeaderException, GetCaseException, InvalidIdException {
        try {

            authorisationService.authorise(serviceAuthHeader);

            log.info("Retrieving case details using Case id : {}, for use in generating Service Hearing Values", caseId);

            SscsCaseData caseData = ccdCaseService.getCaseDetails(caseId).getData();

            List<LinkedCase> linkedCases = LinkedCasesMapping.getLinkedCases(caseData);

            ServiceLinkedCases model = ServiceLinkedCases.builder()
                    .linkedCases(linkedCases)
                    .build();

            return status(HttpStatus.OK).body(model);
        } catch (Exception exc) {
            logException(exc, caseId);
            throw exc;
        }
    }

    private void logException(Exception exc, String caseId) {
        if (exc instanceof GetCaseException) {
            log.error("Case not found for case id {}, {}", caseId, exc);
        }
        if (exc instanceof InvalidHeaderException) {
            log.error("Incorrect service authorisation header format for case id {}, {}", caseId, exc);
        }
        if (exc instanceof AuthorisationException) {
            log.error("Incorrect service authorisation for case case id {}, {}", caseId, exc);
        }
        if (exc instanceof InvalidIdException) {
            log.error("Invalid case id format case id case id {}, {}", caseId, exc);
        }
    }
}
