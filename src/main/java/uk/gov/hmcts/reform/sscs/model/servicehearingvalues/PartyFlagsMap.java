package uk.gov.hmcts.reform.sscs.model.servicehearingvalues;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PartyFlagsMap {

    DISABLED_ACCESS("21", "RA0019", "Step free / wheelchair access", "6"),
    SIGN_LANGUAGE_TYPE("44", "RA0042", "Sign Language Interpreter", "10"),
    HEARING_LOOP("45", "RA0043", "Hearing loop (hearing enhancement system)", "11"),
    IS_IDENTICAL_CASE("53", "PF0004", "Confidential address", "2"),
    DWP_UCB("56", "PF0007", "Unacceptable customer behaviour", "2"),
    DWP_PHME("63", "CF0003", "Potentially harmful medical evidence", "1"),
    URGENT_CASE("67", "CF0007", "Urgent flag", "1"),
    ADJOURN_CASE_INTERPRETER_LANGUAGE("70", "PF0015", "Language Interpreter", "2");

    private final String flagId;
    private final String flagCode;
    private final String flagDescription;
    private final String parentId;
}
