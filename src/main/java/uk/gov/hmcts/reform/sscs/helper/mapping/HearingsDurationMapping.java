package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingDuration;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsCaseMapping.isInterpreterRequired;



public final class HearingsDurationMapping {
    public static final int DURATION_DEFAULT = 30;
    public static final int DURATION_HOURS_MULTIPLIER = 60;
    public static final int DURATION_SESSIONS_MULTIPLIER = 165;
    public static final int MIN_HEARING_DURATION = 1;
    public static final int MIN_HEARING_SESSION_DURATION = 1;
    public static final String DURATION_TYPE_NON_STANDARD_TIME_SLOT = "nonStandardTimeSlot";
    public static final String DURATION_UNIT_HOURS = "hours";
    public static final String DURATION_UNITS_MINUTES = "minutes";
    public static final String DURATION_UNITS_SESSIONS = "sessions";

    private HearingsDurationMapping() {
    }

    public static int getHearingDuration(
        SscsCaseData caseData,
        ReferenceDataServiceHolder referenceDataServiceHolder
    ) {
        OverrideFields overrideFields = OverridesMapping.getOverrideFields(caseData);

        if (nonNull(overrideFields.getDuration())
            && overrideFields.getDuration().intValue() >= MIN_HEARING_DURATION
        ) {
            return overrideFields.getDuration().intValue();
        }

        Integer duration = getHearingDurationAdjournment(caseData, referenceDataServiceHolder);
        if (isNull(duration)) {
            duration = getHearingDurationBenefitIssueCodes(caseData, referenceDataServiceHolder);
        }

        return nonNull(duration) ? duration : DURATION_DEFAULT;
    }

    public static Integer getHearingDurationAdjournment(
        SscsCaseData caseData,
        ReferenceDataServiceHolder referenceDataServiceHolder
    ) {
        return referenceDataServiceHolder.isAdjournmentFlagEnabled() // TODO remove when flag enabled
            ? getHearingDurationAdjournmentNew(caseData)
            : getHearingDurationAdjournmentOld(caseData);
    }

    private static Integer getHearingDurationAdjournmentOld(SscsCaseData caseData) {
        String adjournCaseNextHearingListingDuration = caseData.getAdjournCaseNextHearingListingDuration();

        if (isNotBlank(adjournCaseNextHearingListingDuration)
            && Integer.parseInt(adjournCaseNextHearingListingDuration) >= MIN_HEARING_DURATION
        ) {
            if (DURATION_UNITS_SESSIONS.equalsIgnoreCase(caseData.getAdjournCaseNextHearingListingDurationUnits())) {
                return Integer.parseInt(adjournCaseNextHearingListingDuration) * DURATION_SESSIONS_MULTIPLIER;
            }
            if (DURATION_UNIT_HOURS.equalsIgnoreCase(caseData.getAdjournCaseNextHearingListingDurationUnits())) {
                // TODO Adjournments - check no other measurement than hours, sessions and null
                return Integer.parseInt(adjournCaseNextHearingListingDuration) * DURATION_HOURS_MULTIPLIER;
            }
        }

        return null;
    }

    private static Integer getHearingDurationAdjournmentNew(SscsCaseData caseData) {
        String nextHearingListingDuration = caseData.getAdjournCaseNextHearingListingDuration();

        if (DURATION_TYPE_NON_STANDARD_TIME_SLOT.equalsIgnoreCase(caseData.getAdjournCaseNextHearingListingDurationType())
            && isNotBlank(nextHearingListingDuration)
        ) {
            int duration = Integer.parseInt(nextHearingListingDuration);

            String durationUnits = caseData.getAdjournCaseNextHearingListingDurationUnits();
            if (DURATION_UNITS_SESSIONS.equalsIgnoreCase(durationUnits)
                && duration >= MIN_HEARING_SESSION_DURATION
            ) {
                return duration * DURATION_SESSIONS_MULTIPLIER;
            } else if (DURATION_UNITS_MINUTES.equalsIgnoreCase(durationUnits)
                && duration >= MIN_HEARING_DURATION
            ) {
                return duration;
            }
        }

        return null;
    }

    public static Integer getHearingDurationBenefitIssueCodes(
        SscsCaseData caseData,
        ReferenceDataServiceHolder referenceDataServiceHolder
    ) {
        HearingDuration hearingDuration = referenceDataServiceHolder.getHearingDurations().getHearingDuration(
            caseData.getBenefitCode(), caseData.getIssueCode());

        if (isNull(hearingDuration)) {
            return null;
        }

        if (isYes(caseData.getAppeal().getHearingOptions().getWantsToAttend())) {
            Integer duration = isInterpreterRequired(caseData)
                ? hearingDuration.getDurationInterpreter()
                : hearingDuration.getDurationFaceToFace();
            return referenceDataServiceHolder.getHearingDurations()
                .addExtraTimeIfNeeded(
                    duration,
                    hearingDuration.getBenefitCode(),
                    hearingDuration.getIssue(),
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

    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public static List<String> getElementsDisputed(SscsCaseData caseData) {
        List<ElementDisputed> elementDisputed = new ArrayList<>();

        List<List<ElementDisputed>> elementsToCheck = new ArrayList<>();
        elementsToCheck.add(caseData.getElementsDisputedGeneral());
        elementsToCheck.add(caseData.getElementsDisputedSanctions());
        elementsToCheck.add(caseData.getElementsDisputedOverpayment());
        elementsToCheck.add(caseData.getElementsDisputedHousing());
        elementsToCheck.add(caseData.getElementsDisputedChildCare());
        elementsToCheck.add(caseData.getElementsDisputedCare());
        elementsToCheck.add(caseData.getElementsDisputedChildElement());
        elementsToCheck.add(caseData.getElementsDisputedChildDisabled());
        elementsToCheck.add(caseData.getElementsDisputedLimitedWork());

        elementsToCheck.forEach((List<ElementDisputed> list) -> {
            if (isNotEmpty(list)) {
                elementDisputed.addAll(list);
            }
        });

        return elementDisputed.stream()
            .map(ElementDisputed::getValue)
            .map(ElementDisputedDetails::getIssueCode)
            .collect(Collectors.toList());
    }
}
