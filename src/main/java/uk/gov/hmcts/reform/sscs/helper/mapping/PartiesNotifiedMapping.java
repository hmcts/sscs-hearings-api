package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.partiesnotified.PartiesNotifiedRequestPayload;
import uk.gov.hmcts.reform.sscs.model.partiesnotified.ServiceData;

import static uk.gov.hmcts.reform.sscs.helper.service.HearingsServiceHelper.getVersion;

public final class PartiesNotifiedMapping {
    private PartiesNotifiedMapping() {
    }

    public static PartiesNotifiedRequestPayload buildUpdatePartiesNotifiedPayload(HearingWrapper wrapper) {
        return PartiesNotifiedRequestPayload.builder()
            .requestVersion(getVersion(wrapper))
            .serviceData(ServiceData.builder()
                //TODO .partyId(partyId)
                //TODO .notificationType(notificationType)
                .build())
            .build();
    }
}
