package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.CaseFlags;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PartyFlags;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsDetailsMapping.isCaseUrgent;
import static uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PartyFlagsMap.ADJOURN_CASE_INTERPRETER_LANGUAGE;
import static uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PartyFlagsMap.DISABLED_ACCESS;
import static uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PartyFlagsMap.DWP_PHME;
import static uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PartyFlagsMap.DWP_UCB;
import static uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PartyFlagsMap.HEARING_LOOP;
import static uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PartyFlagsMap.IS_CONFIDENTIAL_CASE;
import static uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PartyFlagsMap.SIGN_LANGUAGE_TYPE;
import static uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PartyFlagsMap.URGENT_CASE;

public final class PartyFlagsMapping {

    private PartyFlagsMapping() {

    }

    public static List<PartyFlags> getPartyFlags(SscsCaseData caseData) {
        return Stream.of(
                signLanguage(caseData),
                disabledAccess(caseData),
                hearingLoop(caseData),
                confidentialCase(caseData),
                dwpUcb(caseData),
                dwpPhme(caseData),
                urgentCase(caseData),
                adjournCaseInterpreterLanguage(caseData))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public static PartyFlags signLanguage(SscsCaseData caseData) {
        String signLanguageType = Optional
            .ofNullable(caseData.getAppeal())
            .map(Appeal::getHearingOptions)
            .map(HearingOptions::getSignLanguageType)
            .orElse(null);

        if (isNotBlank(signLanguageType)) {
            return PartyFlags.builder()
                .flagId(SIGN_LANGUAGE_TYPE.getFlagId())
                .flagDescription(SIGN_LANGUAGE_TYPE.getFlagDescription())
                .flagParentId(SIGN_LANGUAGE_TYPE.getParentId())
                .build();
        }
        return null;
    }

    public static PartyFlags disabledAccess(SscsCaseData caseData) {
        boolean wantsAccessibleHearingRoom = Optional
            .ofNullable(caseData.getAppeal())
            .map(Appeal::getHearingOptions)
            .map(HearingOptions::wantsAccessibleHearingRoom)
            .orElse(false);

        if (wantsAccessibleHearingRoom) {
            return PartyFlags.builder()
                .flagId(DISABLED_ACCESS.getFlagId())
                .flagDescription(DISABLED_ACCESS.getFlagDescription())
                .flagParentId(DISABLED_ACCESS.getParentId()).build();
        }
        return null;
    }

    public static PartyFlags hearingLoop(SscsCaseData caseData) {
        boolean wantsHearingLoop = Optional
            .ofNullable(caseData.getAppeal())
            .map(Appeal::getHearingOptions)
            .map(HearingOptions::wantsHearingLoop)
            .orElse(false);

        if (wantsHearingLoop) {
            return PartyFlags.builder()
                .flagId(HEARING_LOOP.getFlagId())
                .flagDescription(HEARING_LOOP.getFlagDescription())
                .flagParentId(HEARING_LOOP.getParentId()).build();
        }
        return null;
    }

    public static PartyFlags confidentialCase(SscsCaseData caseData) {
        if (isYes(caseData.getIsConfidentialCase())) {
            return PartyFlags.builder()
                .flagId(IS_CONFIDENTIAL_CASE.getFlagId())
                .flagDescription(IS_CONFIDENTIAL_CASE.getFlagDescription())
                .flagParentId(IS_CONFIDENTIAL_CASE.getParentId())
                .build();
        }
        return null;
    }

    public static PartyFlags dwpUcb(SscsCaseData caseData) {
        if (isYes(caseData.getDwpUcb())) {
            return PartyFlags.builder()
                .flagId(DWP_UCB.getFlagId())
                .flagDescription(DWP_UCB.getFlagDescription())
                .flagParentId(DWP_UCB.getParentId()).build();
        }
        return null;
    }

    public static PartyFlags dwpPhme(SscsCaseData caseData) {
        if (isYes(caseData.getDwpPhme())) {
            return PartyFlags.builder()
                .flagId(DWP_PHME.getFlagId())
                .flagDescription(DWP_PHME.getFlagDescription())
                .flagParentId(DWP_PHME.getParentId()).build();
        }
        return null;
    }

    public static PartyFlags urgentCase(SscsCaseData caseData) {
        if (isCaseUrgent(caseData)) {
            return PartyFlags.builder()
                .flagId(URGENT_CASE.getFlagId())
                .flagDescription(URGENT_CASE.getFlagDescription())
                .flagParentId(URGENT_CASE.getParentId()).build();
        }
        return null;
    }

    public static PartyFlags adjournCaseInterpreterLanguage(SscsCaseData caseData) {
        if (isNotBlank(caseData.getAdjournCaseInterpreterLanguage())) {
            return PartyFlags.builder()
                .flagId(ADJOURN_CASE_INTERPRETER_LANGUAGE.getFlagId())
                .flagDescription(ADJOURN_CASE_INTERPRETER_LANGUAGE.getFlagDescription())
                .flagParentId(ADJOURN_CASE_INTERPRETER_LANGUAGE.getParentId()).build();
        }
        return null;
    }

    public static CaseFlags getCaseFlags(SscsCaseData sscsCaseData) {
        return CaseFlags.builder()
                .flags(PartyFlagsMapping.getPartyFlags(sscsCaseData).stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .flagAmendUrl("") //TODO Implement when present
                .build();
    }
}
