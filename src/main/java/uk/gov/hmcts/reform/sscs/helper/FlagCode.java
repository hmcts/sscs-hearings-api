package uk.gov.hmcts.reform.sscs.helper;

import lombok.Getter;

@Getter
public enum FlagCode {

    DISABLED_ACCESS("RA0019"),
    SIGN_LANGUAGE_TYPE("RA0042"),
    HEARING_LOOP("RA0043"),
    IS_CONFIDENTIAL_CASE("PF0004"),
    DWP_UCB("PF0007"),
    DWP_PHME("CF0003"),
    URGENT_CASE("CF0007"),
    AD_JOURN_CASE_INTERPRETER_LANGUAGE("PF0015");

    private final String flagCode;

    FlagCode(String flagCode) {
        this.flagCode = flagCode;
    }
}
