package uk.gov.hmcts.reform.sscs.helpers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@SuppressWarnings("PMD")
public enum FlagCodes {

    RA0019("Step free / wheelchair access", 6),
    RA0042("Sign Language Interpreter", 10),
    RA0043("Hearing loop (hearing enhancement system)", 11),
    PF0004("Confidential address", 2),
    PF0007("Unacceptable customer behaviour ", 2),
    CF0003("Potentially harmful medical evidence", 1),
    CF0007("Urgent flag", 1),
    PF0015("Language Interpreter", 2);

    @SuppressWarnings("unused")
    FlagCodes(String valueEn, int catId) {
    }
}
