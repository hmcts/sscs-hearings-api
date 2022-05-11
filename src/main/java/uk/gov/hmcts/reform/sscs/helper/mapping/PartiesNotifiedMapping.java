package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.SchedulingAndListingFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.partiesnotified.PartiesNotified;

import java.util.Optional;

public final class PartiesNotifiedMapping {
    private PartiesNotifiedMapping() {
    }

    public static PartiesNotified buildUpdatePartiesNotifiedPayload(HearingWrapper wrapper) {

        PartiesNotified.PartiesNotifiedBuilder partiesNotifiedBuilder = PartiesNotified.builder();
        partiesNotifiedBuilder.requestVersion(getVersionNumber(wrapper));
        return partiesNotifiedBuilder.build();
    }

    public static String getVersionNumber(HearingWrapper wrapper) {
        return Optional.of(wrapper)
                .map(HearingWrapper::getUpdatedCaseData)
                .map(SscsCaseData::getSchedulingAndListingFields)
                .map(SchedulingAndListingFields::getActiveHearingVersionNumber)
                .map(Object::toString).orElse(null);
    }
}
