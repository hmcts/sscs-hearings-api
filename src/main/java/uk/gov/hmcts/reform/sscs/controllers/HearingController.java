package uk.gov.hmcts.reform.sscs.controllers;

import java.util.HashMap;
import java.util.Map;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sscs.ccd.callback.Callback;
import uk.gov.hmcts.reform.sscs.ccd.deserialisation.SscsCaseCallbackDeserializer;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.domain.SscsCaseDataWrapper;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.service.AuthorisationService;
import uk.gov.hmcts.reform.sscs.service.HearingService;

import static org.springframework.http.ResponseEntity.status;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@Slf4j
public class HearingController {

    private final HearingService hearingService;
    private final AuthorisationService authorisationService;
    private final CcdService ccdService;
    private final SscsCaseCallbackDeserializer deserializer;
    private final IdamService idamService;

    @Autowired
    public HearingController(HearingService hearingService,
                             AuthorisationService authorisationService,
                             CcdService ccdService,
                             SscsCaseCallbackDeserializer deserializer,
                             IdamService idamService) {
        this.hearingService = hearingService;
        this.authorisationService = authorisationService;
        this.ccdService = ccdService;
        this.deserializer = deserializer;
        this.idamService = idamService;
    }

    @PostMapping(value = "/send", produces = APPLICATION_JSON_VALUE)
    public void sendNotification(
            @RequestHeader(AuthorisationService.SERVICE_AUTHORISATION_HEADER) String serviceAuthHeader,
            @RequestBody String message) {
        try {
            Callback<SscsCaseData> callback = deserializer.deserialize(message);

            CaseDetails<SscsCaseData> caseDetailsBefore = callback.getCaseDetailsBefore().orElse(null);

            SscsCaseDataWrapper sscsCaseDataWrapper = buildSscsCaseDataWrapper(
                callback.getCaseDetails().getCaseData(),
                caseDetailsBefore != null ? caseDetailsBefore.getCaseData() : null,
                callback.getEvent());

            log.info("Ccd Response received for case id: {} , {}",
                sscsCaseDataWrapper.getNewSscsCaseData().getCcdCaseId(),
                sscsCaseDataWrapper.getEventType());

            callback.getCaseDetails().getCreatedDate();
            authorisationService.authorise(serviceAuthHeader);
            hearingService.doSomething(sscsCaseDataWrapper);
        } catch (Exception e) {
            log.info("Exception thrown", e);
            throw e;
        }
    }
    @ApiResponses(value =
        {@ApiResponse(code = 200, message = "Updated draft appeal successfully", response = SscsCaseData.class)})
    @GetMapping(value = "/test", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> test(
        @RequestParam(value = "caseIdIn") String caseIdIn,
        @RequestParam(value = "eventIdIn") String eventIdIn) {
        try {
            long caseId = Long.parseLong(caseIdIn);
            log.info("Case id: {}", caseId);

            IdamTokens idamTokens = idamService.getIdamTokens();

            SscsCaseDetails caseDetails = ccdService.getByCaseId(caseId, idamTokens);

            if (caseDetails != null) {

                SscsCaseDataWrapper sscsCaseDataWrapper = buildSscsCaseDataWrapper(
                    null,
                    caseDetails.getData(),
                    EventType.getEventTypeByCcdType(eventIdIn));

                log.info("Ccd Response received for case id: {} , {}",
                    sscsCaseDataWrapper.getOldSscsCaseData().getCcdCaseId(),
                    sscsCaseDataWrapper.getEventType());
                Map<String, String> out = new HashMap<>();
                SscsCaseData caseData = sscsCaseDataWrapper.getOldSscsCaseData();
                out.put("getCaseCreated", caseData.getCaseCreated());
                out.put("getFullName", caseData.getAppeal().getAppellant().getName().getFullName());
                out.put("getWantsHearingTypeFaceToFace", caseData.getAppeal().getHearingSubtype().getWantsHearingTypeFaceToFace());

                return status(HttpStatus.OK).body(out);
            } else {
                log.warn("Case id: {} could not be found for event: {}", caseId, eventIdIn);
            }
        } catch (Exception exc) {
            log.error("Failed to process job for case [" + caseIdIn + "] and event [" + eventIdIn + "]",
                exc);
        }
        return ResponseEntity.noContent().build();
    }

    private SscsCaseDataWrapper buildSscsCaseDataWrapper(SscsCaseData newCaseData,
                                                         SscsCaseData oldCaseData,
                                                         EventType event) {
        return SscsCaseDataWrapper.builder()
                .newSscsCaseData(newCaseData)
                .oldSscsCaseData(oldCaseData)
                .eventType(event)
                .build();
    }

}
