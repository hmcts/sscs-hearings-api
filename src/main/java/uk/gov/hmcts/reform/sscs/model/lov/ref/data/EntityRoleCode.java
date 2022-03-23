package uk.gov.hmcts.reform.sscs.model.lov.ref.data;

public enum EntityRoleCode {
    APPELLANT("BBA3-appellant", "Appellant"),
    APPOINTEE("BBA3-appointee", "Appointee"),
    JOINT_PARTY("BBA3-jointParty", "Joint Party"),
    OTHER_PARTY("BBA3-otherParty", "Other Party"),
    RESPONDENT("BBA3-respondent", "Respondent"),
    WELFARE_REPRESENTATIVE("BBA3-welfareRepresentative", "Welfare Representative"),
    LEGAL_REPRESENTATIVE("BBA3-legalRepresentative", "Legal Representative"),
    BARRISTER("BBA3-barrister", "Barrister"),
    INTERPRETER("BBA3-interpreter", "Interpreter");

    private final String key;
    private final String value;

    EntityRoleCode(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public static EntityRoleCode getEntityRoleCodeByValue(String value) {
        EntityRoleCode entityRoleCode = null;
        for (EntityRoleCode erc : EntityRoleCode.values()) {
            if (erc.getValue().equals(value)) {
                entityRoleCode = erc;
                break;
            }
        }
        return entityRoleCode;
    }

    public String getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }
}
