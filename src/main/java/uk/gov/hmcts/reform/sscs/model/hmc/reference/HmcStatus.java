package uk.gov.hmcts.reform.sscs.model.hmc.reference;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingStatus;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingEventMappers;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;

import java.util.function.Function;

import static uk.gov.hmcts.reform.sscs.ccd.domain.EventType.HEARING_BOOKED;
import static uk.gov.hmcts.reform.sscs.ccd.domain.EventType.LISTING_ERROR;
import static uk.gov.hmcts.reform.sscs.ccd.domain.EventType.UPDATE_CASE_ONLY;

@RequiredArgsConstructor
@Getter
public enum HmcStatus {
    HEARING_REQUESTED("Hearing requested", null, HearingStatus.AWAITING_LISTING, "", ""),
    AWAITING_LISTING("Awaiting listing", response -> UPDATE_CASE_ONLY, HearingStatus.AWAITING_LISTING, "Awaiting Listing ",
        "Hearing is waiting to be listed"),
    LISTED("Listed", response -> HEARING_BOOKED, HearingStatus.LISTED, "Hearing Listed",
        "New hearing %s has been listed and added to case"),
    UPDATE_REQUESTED("Update requested", null, null, "", ""),
    UPDATE_SUBMITTED("Update submitted", response -> UPDATE_CASE_ONLY, null, "Hearing Updated",
        "The hearing with id %s has been updated and has been updated on the case"),
    EXCEPTION("Exception", response -> LISTING_ERROR, HearingStatus.EXCEPTION, "Hearing Exception",
        "An error has occurred when trying to process the hearing with id %s"),
    CANCELLATION_REQUESTED("Cancellation requested", null, null, "", ""),
    CANCELLATION_SUBMITTED("Cancellation submitted", null, null, "", ""),
    CANCELLED("Cancelled", HearingEventMappers.dormantHandler(), HearingStatus.CANCELLED, "Hearing Cancelled.",
        "The hearing with id %s has been successfully cancelled"),
    AWAITING_ACTUALS("Awaiting Actuals", null, HearingStatus.AWAITING_ACTUALS, "", ""),
    COMPLETED("Completed", null, HearingStatus.COMPLETED, "", ""),
    ADJOURNED("Adjourned", null, HearingStatus.ADJOURNED, "", "");

    private final String label;
    private final Function<HearingGetResponse, EventType> eventMapper;
    private final HearingStatus hearingStatus;
    private final String ccdUpdateSummary;
    private final String ccdUpdateDescription;

}
