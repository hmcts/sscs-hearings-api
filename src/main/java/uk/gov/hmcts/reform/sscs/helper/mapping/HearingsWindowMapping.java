package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.OverrideFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.helper.adjournment.AdjournmentCalculateDateHelper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingWindow;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.validation.Valid;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;

public final class HearingsWindowMapping {

    public static final int DAYS_TO_ADD_HEARING_WINDOW_DWP_RESPONDED = 28;
    public static final int DAYS_TO_ADD_HEARING_WINDOW_TODAY_POSTPONEMENT = 14;
    public static final int DAYS_TO_ADD_HEARING_WINDOW_TODAY = 1;

    private HearingsWindowMapping() {
    }

    public static HearingWindow buildHearingWindow(@Valid SscsCaseData caseData, ReferenceDataServiceHolder refData) {
        OverrideFields overrideFields = OverridesMapping.getOverrideFields(caseData);
        uk.gov.hmcts.reform.sscs.ccd.domain.HearingWindow overrideWindow = overrideFields.getHearingWindow();

        if (nonNull(overrideWindow)
            && (nonNull(overrideWindow.getFirstDateTimeMustBe())
            || nonNull(overrideWindow.getDateRangeStart())
            || nonNull(overrideWindow.getDateRangeEnd()))) {
            return HearingWindow.builder()
                .firstDateTimeMustBe(overrideWindow.getFirstDateTimeMustBe())
                .dateRangeStart(overrideWindow.getDateRangeStart())
                .dateRangeEnd(overrideWindow.getDateRangeEnd())
                .build();
        }

        return HearingWindow.builder()
            .firstDateTimeMustBe(getFirstDateTimeMustBe())
            .dateRangeStart(getDateRangeStart(caseData, refData))
            .dateRangeEnd(null)
            .build();
    }

    public static LocalDate getDateRangeStart(@Valid SscsCaseData caseData, ReferenceDataServiceHolder refData) {
        return refData.isAdjournmentFlagEnabled() && isYes(caseData.getAdjournment().getAdjournmentInProgress())
            ? AdjournmentCalculateDateHelper.getHearingWindowStart(caseData)
            : getHearingWindowStart(caseData);
    }

    public static LocalDate getHearingWindowStart(@Valid SscsCaseData caseData) {
        if (isCasePostponed(caseData)) {
            return LocalDate.now().plusDays(DAYS_TO_ADD_HEARING_WINDOW_TODAY_POSTPONEMENT);
        }

        if (isNotBlank(caseData.getDwpResponseDate())) {
            LocalDate dwpResponded = LocalDate.parse(caseData.getDwpResponseDate());
            if (HearingsDetailsMapping.isCaseUrgent(caseData)) {
                return dwpResponded.plusDays(DAYS_TO_ADD_HEARING_WINDOW_TODAY);
            } else {
                return dwpResponded.plusDays(DAYS_TO_ADD_HEARING_WINDOW_DWP_RESPONDED);
            }
        }

        return LocalDate.now().plusDays(DAYS_TO_ADD_HEARING_WINDOW_TODAY);
    }

    public static LocalDateTime getFirstDateTimeMustBe() {
        // TODO Adjournments - Find out how to use adjournCase data to work this out, possibly related variables:
        //      adjournCaseNextHearingDateType, adjournCaseNextHearingDateOrPeriod, adjournCaseNextHearingDateOrTime,
        //      adjournCaseNextHearingFirstAvailableDateAfterDate, adjournCaseNextHearingFirstAvailableDateAfterPeriod
        // TODO Future Work - Manual Override
        return null;
    }

    public static boolean isCasePostponed(SscsCaseData caseData) {
        return isYes(caseData.getPostponement().getUnprocessedPostponement());
    }
}
