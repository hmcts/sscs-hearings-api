package uk.gov.hmcts.reform.sscs.helper.service;

import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.HearingEvent;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingResponse;

import java.util.Optional;

public final class HearingsServiceHelper {

    private HearingsServiceHelper() {
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

    public static String getHearingId(HearingWrapper wrapper) {
        return Optional.of(wrapper)
            .map(HearingWrapper::getCaseData)
            .map(SscsCaseData::getSchedulingAndListingFields)
            .map(SchedulingAndListingFields::getActiveHearingId)
            .map(Object::toString).orElse(null);
    }
}
