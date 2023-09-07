package uk.gov.hmcts.reform.sscs.helper.mapping;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.sscs.ccd.domain.AdjournCaseNextHearingDurationUnits;
import uk.gov.hmcts.reform.sscs.ccd.domain.OverrideFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.reference.data.service.HearingDurationsService;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.sscs.ccd.domain.AdjournCaseNextHearingDurationType.NON_STANDARD;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;

@Slf4j
public final class HearingsDurationMapping {
    public static final int DURATION_SESSIONS_MULTIPLIER = 165;
    public static final int DURATION_DEFAULT = 60;
    public static final int MIN_HEARING_DURATION = 30;
    public static final int MIN_HEARING_SESSION_DURATION = 1;

    private HearingsDurationMapping() {
    }

    public static int getHearingDuration(SscsCaseData caseData, ReferenceDataServiceHolder refData) {
        Integer duration;
        HearingDurationsService hearingDurationsService = refData.getHearingDurations();
        String caseId = caseData.getCcdCaseId();
        boolean adjournmentInProgress = refData.isAdjournmentFlagEnabled() && isYes(caseData.getAdjournment().getAdjournmentInProgress());
        // adjournment values take precedence over override fields if adjournment in progress
        if (adjournmentInProgress) {
            duration = getHearingDurationAdjournment(caseData, hearingDurationsService);
            if (nonNull(duration)) {
                log.debug("Hearing Duration for Case ID {} set as Adjournment value {}", caseId, duration);
                return duration;
            }
        }
        OverrideFields overrideFields = OverridesMapping.getOverrideFields(caseData);
        // if no adjournment in progress, we first try to set the override value if present
        if (nonNull(overrideFields.getDuration()) && overrideFields.getDuration() >= MIN_HEARING_DURATION) {
            log.debug("Hearing Duration for Case ID {} set as existing Override Field value {}", caseId, overrideFields.getDuration());
            return overrideFields.getDuration();
        }
        // otherwise we set duration based on existing duration values ref data json
        duration = hearingDurationsService.getHearingDurationBenefitIssueCodes(caseData);
        if (nonNull(duration)) {
            log.debug("Hearing Duration for Case ID {} set as Benefit Code value {}", caseId, duration);
            return duration;
        }
        // else return default value (30)
        log.debug("Hearing Duration for Case ID {} set as default value {}", caseId, DURATION_DEFAULT);
        return DURATION_DEFAULT;
    }

    public static Integer getHearingDurationAdjournment(SscsCaseData caseData, HearingDurationsService hearingDurationsService) {
        Integer duration = caseData.getAdjournment().getNextHearingListingDuration();

        log.info("Next hearing listing duration: {}", duration);

        if (nonNull(duration) && caseData.getAdjournment().getNextHearingListingDurationType() == NON_STANDARD) {
            return handleNonStandardDuration(caseData, duration);
        }

        return hearingDurationsService.getHearingDurationBenefitIssueCodes(caseData);
    }

    private static Integer handleNonStandardDuration(SscsCaseData caseData, Integer duration) {
        AdjournCaseNextHearingDurationUnits units = caseData.getAdjournment().getNextHearingListingDurationUnits();
        if (isYes(caseData.getAdjournment().getInterpreterRequired())) {
            return DURATION_DEFAULT;
        } else if (units == AdjournCaseNextHearingDurationUnits.SESSIONS && duration >= MIN_HEARING_SESSION_DURATION) {
            return duration * DURATION_SESSIONS_MULTIPLIER;
        } else if (units == AdjournCaseNextHearingDurationUnits.MINUTES && duration >= MIN_HEARING_DURATION) {
            return duration;
        }

        return DURATION_DEFAULT;
    }

    public static List<String> getNonStandardHearingDurationReasons() {
        // TODO Future Work
        return Collections.emptyList();
    }
}
