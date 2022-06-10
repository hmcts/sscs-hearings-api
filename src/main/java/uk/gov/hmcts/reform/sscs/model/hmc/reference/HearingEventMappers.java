package uk.gov.hmcts.reform.sscs.model.hmc.reference;

import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.reference.data.model.CancellationReason;

import java.util.List;
import java.util.function.Function;

public class HearingEventMappers {

    private HearingEventMappers() {
    }

    public static Function<HearingGetResponse, EventType> dormantHandler() {
        return response -> List.of(CancellationReason.WITHDRAWN, CancellationReason.STRUCK_OUT,
            CancellationReason.LAPSED).contains(response.getHearingResponse()
            .getHearingCancellationReason()) ? EventType.DORMANT : null;
    }
}
