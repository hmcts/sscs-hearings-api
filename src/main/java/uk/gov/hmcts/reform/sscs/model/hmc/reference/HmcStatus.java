package uk.gov.hmcts.reform.sscs.model.hmc.reference;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum HmcStatus {
    HEARING_REQUESTED("Hearing requested"),
    AWAITING_LISTING("Awaiting listing"),
    LISTED("Listed"),
    UPDATE_REQUESTED("Update requested"),
    UPDATE_SUBMITTED("Update submitted"),
    EXCEPTION("Exception"),
    CANCELLATION_REQUESTED("Cancellation requested"),
    CANCELLED("Cancelled"),
    AWAITING_ACTUALS("Awaiting Actuals"),
    COMPLETED("Completed"),
    ADJOURNED("Adjourned");

    @JsonValue
    private final String state;
}