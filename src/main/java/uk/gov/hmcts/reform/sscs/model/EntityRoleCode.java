package uk.gov.hmcts.reform.sscs.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
@Getter
@RequiredArgsConstructor
public enum EntityRoleCode {
    // TODO SSCS-10273 - Replace with commons object
    APPELLANT("BBA3-appellant", "Applicant", "Appellant", "", "BBA3"),
    APPOINTEE("BBA3-appointee", "?", "Appointee", "", "BBA3"),
    JOINT_PARTY("BBA3-jointParty", "Applicant", "Joint Party", "", "BBA3"),
    OTHER_PARTY("BBA3-otherParty", "Respondent", "Other Party", "", "BBA3"),
    RESPONDENT("BBA3-respondent", "Respondent", "Respondent", "", "BBA3"),
    REPRESENTATIVE("BBA3-representative", "Representative", "Representative", "", "BBA3"),
    WELFARE_REPRESENTATIVE("BBA3-welfareRepresentative", "Representative", "Welfare Representative", "", "BBA3"),
    LEGAL_REPRESENTATIVE("BBA3-legalRepresentative", "Representative", "Legal Representative", "", "BBA3"),
    BARRISTER("BBA3-barrister", "Representative", "Barrister", "", "BBA3"),
    INTERPRETER("BBA3-interpreter", "Interpreter", "Interpreter", "", "BBA3");

    private final String key;
    private final String parentRole;
    private final String valueEN;
    private final String valueCY;
    private final String serviceCode;
}
