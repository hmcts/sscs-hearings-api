package uk.gov.hmcts.reform.sscs.helpers;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

import java.util.HashMap;
import java.util.Map;

@Service
@SuppressWarnings("PMD")
public class CaseFlagMappingServiceHelper {

    private final String[] mappingValues =
        {
        "disabledAccess",
        "signLanguageType",
        "hearingLoop",
        "isConfidentialCase",
        "dwpUCB",
        "dwpPHME",
        "urgentCase",
        "adjournCaseInterpreterLanguage"
        };

    private final Map<String, Object> caseFlags;

    public CaseFlagMappingServiceHelper() {
        this.caseFlags = new HashMap<>();
    }

    public Map<String, Object> mapCaseFlags(SscsCaseData caseData) {
        mapSignLanguageType(caseData);
        mapArrangements(caseData);
        confidentialCase(caseData);
        dwp(caseData);
        urgentCase(caseData);
        adjournCaseInterpreterLanguage(caseData);
        return caseFlags;
    }

    private void mapSignLanguageType(SscsCaseData caseData) {
        var signLanguageType = caseData.getAppeal().getHearingOptions().getSignLanguageType();
        if (signLanguageType != null) {
            caseFlags.putAll(
                Map.of(FlagCodes.RA0042.name(), mappingValues[1])
            );
        }
    }

    private void mapArrangements(SscsCaseData caseData) {
        var arrangements = caseData.getAppeal().getHearingOptions().getArrangements();
        if (arrangements.contains(mappingValues[0])) {
            caseFlags.putAll(
                Map.of(FlagCodes.RA0019.name(), mappingValues[0])
            );
        }
        if (arrangements.contains(mappingValues[2])) {
            caseFlags.putAll(
                Map.of(FlagCodes.RA0043.name(), mappingValues[2])
            );
        }
    }

    private void confidentialCase(SscsCaseData caseData) {
        var isConfidentialCase = caseData.getIsConfidentialCase();
        if (isConfidentialCase != null) {
            caseFlags.putAll(
                Map.of(FlagCodes.PF0004.name(), mappingValues[3])
            );
        }
    }

    private void dwp(SscsCaseData caseData) {
        var dwpUcb = caseData.getDwpUcb();
        var dwpPhme = caseData.getDwpPhme();

        if (dwpUcb != null) {
            caseFlags.putAll(
                Map.of(FlagCodes.PF0007.name(), mappingValues[4])
            );
        }
        if (dwpPhme != null) {
            caseFlags.putAll(
                Map.of(FlagCodes.CF0003.name(), mappingValues[5])
            );
        }
    }

    private void urgentCase(SscsCaseData caseData) {
        var urgentCase = caseData.getUrgentCase();
        if (urgentCase != null) {
            caseFlags.putAll(
                Map.of(FlagCodes.CF0007.name(), mappingValues[6])
            );
        }
    }

    private void adjournCaseInterpreterLanguage(SscsCaseData caseData) {
        var adjournCaseInterpreterLanguage = caseData.getAdjournCaseInterpreterLanguage();
        if (adjournCaseInterpreterLanguage != null) {
            caseFlags.putAll(
                Map.of(FlagCodes.PF0015.name(), mappingValues[7])
            );
        }
    }
}
