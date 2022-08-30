package uk.gov.hmcts.reform.sscs.ccd.helper;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.sscs.ccd.callback.Callback;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SchedulingAndListingFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.State;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.sscs.ccd.domain.HearingRoute.LIST_ASSIST;

@Slf4j
public final class SscsUtil {

    private SscsUtil() {
        //
    }

    public static <T> List<T> mutableEmptyListIfNull(List<T> list) {
        return Optional.ofNullable(list).orElse(new ArrayList<>());
    }

    public static boolean isSAndLCase(@Valid Callback<SscsCaseData> callback) {
        return Optional.ofNullable(callback.getCaseDetails())
            .map(CaseDetails::getCaseData)
            .map(SscsUtil::isSAndLCase)
            .orElse(false);
    }

    public static boolean isSAndLCase(@Valid SscsCaseData sscsCaseData) {
        return LIST_ASSIST == Optional.ofNullable(sscsCaseData)
            .map(SscsCaseData::getSchedulingAndListingFields)
            .map(SchedulingAndListingFields::getHearingRoute)
            .orElse(null);
    }

    public static boolean isValidCaseState(State state, Collection<State> allowedStates) {
        return  nonNull(state) && allowedStates.contains(state);
    }

    public static boolean isValidEventType(EventType event, Collection<EventType> allowedEvents) {
        return nonNull(event) && allowedEvents.contains(event);
    }
}
