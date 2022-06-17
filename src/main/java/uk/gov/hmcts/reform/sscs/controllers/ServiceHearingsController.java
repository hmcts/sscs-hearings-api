package uk.gov.hmcts.reform.sscs.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.model.service.ServiceHearingRequest;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.ServiceHearingValues;
import uk.gov.hmcts.reform.sscs.model.service.linkedcases.ServiceLinkedCases;
import uk.gov.hmcts.reform.sscs.service.ServiceHearingsService;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER;
import static org.springframework.http.ResponseEntity.status;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ServiceHearingsController {

    private final ServiceHearingsService serviceHearingsService;


    @PostMapping("/serviceHearingValues")
    @Operation(description = "Get Hearing Values for a case")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Hearing Values Generated", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = ServiceHearingValues.class)) }),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "403", description = "Incorrect authorisation", content = @Content),
        @ApiResponse(responseCode = "404", description = "Case not found", content = @Content),
    })
    @Parameter(name = "ServiceAuthorization", description = "Service authorisation token to authorise access, must be prefixed with 'Bearer '", in = HEADER, example = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdW", required = true)
    public ResponseEntity<ServiceHearingValues> serviceHearingValues(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "CCD Case ID and Hearing ID (could be null, empty string or missing) of the case the Hearing Values will be generated for", required = true,
                    content = @Content(schema = @Schema(implementation = ServiceHearingRequest.class, example = "{ \n  \"caseReference\": \"1234123412341234\",\n  \"hearingId\": \"123123123\"\n}")))
            @RequestBody ServiceHearingRequest request) throws GetCaseException, UpdateCaseException, InvalidMappingException {
        try {
            log.info("Retrieving case details using Case id : {}, for use in generating Service Hearing Values",
                    request.getCaseId());

            ServiceHearingValues model = serviceHearingsService.getServiceHearingValues(request);
            return status(HttpStatus.OK).body(model);
        } catch (Exception exc) {
            logException(exc, request.getCaseId());
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
    @Parameter(name = "ServiceAuthorization", description = "Service authorisation token to authorise access, must be prefixed with 'Bearer '", in = HEADER, example = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdW", required = true)
    public ResponseEntity<ServiceLinkedCases> serviceLinkedCases(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "CCD Case ID and Hearing ID (could be null, empty string or missing) of the case the Linked Cases will be found", required = true,
                    content = @Content(schema = @Schema(implementation = ServiceHearingRequest.class, example = "{ \n  \"caseReference\": \"1234123412341234\",\n  \"hearingId\": \"123123123\"\n}")))
            @RequestBody ServiceHearingRequest request)
        throws GetCaseException, UpdateCaseException, InvalidMappingException {
        try {
            log.info("Retrieving case details using Case id : {}, for use in generating Service Linked Cases",
                    request.getCaseId());

            ServiceLinkedCases model = serviceHearingsService.getServiceLinkedCases(request);

            return status(HttpStatus.OK).body(model);
        } catch (Exception exc) {
            logException(exc, request.getCaseId());
            throw exc;
        }
    }


    private void logException(Exception exc, String caseId) {
        if (exc instanceof GetCaseException) {
            log.error("Case not found for case id {}, {}", caseId, exc);
        }
        if (exc instanceof UpdateCaseException) {
            log.error("Error updating case id {}, {}", caseId, exc);
        }
    }
}
