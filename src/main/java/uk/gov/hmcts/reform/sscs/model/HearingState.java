package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum HearingState {
    // TODO Remove this when SSCS-10222 is deployed

    CREATE_HEARING("createHearing", "Create Hearing"),
    UPDATED_CASE("updatedCase", "Case Updated"),
    UPDATE_HEARING("updateHearing", "Update Hearing"),
    CANCEL_HEARING("cancelHearing", "Cancel Hearing"),
    PARTY_NOTIFIED("partyNotified", "Parties Notified"),
    PARTY_HEARING("partyHearing", "Parties Hearings");

    private final String state;
    private final String description;

    HearingState(String state, String description) {
        this.state = state;
        this.description = description;
    }

    @JsonValue
    public String getState() {
        return state;
    }
}
