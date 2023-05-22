package uk.gov.hmcts.reform.sscs.helper.mapping;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.sscs.ccd.domain.AdjournCaseNextHearingDurationUnits;
import uk.gov.hmcts.reform.sscs.ccd.domain.ElementDisputed;
import uk.gov.hmcts.reform.sscs.ccd.domain.ElementDisputedDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.OverrideFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingDuration;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.sscs.ccd.domain.AdjournCaseNextHearingDurationType.NON_STANDARD;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsCaseMapping.isInterpreterRequired;

@Slf4j
public final class HearingsDurationMapping {
    public static final int DURATION_SESSIONS_MULTIPLIER = 165;
    public static final int DURATION_DEFAULT = 30;
    public static final int MIN_HEARING_DURATION = 30;
    public static final int MIN_HEARING_SESSION_DURATION = 1;

    private HearingsDurationMapping() {
    }

    public static int getHearingDuration(SscsCaseData caseData, ReferenceDataServiceHolder refData) {

        Integer duration;
        String caseId = caseData.getCcdCaseId();
        boolean adjournmentInProgress = refData.isAdjournmentFlagEnabled() && isYes(caseData.getAdjournment().getAdjournmentInProgress());
        // adjournment values take precedence over override fields if adjournment in progress
        if (adjournmentInProgress) {
            duration = getHearingDurationAdjournment(caseData);
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
        duration = getHearingDurationBenefitIssueCodes(caseData, refData);
        if (nonNull(duration)) {
            log.debug("Hearing Duration for Case ID {} set as Benefit Code value {}", caseId, duration);
            return duration;
        }
        // else return default value (30)
        log.debug("Hearing Duration for Case ID {} set as default value {}", caseId, DURATION_DEFAULT);
        return DURATION_DEFAULT;
    }

    public static Integer getHearingDurationAdjournment(SscsCaseData caseData) {
        Integer duration = caseData.getAdjournment().getNextHearingListingDuration();
        if (duration != null && caseData.getAdjournment().getNextHearingListingDurationType() == NON_STANDARD) {
            return handleNonStandardDuration(caseData, duration);
        }
        return duration;
    }

    private static Integer handleNonStandardDuration(SscsCaseData caseData, Integer duration) {
        AdjournCaseNextHearingDurationUnits units = caseData.getAdjournment().getNextHearingListingDurationUnits();
        if (units == AdjournCaseNextHearingDurationUnits.SESSIONS && duration >= MIN_HEARING_SESSION_DURATION) {
            return duration * DURATION_SESSIONS_MULTIPLIER;
        } else if (units == AdjournCaseNextHearingDurationUnits.MINUTES && duration >= MIN_HEARING_DURATION) {
            return duration;
        }
        return DURATION_DEFAULT;
    }

    public static Integer getHearingDurationBenefitIssueCodes(SscsCaseData caseData, ReferenceDataServiceHolder refData) {
        HearingDuration hearingDuration = refData.getHearingDurations().getHearingDuration(
            caseData.getBenefitCode(), caseData.getIssueCode());

        if (isNull(hearingDuration)) {
            return null;
        }

        if (isYes(caseData.getAppeal().getHearingOptions().getWantsToAttend())) {
            Integer duration = isInterpreterRequired(caseData)
                ? hearingDuration.getDurationInterpreter()
                : hearingDuration.getDurationFaceToFace();
            return refData.getHearingDurations()
                .addExtraTimeIfNeeded(
                    duration,
                    hearingDuration.getBenefitCode(),
                    hearingDuration.getIssue(),
                    getElementsDisputed(caseData));
        } else if (HearingsChannelMapping.isPaperCase(caseData)) {
            return hearingDuration.getDurationPaper();
        }
        return DURATION_DEFAULT;
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
