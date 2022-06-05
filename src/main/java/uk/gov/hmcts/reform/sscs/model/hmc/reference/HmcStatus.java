package uk.gov.hmcts.reform.sscs.model.hmc.reference;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;

import static uk.gov.hmcts.reform.sscs.ccd.domain.EventType.HEARING_BOOKED;
import static uk.gov.hmcts.reform.sscs.ccd.domain.EventType.UPDATE_CASE_ONLY;

@RequiredArgsConstructor
@Getter
public enum HmcStatus {
    HEARING_REQUESTED("Hearing requested", null, "", ""),
    AWAITING_LISTING("Awaiting listing", null, "", ""),
    LISTED("Listed", HEARING_BOOKED, "Hearing Listed", "New hearing %s has been listed and added to case"),
    UPDATE_REQUESTED("Update requested", null, "", ""),
    UPDATE_SUBMITTED("Update submitted", UPDATE_CASE_ONLY, "Hearing Updated", "The hearing with id %s has been updated and has been updated on the case"),
    EXCEPTION("Exception", UPDATE_CASE_ONLY, "Hearing Exception", "An error has occurred when trying to process the hearing with id %s"),
    CANCELLATION_REQUESTED("Cancellation requested", null, "", ""),
    CANCELLED("Cancelled", UPDATE_CASE_ONLY, "Hearing Cancelled", "The hearing with id %s has been successfully cancelled"),
    AWAITING_ACTUALS("Awaiting Actuals", null, "", ""),
    COMPLETED("Completed", null, "", ""),
    ADJOURNED("Adjourned", null, "", "");

    private final String label;
    private final EventType ccdUpdateEventType;
    private final String ccdUpdateSummary;
    private final String ccdUpdateDescription;

}
