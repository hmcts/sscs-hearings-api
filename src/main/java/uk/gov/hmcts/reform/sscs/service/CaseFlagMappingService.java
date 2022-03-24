package uk.gov.hmcts.reform.sscs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.helpers.FlagCode;
import uk.gov.hmcts.reform.sscs.model.DescendantCaseData;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;

import java.util.Map;
import javax.validation.constraints.NotNull;

@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.LawOfDemeter")
public class CaseFlagMappingService {

    private final Map<String, String> caseFlags;

    public DescendantCaseData getCaseFlagMapping(@NotNull HearingWrapper hearingWrapper) {
        SscsCaseData caseData = hearingWrapper.getOriginalCaseData();
        return DescendantCaseData
            .DescendantCaseDataBuilder()
            .caseFlags(mapCaseFlags(caseData))
            .build();
    }

    public Map<String, String> mapCaseFlags(SscsCaseData caseData) {
        mapSignLanguageType(caseData);
        mapArrangements(caseData);
        confidentialCase(caseData);
        dwp(caseData);
        urgentCase(caseData);
        adjournCaseInterpreterLanguage(caseData);
        return this.caseFlags;
    }

    private void mapSignLanguageType(SscsCaseData caseData) {
        var signLanguageType = caseData.getAppeal().getHearingOptions().getSignLanguageType();
        if (signLanguageType != null) {
            this.caseFlags.put(
                FlagCode.SIGN_LANGUAGE_TYPE.name(), FlagCode.SIGN_LANGUAGE_TYPE.getFlagCode()
            );
        }
    }

    private void mapArrangements(SscsCaseData caseData) {
        var arrangements = caseData.getAppeal().getHearingOptions().getArrangements();

        if (!arrangements.isEmpty()) {
            if (arrangements.contains("disabledAccess")) {
                this.caseFlags.put(
                    FlagCode.DISABLED_ACCESS.name(), FlagCode.DISABLED_ACCESS.getFlagCode()
                );
            }
            if (arrangements.contains("hearingLoop")) {
                this.caseFlags.put(
                    FlagCode.HEARING_LOOP.name(), FlagCode.HEARING_LOOP.getFlagCode()
                );
            }
        }
    }

    private void confidentialCase(SscsCaseData caseData) {
        var isConfidentialCase = caseData.getIsConfidentialCase();
        if (isConfidentialCase != null) {
            this.caseFlags.put(
                FlagCode.IS_CONFIDENTIAL_CASE.name(), FlagCode.IS_CONFIDENTIAL_CASE.getFlagCode()
            );
        }
    }

    private void dwp(SscsCaseData caseData) {
        var dwpUcb = caseData.getDwpUcb();
        var dwpPhme = caseData.getDwpPhme();

        if (dwpUcb != null) {
            this.caseFlags.put(
                FlagCode.DWP_UCB.name(), FlagCode.DWP_UCB.getFlagCode()
            );
        }
        if (dwpPhme != null) {
            this.caseFlags.put(
                FlagCode.DWP_PHME.name(), FlagCode.DWP_PHME.getFlagCode()
            );
        }
    }

    private void urgentCase(SscsCaseData caseData) {
        var urgentCase = caseData.getUrgentCase();
        if (urgentCase != null) {
            this.caseFlags.put(
                FlagCode.URGENT_CASE.name(), FlagCode.URGENT_CASE.getFlagCode()
            );
        }
    }

    private void adjournCaseInterpreterLanguage(SscsCaseData caseData) {
        var adjournCaseInterpreterLanguage = caseData.getAdjournCaseInterpreterLanguage();
        if (adjournCaseInterpreterLanguage != null) {
            this.caseFlags.put(
                FlagCode.AD_JOURN_CASE_INTERPRETER_LANGUAGE.name(),
                FlagCode.AD_JOURN_CASE_INTERPRETER_LANGUAGE.getFlagCode()
            );
        }
    }
}
