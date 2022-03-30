package uk.gov.hmcts.reform.sscs.hearing.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;
import uk.gov.hmcts.reform.sscs.model.servicehearingvalues.PartyFlags;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PartyFlagsMapping {

    public List<PartyFlags> getPartyFlags(SscsCaseData caseData) {
        List<PartyFlags> partyFlagsList = new ArrayList<>();
        partyFlagsList.add(mapSignLanguageType(caseData));
        partyFlagsList.add(disabledAccess(caseData));
        partyFlagsList.add(hearingLoop(caseData));
        partyFlagsList.add(confidentialCase(caseData));
        partyFlagsList.add(dwpUcb(caseData));
        partyFlagsList.add(dwpPhme(caseData));
        partyFlagsList.add(urgentCase(caseData));
        partyFlagsList.add(adjournCaseInterpreterLanguage(caseData));
        return partyFlagsList;
    }

    private PartyFlags mapSignLanguageType(SscsCaseData caseData) {
        var signLanguageType = Optional
               .ofNullable(caseData.getAppeal())
               .map(Appeal::getHearingOptions)
               .map(HearingOptions::getArrangements);
        PartyFlags partyFlagsSignLanguage = null;
        if (signLanguageType.isPresent()) {
            partyFlagsSignLanguage = PartyFlags.builder()
                .flagId(PartyFlagsMap.SIGN_LANGUAGE_TYPE.getFlagId())
                .flagDescription(PartyFlagsMap.SIGN_LANGUAGE_TYPE.getFlagDescription())
                .flagParentId(PartyFlagsMap.SIGN_LANGUAGE_TYPE.getParentId())
                .build();
        }
        return partyFlagsSignLanguage;
    }

    private PartyFlags disabledAccess(SscsCaseData caseData) {
        var arrangements = Optional
            .ofNullable(caseData.getAppeal())
            .map(Appeal::getHearingOptions)
            .map(HearingOptions::getArrangements);
        PartyFlags partyFlagsDisabledAccess = null;
        if (arrangements.isPresent() && arrangements.get().contains("disabledAccess")) {
            partyFlagsDisabledAccess = PartyFlags.builder()
                .flagId(PartyFlagsMap.DISABLED_ACCESS.getFlagId())
                .flagDescription(PartyFlagsMap.DISABLED_ACCESS.getFlagDescription())
                .flagParentId(PartyFlagsMap.DISABLED_ACCESS.getParentId()).build();
        }
        return partyFlagsDisabledAccess;
    }

    private PartyFlags hearingLoop(SscsCaseData caseData) {
        var arrangements = Optional
               .ofNullable(caseData.getAppeal())
               .map(Appeal::getHearingOptions)
               .map(HearingOptions::getArrangements);
        PartyFlags hearingLoop = null;
        if (arrangements.isPresent() && arrangements.get().contains("hearingLoop")) {
            hearingLoop = PartyFlags.builder()
                .flagId(PartyFlagsMap.HEARING_LOOP.getFlagId())
                .flagDescription(PartyFlagsMap.HEARING_LOOP.getFlagDescription())
                .flagParentId(PartyFlagsMap.HEARING_LOOP.getParentId()).build();
        }
        return hearingLoop;
    }

    private PartyFlags confidentialCase(SscsCaseData caseData) {
        var isConfidentialCase = caseData.getIsConfidentialCase();
        PartyFlags confidentialCase = null;
        if (isConfidentialCase == YesNo.YES) {
            confidentialCase = PartyFlags.builder()
                .flagId(PartyFlagsMap.IS_CONFIDENTIAL_CASE.getFlagId())
                .flagDescription(PartyFlagsMap.IS_CONFIDENTIAL_CASE.getFlagDescription())
                .flagParentId(PartyFlagsMap.IS_CONFIDENTIAL_CASE.getParentId()).build();
        }
        return confidentialCase;
    }

    private PartyFlags dwpUcb(SscsCaseData caseData) {
        var dwpUcb = caseData.getDwpUcb();
        PartyFlags dwpUcbPartyFlag = null;
        if (dwpUcb != null) {
            dwpUcbPartyFlag = PartyFlags.builder()
                .flagId(PartyFlagsMap.DWP_UCB.getFlagId())
                .flagDescription(PartyFlagsMap.DWP_UCB.getFlagDescription())
                .flagParentId(PartyFlagsMap.DWP_UCB.getParentId()).build();
        }
        return  dwpUcbPartyFlag;
    }

    private PartyFlags dwpPhme(SscsCaseData caseData) {
        var dwpPhme = caseData.getDwpPhme();
        PartyFlags dwpPhmePartyFlag = null;
        if (dwpPhme != null) {
            dwpPhmePartyFlag = PartyFlags.builder()
                .flagId(PartyFlagsMap.DWP_PHME.getFlagId())
                .flagDescription(PartyFlagsMap.DWP_PHME.getFlagDescription())
                .flagParentId(PartyFlagsMap.DWP_PHME.getParentId()).build();
        }
        return dwpPhmePartyFlag;
    }

    private PartyFlags urgentCase(SscsCaseData caseData) {
        var urgentCase = caseData.getUrgentCase();
        PartyFlags urgentCasePartyFlag = null;
        if (urgentCase != null) {
            urgentCasePartyFlag = PartyFlags.builder()
                .flagId(PartyFlagsMap.URGENT_CASE.getFlagId())
                .flagDescription(PartyFlagsMap.URGENT_CASE.getFlagDescription())
                .flagParentId(PartyFlagsMap.URGENT_CASE.getParentId()).build();
        }
        return urgentCasePartyFlag;
    }

    private PartyFlags adjournCaseInterpreterLanguage(SscsCaseData caseData) {
        var adjournCaseInterpreterLanguage = caseData.getAdjournCaseInterpreterLanguage();
        PartyFlags adjourncaseilpartyflag = null;
        if (adjournCaseInterpreterLanguage != null) {
            adjourncaseilpartyflag = PartyFlags.builder()
                .flagId(PartyFlagsMap.ADJOURN_CASE_INTERPRETER_LANGUAGE.getFlagId())
                .flagDescription(PartyFlagsMap.ADJOURN_CASE_INTERPRETER_LANGUAGE.getFlagDescription())
                .flagParentId(PartyFlagsMap.ADJOURN_CASE_INTERPRETER_LANGUAGE.getParentId()).build();
        }
        return adjourncaseilpartyflag;
    }
}
