package uk.gov.hmcts.reform.sscs.model.hmc.reference;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingEventMappers;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;

import java.util.function.Function;

import static uk.gov.hmcts.reform.sscs.ccd.domain.EventType.HEARING_BOOKED;
import static uk.gov.hmcts.reform.sscs.ccd.domain.EventType.LISTING_ERROR;
import static uk.gov.hmcts.reform.sscs.ccd.domain.EventType.UPDATE_CASE_ONLY;

@RequiredArgsConstructor
@Getter
public enum HmcStatus {
    HEARING_REQUESTED("Hearing requested", null, "", ""),
    AWAITING_LISTING("Awaiting listing", response -> UPDATE_CASE_ONLY, "Awaiting Listing ",
        "Hearing is waiting to be listed."),
    LISTED("Listed", response -> HEARING_BOOKED, "Hearing Listed",
        "New hearing has been listed and added to case."),
    UPDATE_REQUESTED("Update requested", null, "", ""),
    UPDATE_SUBMITTED("Update submitted", response -> UPDATE_CASE_ONLY, "Hearing Updated",
        "Hearing has been updated and has been updated on the case."),
    //Change to handling error
    EXCEPTION("Exception", response -> LISTING_ERROR, "Hearing Exception",
        "An error has occurred when trying to process the hearing with id. "),
    CANCELLATION_REQUESTED("Cancellation requested", null, "", ""),
    CANCELLED("Cancelled", HearingEventMappers.dormantHandler(), "Hearing Cancelled.",
        "A hearing has been successfully cancelled - Dormant."),
    AWAITING_ACTUALS("Awaiting Actuals", null, "", ""),
    COMPLETED("Completed", null, "", ""),
    ADJOURNED("Adjourned", null, "", "");

    private final String label;
    private final Function<HearingGetResponse, EventType> eventMapper;
    private final String ccdUpdateSummary;
    private final String ccdUpdateDescription;

}
