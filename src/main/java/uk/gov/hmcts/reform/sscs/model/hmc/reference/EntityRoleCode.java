package uk.gov.hmcts.reform.sscs.model.hmc.reference;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public enum EntityRoleCode {
    APPELLANT("APEL", "Applicant", "Appellant", "", null),
    APPOINTEE("APIN", "Appointee", "Appointee", "", null),
    JOINT_PARTY("JOPA", "Applicant", "Joint Party", "", null),
    OTHER_PARTY("OTPA", "Respondent", "Other Party", "", null),
    RESPONDENT("RESP", "Respondent", "Respondent", "", null),
    WELFARE_REPRESENTATIVE("WERP", "Representative", "Welfare Representative", "", PartyRelationshipType.SOLICITOR),
    LEGAL_REPRESENTATIVE("LGRP", "Representative", "Legal Representative", "", PartyRelationshipType.SOLICITOR),
    BARRISTER("BARR", "Representative", "Barrister", "", PartyRelationshipType.SOLICITOR),
    INTERPRETER("INTP", "Interpreter", "Interpreter", "", PartyRelationshipType.INTERPRETER),
    REPRESENTATIVE("RPTT", "Representative", "Barrister", "", PartyRelationshipType.SOLICITOR),
    SUPPORT("SUPP", "Support", "Support", "", null),
    APPLICANT("APPL", "Applicant", "Applicant", "", null);

    private final String hmcReference;
    private final String parentRole;
    private final String valueEn;
    private final String valueCy;
    private final PartyRelationshipType partyRelationshipType;

}
