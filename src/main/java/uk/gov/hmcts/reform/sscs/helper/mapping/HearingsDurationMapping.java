package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingDuration;
import uk.gov.hmcts.reform.sscs.reference.data.service.HearingDurationsService;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsCaseMapping.isInterpreterRequired;



public final class HearingsDurationMapping {
    public static final int DURATION_SESSIONS_MULTIPLIER = 165;
    public static final int DURATION_DEFAULT = 30;
    public static final int MIN_HEARING_DURATION = 30;
    public static final int MIN_HEARING_SESSION_DURATION = 1;
    public static final String DURATION_TYPE_NON_STANDARD_TIME_SLOT = "nonStandardTimeSlot";
    public static final String DURATION_UNITS_MINUTES = "minutes";
    public static final String DURATION_UNITS_SESSIONS = "sessions";

    private HearingsDurationMapping() {

    }

    public static int getHearingDuration(SscsCaseData caseData, ReferenceDataServiceHolder referenceDataServiceHolder) {
        OverrideFields overrideFields = OverridesMapping.getOverrideFields(caseData);

        if (nonNull(overrideFields.getDuration()) && overrideFields.getDuration().intValue() >= MIN_HEARING_DURATION) {
            return overrideFields.getDuration().intValue();
        }

        Integer duration = getHearingDurationAdjournment(caseData.getAdjournment());
        if (isNull(duration)) {
            duration = getHearingDurationBenefitIssueCodes(caseData, referenceDataServiceHolder);
        }

        return nonNull(duration) ? duration : DURATION_DEFAULT;
    }

    public static Integer getHearingDurationAdjournment(Adjournment adjournment) {
        if (adjournment.getIsAdjournmentInProgress().toBoolean()) {
            Integer duration = adjournment.getNextHearingListingDuration();

            AdjournCaseNextHearingDurationType nextHearingDurationType = adjournment.getNextHearingListingDurationType();

            if (nonNull(nextHearingDurationType)
                && DURATION_TYPE_NON_STANDARD_TIME_SLOT.equalsIgnoreCase(nextHearingDurationType.toString())
                && nonNull(duration)) {
                return getDuration(duration, adjournment.getNextHearingListingDurationUnits());
            }
        }

        return null;
    }

    private static Integer getDuration(Integer duration, AdjournCaseNextHearingDurationUnits nextDurationUnits) {
        if (nonNull(nextDurationUnits)) {
            String durationUnits = nextDurationUnits.toString();

            if (DURATION_UNITS_SESSIONS.equalsIgnoreCase(durationUnits)
                && duration >= MIN_HEARING_SESSION_DURATION) {
                return duration * DURATION_SESSIONS_MULTIPLIER;
            } else if (DURATION_UNITS_MINUTES.equalsIgnoreCase(durationUnits)
                && duration >= MIN_HEARING_DURATION) {
                return duration;
            }
        }

        return null;
    }

    public static Integer getHearingDurationBenefitIssueCodes(SscsCaseData caseData,
                                                              ReferenceDataServiceHolder referenceDataServiceHolder) {
        HearingDurationsService hearingDurationsService = referenceDataServiceHolder.getHearingDurations();

        if (isNull(hearingDurationsService)) {
            return null;
        }

        HearingDuration hearingDuration = hearingDurationsService.getHearingDuration(
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
                                      getElementsDisputed(caseData));
        } else if (HearingsChannelMapping.isPaperCase(caseData)) {
            return hearingDuration.getDurationPaper();
        }

        return null;
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
