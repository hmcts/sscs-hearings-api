package uk.gov.hmcts.reform.sscs.model.hmcmessage;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum HearingListingStatus {
    @JsonProperty("Draft")
    DRAFT,
    @JsonProperty("Provisional")
    PROVISIONAL,
    @JsonProperty("Fixed")
    FIXED
}
