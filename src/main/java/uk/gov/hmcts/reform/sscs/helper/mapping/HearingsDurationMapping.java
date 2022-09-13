package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsCaseMapping.isInterpreterRequired;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsDetailsMapping.getElementsDisputed;


public final class HearingsDurationMapping {
    public static final int DURATION_SESSIONS_MULTIPLIER = 165;
    public static final int DURATION_DEFAULT = 30;
    public static final int MIN_HEARING_DURATION = 30;
    public static final int MIN_HEARING_SESSION_DURATION = 1;
    public static final String DURATION_TYPE_NON_STANDARD_TIME_SLOT = "nonStandardTimeSlot";
    public static final String DURATION_TYPE_STANDARD_TIME_SLOT = "standardTimeSlot";
    public static final String DURATION_UNITS_MINUTES = "minutes";
    public static final String DURATION_UNITS_SESSIONS = "sessions";

    private HearingsDurationMapping() {

    }

    public static int getHearingDuration(SscsCaseData caseData, ReferenceDataServiceHolder referenceDataServiceHolder) {
        OverrideFields overrideFields = OverridesMapping.getOverrideFields(caseData);

        if (nonNull(overrideFields.getDuration()) && overrideFields.getDuration().intValue() >= MIN_HEARING_DURATION) {
            return overrideFields.getDuration().intValue();
        }

        Integer duration = getHearingDurationAdjournment(caseData);
        if (isNull(duration)) {
            duration = getHearingDurationBenefitIssueCodes(caseData, referenceDataServiceHolder);
        }

        return nonNull(duration) ? duration : DURATION_DEFAULT;
    }

    public static Integer getHearingDurationAdjournment(SscsCaseData caseData) {

        if (DURATION_TYPE_NON_STANDARD_TIME_SLOT.equalsIgnoreCase(caseData.getAdjournCaseNextHearingListingDurationType())
            && isNotBlank(caseData.getAdjournCaseNextHearingListingDuration())) {
            int duration =  Integer.parseInt(caseData.getAdjournCaseNextHearingListingDuration());

            if (DURATION_UNITS_SESSIONS.equalsIgnoreCase(caseData.getAdjournCaseNextHearingListingDurationUnits())
                && duration >= MIN_HEARING_SESSION_DURATION) {
                return duration * DURATION_SESSIONS_MULTIPLIER;
            } else if (DURATION_UNITS_MINUTES.equalsIgnoreCase(caseData.getAdjournCaseNextHearingListingDurationUnits())
                       && duration >= MIN_HEARING_DURATION) {


                return duration;
            }
        }

        return null;
    }

    public static Integer getHearingDurationBenefitIssueCodes(SscsCaseData caseData, ReferenceDataServiceHolder referenceDataServiceHolder) {
        uk.gov.hmcts.reform.sscs.reference.data.model.HearingDuration hearingDuration = referenceDataServiceHolder.getHearingDurations().getHearingDuration(
            caseData.getBenefitCode(), caseData.getIssueCode());

        if (isNull(hearingDuration)) {
            return null;
        }

        if (isYes(caseData.getAppeal().getHearingOptions().getWantsToAttend())) {
            Integer duration = isInterpreterRequired(caseData)
                ? hearingDuration.getDurationInterpreter()
                : hearingDuration.getDurationFaceToFace();
            return referenceDataServiceHolder.getHearingDurations()
                .addExtraTimeIfNeeded(duration, hearingDuration.getBenefitCode(), hearingDuration.getIssue(),
                                      getElementsDisputed(caseData)
                );
        } else if (HearingsChannelMapping.isPaperCase(caseData)) {
            return hearingDuration.getDurationPaper();
        } else {
            return null;
        }
    }

    public static List<String> getNonStandardHearingDurationReasons() {
        // TODO Future Work
        return Collections.emptyList();
    }


}
