package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.reference.data.model.CancellationReason;

import java.util.Arrays;
import java.util.function.Function;

public final class HearingsEventMappers {

    private HearingsEventMappers() {
    }

    public static Function<HearingGetResponse, EventType> dormantHandler() {
        return response -> Arrays.asList(CancellationReason.WITHDRAWN, CancellationReason.STRUCK_OUT,
            CancellationReason.LAPSED).contains(response.getHearingResponse()
            .getHearingCancellationReason()) ? EventType.DORMANT : null;
    }
}
