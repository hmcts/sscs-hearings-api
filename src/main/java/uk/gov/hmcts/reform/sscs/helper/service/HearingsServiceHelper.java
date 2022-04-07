package uk.gov.hmcts.reform.sscs.helper.service;

import uk.gov.hmcts.reform.sscs.ccd.domain.SchedulingAndListingFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;

import java.util.Optional;

public final class HearingsServiceHelper {

    private HearingsServiceHelper() {
    }

    public static String getHearingId(HearingWrapper wrapper) {
        return Optional.of(wrapper)
            .map(HearingWrapper::getUpdatedCaseData)
            .map(SscsCaseData::getSchedulingAndListingFields)
            .map(SchedulingAndListingFields::getActiveHearingId)
            .map(Object::toString).orElse(null);
    }
}
