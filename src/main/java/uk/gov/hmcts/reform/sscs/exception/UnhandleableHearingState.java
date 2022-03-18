package uk.gov.hmcts.reform.sscs.exception;

import uk.gov.hmcts.reform.sscs.model.HearingState;

import static java.util.Objects.nonNull;

public class UnhandleableHearingState extends Exception {
    private static final long serialVersionUID = 4010841641319292161L;

    public UnhandleableHearingState(HearingState hearingState) {
        super(String.format("Unable to handle Hearing State: %s",
                nonNull(hearingState) ? hearingState.getDescription() : "null"));
    }
}
