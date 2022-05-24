package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.SchedulingAndListingFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.partiesnotified.PartiesNotifiedRequest;

import java.util.Optional;

public final class PartiesNotifiedMapping {
    private PartiesNotifiedMapping() {
    }

    public static PartiesNotifiedRequest buildUpdatePartiesNotifiedPayload(HearingWrapper wrapper) {
        return PartiesNotifiedRequest
                .builder()
                .requestVersion(getVersionNumber(wrapper))
                .build();
    }

    public static Long getVersionNumber(HearingWrapper wrapper) {
        return Optional.of(wrapper)
                .map(HearingWrapper::getUpdatedCaseData)
                .map(SscsCaseData::getSchedulingAndListingFields)
                .map(SchedulingAndListingFields::getActiveHearingVersionNumber)
                .orElse(null);
    }
}
