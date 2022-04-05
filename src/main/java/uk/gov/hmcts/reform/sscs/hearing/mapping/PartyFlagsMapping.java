package uk.gov.hmcts.reform.sscs.hearing.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;
import uk.gov.hmcts.reform.sscs.model.servicehearingvalues.PartyFlags;

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

public class PartyFlagsMapping {

    public List<PartyFlags> getPartyFlags(SscsCaseData caseData) {
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

    private PartyFlags mapSignLanguageType(SscsCaseData caseData) {
        var signLanguageType = Optional
            .ofNullable(caseData.getAppeal())
            .map(Appeal::getHearingOptions)
            .map(HearingOptions::getSignLanguageType);
        PartyFlags partyFlagsSignLanguage = null;
        if (signLanguageType.isPresent()) {
            partyFlagsSignLanguage = PartyFlags.builder()
                .flagId(SIGN_LANGUAGE_TYPE.getFlagId())
                .flagDescription(SIGN_LANGUAGE_TYPE.getFlagDescription())
                .flagParentId(SIGN_LANGUAGE_TYPE.getParentId())
                .build();
        }
        return partyFlagsSignLanguage;
    }

    private PartyFlags disabledAccess(SscsCaseData caseData) {
        HearingOptions options = Optional
            .ofNullable(caseData.getAppeal())
            .map(Appeal::getHearingOptions).orElse(null);
        PartyFlags partyFlagsDisabledAccess = null;

        if (Objects.nonNull(options) && options.wantsAccessibleHearingRoom()) {
            partyFlagsDisabledAccess = PartyFlags.builder()
                .flagId(DISABLED_ACCESS.getFlagId())
                .flagDescription(DISABLED_ACCESS.getFlagDescription())
                .flagParentId(DISABLED_ACCESS.getParentId()).build();
        }
        return partyFlagsDisabledAccess;
    }

    private PartyFlags hearingLoop(SscsCaseData caseData) {
        HearingOptions options = Optional
            .ofNullable(caseData.getAppeal())
            .map(Appeal::getHearingOptions).orElse(null);
        PartyFlags hearingLoop = null;
        if (Objects.nonNull(options) && options.wantsHearingLoop()) {
            hearingLoop = PartyFlags.builder()
                .flagId(HEARING_LOOP.getFlagId())
                .flagDescription(HEARING_LOOP.getFlagDescription())
                .flagParentId(HEARING_LOOP.getParentId()).build();
        }
        return hearingLoop;
    }

    private PartyFlags confidentialCase(SscsCaseData caseData) {
        var isConfidentialCase = caseData.getIsConfidentialCase();
        PartyFlags confidentialCase = null;
        if (isConfidentialCase == YesNo.YES) {
            confidentialCase = PartyFlags.builder()
                .flagId(IS_CONFIDENTIAL_CASE.getFlagId())
                .flagDescription(IS_CONFIDENTIAL_CASE.getFlagDescription())
                .flagParentId(IS_CONFIDENTIAL_CASE.getParentId())
                .build();
        }
        return confidentialCase;
    }

    private PartyFlags dwpUcb(SscsCaseData caseData) {
        var dwpUcb = caseData.getDwpUcb();
        PartyFlags dwpUcbPartyFlag = null;
        if (dwpUcb != null) {
            dwpUcbPartyFlag = PartyFlags.builder()
                .flagId(DWP_UCB.getFlagId())
                .flagDescription(DWP_UCB.getFlagDescription())
                .flagParentId(DWP_UCB.getParentId()).build();
        }
        return  dwpUcbPartyFlag;
    }

    private PartyFlags dwpPhme(SscsCaseData caseData) {
        var dwpPhme = caseData.getDwpPhme();
        PartyFlags dwpPhmePartyFlag = null;
        if (dwpPhme != null) {
            dwpPhmePartyFlag = PartyFlags.builder()
                .flagId(DWP_PHME.getFlagId())
                .flagDescription(DWP_PHME.getFlagDescription())
                .flagParentId(DWP_PHME.getParentId()).build();
        }
        return dwpPhmePartyFlag;
    }

    private PartyFlags urgentCase(SscsCaseData caseData) {
        var urgentCase = caseData.getUrgentCase();
        PartyFlags urgentCasePartyFlag = null;
        if (urgentCase != null) {
            urgentCasePartyFlag = PartyFlags.builder()
                .flagId(URGENT_CASE.getFlagId())
                .flagDescription(URGENT_CASE.getFlagDescription())
                .flagParentId(URGENT_CASE.getParentId()).build();
        }
        return urgentCasePartyFlag;
    }

    private PartyFlags adjournCaseInterpreterLanguage(SscsCaseData caseData) {
        var adjournCaseInterpreterLanguage = caseData.getAdjournCaseInterpreterLanguage();
        PartyFlags adjournCasePartyFlag = null;
        if (adjournCaseInterpreterLanguage != null) {
            adjournCasePartyFlag = PartyFlags.builder()
                .flagId(ADJOURN_CASE_INTERPRETER_LANGUAGE.getFlagId())
                .flagDescription(ADJOURN_CASE_INTERPRETER_LANGUAGE.getFlagDescription())
                .flagParentId(ADJOURN_CASE_INTERPRETER_LANGUAGE.getParentId()).build();
        }
        return adjournCasePartyFlag;
    }
}
