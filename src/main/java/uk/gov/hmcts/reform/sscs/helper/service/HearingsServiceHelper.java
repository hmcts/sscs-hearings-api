package uk.gov.hmcts.reform.sscs.helper.service;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.model.HearingEvent;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequest;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingResponse;
import uk.gov.hmcts.reform.sscs.service.CcdCaseService;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static java.util.Objects.isNull;

public final class HearingsServiceHelper {


    private static CcdCaseService caseService;

    private HearingsServiceHelper() {
    }

    @Autowired
    public static void setCaseService(CcdCaseService caseService) {
        HearingsServiceHelper.caseService = caseService;
    }

    public static void updateHearingId(HearingWrapper wrapper, HearingResponse response) {
        wrapper.getCaseData().getSchedulingAndListingFields().setActiveHearingId(response.getHearingRequestId());
    }

    public static void updateVersionNumber(HearingWrapper wrapper, HearingResponse response) {
        wrapper.getCaseData().getSchedulingAndListingFields().setActiveHearingVersionNumber(response.getVersionNumber());
    }

    public static HearingEvent getHearingEvent(HearingState state) {
        return HearingEvent.valueOf(state.name());
    }

    public static HearingEvent addEvent(HearingWrapper wrapper) {
        HearingEvent hearingEvent = getHearingEvent(wrapper.getState());
        SscsCaseData caseData = wrapper.getCaseData();
        if (isNull(caseData.getEvents())) {
            caseData.setEvents(new ArrayList<>());
        }
        Event event = Event.builder()
                .value(EventDetails.builder()
                        .type(hearingEvent.getEventType().getType())
                        .date(LocalDateTime.now().toString())
                        .description(hearingEvent.getDescription())
                        .build())
                .build();

        caseData.getEvents().add(event);

        return hearingEvent;
    }

    public static HearingWrapper createWrapper(HearingRequest hearingRequest) throws GetCaseException {

        return HearingWrapper.builder()
                .caseData(caseService.getCaseDetails(hearingRequest.getCcdCaseId()).getData())
                .state(hearingRequest.getHearingState())
                .build();
    }
}
