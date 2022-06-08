package uk.gov.hmcts.reform.sscs.model.hmc.reference;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.sscs.ccd.domain.State;

import static uk.gov.hmcts.reform.sscs.ccd.domain.State.HEARING;
import static uk.gov.hmcts.reform.sscs.ccd.domain.State.READY_TO_LIST;

@Getter
@RequiredArgsConstructor
public enum ListAssistCaseStatus {
    CASE_CREATED("Case Created", null),
    AWAITING_LISTING("Awaiting Listing", READY_TO_LIST),
    LISTED("Listed", HEARING),
    PENDING_RELISTING("Pending Relisting", null),
    HEARING_COMPLETED("Hearing Completed", null),
    CASE_CLOSED("Case Closed", null);

    private final String label;
    private final State caseStateUpdate;
}

