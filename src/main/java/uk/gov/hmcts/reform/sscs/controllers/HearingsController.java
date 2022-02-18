package uk.gov.hmcts.reform.sscs.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.domain.ServiceHearingValuesWrapper;
import uk.gov.hmcts.reform.sscs.domain.SscsCaseDataWrapper;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.service.AuthorisationService;
import uk.gov.hmcts.reform.sscs.service.HearingsService;

import static org.springframework.http.ResponseEntity.status;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@Slf4j
public class HearingsController {

    private final AuthorisationService authorisationService;
    private final CcdService ccdService;
    private final IdamService idamService;
    private final HearingsService hearingsService;

    @Autowired
    public HearingsController(
        AuthorisationService authorisationService,
        CcdService ccdService,
        IdamService idamService, HearingsService hearingsService) {
        this.authorisationService = authorisationService;
        this.ccdService = ccdService;
        this.idamService = idamService;
        this.hearingsService = hearingsService;
    }

    @GetMapping(value = "/serviceHearingValues", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ServiceHearingValuesWrapper> serviceHearingValues(
        @RequestHeader(AuthorisationService.SERVICE_AUTHORISATION_HEADER) String serviceAuthHeader,
        @RequestParam("caseReference") String caseReference) {
        try {
            authorisationService.authorise(serviceAuthHeader);

            long caseId = Long.parseLong(caseReference);
            log.info("Case id: {}", caseId);

            IdamTokens idamTokens = idamService.getIdamTokens();

            SscsCaseDetails caseDetails = ccdService.getByCaseId(caseId, idamTokens);

            if (caseDetails == null) {
                log.warn("Case id: {} could not be found", caseId);
            } else {
                SscsCaseDataWrapper sscsCaseDataWrapper = hearingsService.buildSscsCaseDataWrapper(
                    null,
                    caseDetails.getData(),
                    null);

                log.info("Ccd Response received for case id: {}",
                    sscsCaseDataWrapper.getOldSscsCaseData().getCcdCaseId());

                SscsCaseData caseData = sscsCaseDataWrapper.getOldSscsCaseData();

                ServiceHearingValuesWrapper serviceHearingValuesWrapper = new ServiceHearingValuesWrapper(
                    caseData.getWorkAllocationFields().getCaseNamePublic(),
                    true,
                    caseData.getAppeal().getHearingType(),
                    caseData.getAppeal().getBenefitType().getDescription(),
                    "");
                return status(HttpStatus.OK).body(serviceHearingValuesWrapper);
            }
        } catch (Exception exc) {
            log.error("Failed to process job for case [" + caseReference + "] ", exc);
        }
        return ResponseEntity.noContent().build();
    }



}
