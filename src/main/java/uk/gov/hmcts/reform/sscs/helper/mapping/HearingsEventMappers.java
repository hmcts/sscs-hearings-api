package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.reference.data.model.CancellationReason;

import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.sscs.ccd.domain.State.NOT_LISTABLE;
import static uk.gov.hmcts.reform.sscs.ccd.domain.State.READY_TO_LIST;
import static uk.gov.hmcts.reform.sscs.reference.data.model.CancellationReason.LAPSED;
import static uk.gov.hmcts.reform.sscs.reference.data.model.CancellationReason.STRUCK_OUT;
import static uk.gov.hmcts.reform.sscs.reference.data.model.CancellationReason.WITHDRAWN;

public final class HearingsEventMappers {

    public static final List<CancellationReason> DORMANT_CANCELLATION_REASONS = List.of(WITHDRAWN, STRUCK_OUT, LAPSED);

    private HearingsEventMappers() {
    }

    public static EventType cancelledHandler(HearingGetResponse response, SscsCaseData caseData) {
        if (shouldCaseBeDormant(response)) {
            return EventType.DORMANT;
        } else if (shouldCaseBeRelisted(caseData)) {
            return EventType.READY_TO_LIST;
        } else if (shouldCaseBeNotListable(caseData)) {
            return EventType.UPDATE_NOT_LISTABLE;
        } else {
            return null;
        }
    }

    public static boolean shouldCaseBeRelisted(SscsCaseData caseData) {
        return HearingsWindowMapping.isCasePostponed(caseData)
            && READY_TO_LIST.getId().equals(caseData.getPostponementRequest().getListingOption());
    }

    public static boolean shouldCaseBeNotListable(SscsCaseData caseData) {
        return HearingsWindowMapping.isCasePostponed(caseData)
            && NOT_LISTABLE.getId().equals(caseData.getPostponementRequest().getListingOption());
    }

    public static boolean shouldCaseBeDormant(HearingGetResponse response) {
        return nonNull(response.getHearingResponse().getHearingCancellationReason())
            && DORMANT_CANCELLATION_REASONS.contains(response.getHearingResponse().getHearingCancellationReason());
    }
}
