package uk.gov.hmcts.reform.sscs.hearing.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;
import uk.gov.hmcts.reform.sscs.model.servicehearingvalues.ShvPartyFlags;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.sscs.hearing.mapping.PartyFlagsMap.ADJOURN_CASE_INTERPRETER_LANGUAGE;
import static uk.gov.hmcts.reform.sscs.hearing.mapping.PartyFlagsMap.DISABLED_ACCESS;
import static uk.gov.hmcts.reform.sscs.hearing.mapping.PartyFlagsMap.DWP_PHME;
import static uk.gov.hmcts.reform.sscs.hearing.mapping.PartyFlagsMap.DWP_UCB;
import static uk.gov.hmcts.reform.sscs.hearing.mapping.PartyFlagsMap.HEARING_LOOP;
import static uk.gov.hmcts.reform.sscs.hearing.mapping.PartyFlagsMap.IS_CONFIDENTIAL_CASE;
import static uk.gov.hmcts.reform.sscs.hearing.mapping.PartyFlagsMap.SIGN_LANGUAGE_TYPE;
import static uk.gov.hmcts.reform.sscs.hearing.mapping.PartyFlagsMap.URGENT_CASE;

public final class PartyFlagsMapping {

    public static List<ShvPartyFlags> getPartyFlags(SscsCaseData caseData) {
        return Arrays.asList(
            mapSignLanguageType(caseData),
            disabledAccess(caseData),
            hearingLoop(caseData),
            confidentialCase(caseData),
            dwpUcb(caseData),
            dwpPhme(caseData),
            urgentCase(caseData),
            adjournCaseInterpreterLanguage(caseData)
        );
    }

    private PartyFlagsMapping() {
        //not called
    }

    private static ShvPartyFlags mapSignLanguageType(SscsCaseData caseData) {
        var signLanguageType = Optional
            .ofNullable(caseData.getAppeal())
            .map(Appeal::getHearingOptions)
            .map(HearingOptions::getSignLanguageType);
        ShvPartyFlags partyFlagsSignLanguage = null;
        if (signLanguageType.isPresent()) {
            partyFlagsSignLanguage = ShvPartyFlags.builder()
                .flagId(SIGN_LANGUAGE_TYPE.getFlagId())
                .flagDescription(SIGN_LANGUAGE_TYPE.getFlagDescription())
                .flagParentId(SIGN_LANGUAGE_TYPE.getParentId())
                .build();
        }
        return partyFlagsSignLanguage;
    }

    private static ShvPartyFlags disabledAccess(SscsCaseData caseData) {
        HearingOptions options = Optional
            .ofNullable(caseData.getAppeal())
            .map(Appeal::getHearingOptions).orElse(null);
        ShvPartyFlags partyFlagsDisabledAccess = null;

        if (Objects.nonNull(options) && options.wantsAccessibleHearingRoom()) {
            partyFlagsDisabledAccess = ShvPartyFlags.builder()
                .flagId(DISABLED_ACCESS.getFlagId())
                .flagDescription(DISABLED_ACCESS.getFlagDescription())
                .flagParentId(DISABLED_ACCESS.getParentId()).build();
        }
        return partyFlagsDisabledAccess;
    }

    private static ShvPartyFlags hearingLoop(SscsCaseData caseData) {
        HearingOptions options = Optional
            .ofNullable(caseData.getAppeal())
            .map(Appeal::getHearingOptions).orElse(null);
        ShvPartyFlags hearingLoop = null;
        if (Objects.nonNull(options) && options.wantsHearingLoop()) {
            hearingLoop = ShvPartyFlags.builder()
                .flagId(HEARING_LOOP.getFlagId())
                .flagDescription(HEARING_LOOP.getFlagDescription())
                .flagParentId(HEARING_LOOP.getParentId()).build();
        }
        return hearingLoop;
    }

    private static ShvPartyFlags confidentialCase(SscsCaseData caseData) {
        var isConfidentialCase = caseData.getIsConfidentialCase();
        ShvPartyFlags confidentialCase = null;
        if (isConfidentialCase == YesNo.YES) {
            confidentialCase = ShvPartyFlags.builder()
                .flagId(IS_CONFIDENTIAL_CASE.getFlagId())
                .flagDescription(IS_CONFIDENTIAL_CASE.getFlagDescription())
                .flagParentId(IS_CONFIDENTIAL_CASE.getParentId())
                .build();
        }
        return confidentialCase;
    }

    private static ShvPartyFlags dwpUcb(SscsCaseData caseData) {
        var dwpUcb = caseData.getDwpUcb();
        ShvPartyFlags dwpUcbPartyFlag = null;
        if (dwpUcb != null) {
            dwpUcbPartyFlag = ShvPartyFlags.builder()
                .flagId(DWP_UCB.getFlagId())
                .flagDescription(DWP_UCB.getFlagDescription())
                .flagParentId(DWP_UCB.getParentId()).build();
        }
        return  dwpUcbPartyFlag;
    }

    private static ShvPartyFlags dwpPhme(SscsCaseData caseData) {
        var dwpPhme = caseData.getDwpPhme();
        ShvPartyFlags dwpPhmePartyFlag = null;
        if (dwpPhme != null) {
            dwpPhmePartyFlag = ShvPartyFlags.builder()
                .flagId(DWP_PHME.getFlagId())
                .flagDescription(DWP_PHME.getFlagDescription())
                .flagParentId(DWP_PHME.getParentId()).build();
        }
        return dwpPhmePartyFlag;
    }

    private static ShvPartyFlags urgentCase(SscsCaseData caseData) {
        var urgentCase = caseData.getUrgentCase();
        ShvPartyFlags urgentCasePartyFlag = null;
        if (urgentCase != null) {
            urgentCasePartyFlag = ShvPartyFlags.builder()
                .flagId(URGENT_CASE.getFlagId())
                .flagDescription(URGENT_CASE.getFlagDescription())
                .flagParentId(URGENT_CASE.getParentId()).build();
        }
        return urgentCasePartyFlag;
    }

    private static ShvPartyFlags adjournCaseInterpreterLanguage(SscsCaseData caseData) {
        var adjournCaseInterpreterLanguage = caseData.getAdjournCaseInterpreterLanguage();
        ShvPartyFlags adjournCasePartyFlag = null;
        if (adjournCaseInterpreterLanguage != null) {
            adjournCasePartyFlag = ShvPartyFlags.builder()
                .flagId(ADJOURN_CASE_INTERPRETER_LANGUAGE.getFlagId())
                .flagDescription(ADJOURN_CASE_INTERPRETER_LANGUAGE.getFlagDescription())
                .flagParentId(ADJOURN_CASE_INTERPRETER_LANGUAGE.getParentId()).build();
        }
        return adjournCasePartyFlag;
    }
}
