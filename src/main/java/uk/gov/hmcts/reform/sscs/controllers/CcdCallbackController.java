package uk.gov.hmcts.reform.sscs.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sscs.ccd.callback.Callback;
import uk.gov.hmcts.reform.sscs.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.sscs.ccd.deserialisation.SscsCaseCallbackDeserializer;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.model.CallbackResponse;
import uk.gov.hmcts.reform.sscs.ccd.service.CallbackDispatcher;

import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.sscs.ccd.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.sscs.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.sscs.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.sscs.ccd.callback.CallbackType.SUBMITTED;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CcdCallbackController {
    private final CallbackDispatcher<SscsCaseData> dispatcher;
    private final SscsCaseCallbackDeserializer deserializer;

    @PostMapping(path = "/ccdAboutToStart", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CallbackResponse<SscsCaseData>> ccdAboutToStart(
        @RequestBody String message) {
        Callback<SscsCaseData> callback = deserializer.deserialize(message);
        log.info("Hearings API About to Start CCD Callback `{}` received for Case ID `{}`", callback.getEvent(),
            callback.getCaseDetails().getId());
        return performRequest(ABOUT_TO_START, callback);
    }

    @PostMapping(path = "/ccdAboutToSubmit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CallbackResponse<SscsCaseData>> ccdAboutToSubmit(
        @RequestBody String message) {
        Callback<SscsCaseData> callback = deserializer.deserialize(message);
        log.info("Hearings API About to Submit CCD Callback `{}` received for Case ID `{}`", callback.getEvent(),
            callback.getCaseDetails().getId());
        return performRequest(ABOUT_TO_SUBMIT, callback);
    }

    @PostMapping(path = "/ccdMidEvent", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CallbackResponse<SscsCaseData>> ccdMidEvent(
            @RequestBody String message,
            @RequestParam(value = "pageId", required = false, defaultValue = "") String pageId) {
        Callback<SscsCaseData> callback = deserializer.deserialize(message);
        callback.setPageId(pageId);
        log.info("Hearings API Mid Event CCD Callback `{}` on page `{}` received for Case ID `{}`",
            callback.getEvent(),
            callback.getPageId(), callback.getCaseDetails().getId());

        return performRequest(MID_EVENT, callback);
    }

    @PostMapping(path = "/ccdSubmittedEvent", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CallbackResponse<SscsCaseData>> ccdSubmittedEvent(
        @RequestBody String message) {
        Callback<SscsCaseData> callback = deserializer.deserialize(message);
        log.info("Hearings API Submitted CCD Callback for`{}` event and Case ID `{}`", callback.getEvent(),
            callback.getCaseDetails().getId());
        return performRequest(SUBMITTED, callback);
    }

    private ResponseEntity<CallbackResponse<SscsCaseData>> performRequest(CallbackType callbackType, Callback<SscsCaseData> callback) {

        CallbackResponse<SscsCaseData> callbackResponse = dispatcher.handle(callbackType, callback);

        log.info("Sscs Case CCD callback `{}` handled for Case ID `{}`", callback.getEvent(), callback.getCaseDetails().getId());

        return ok(callbackResponse);
    }
}
