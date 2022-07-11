package uk.gov.hmcts.reform.sscs.model.hmc.reference;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.sscs.helper.deserializer.CustomListingStatusDeserializer;

@Getter
@RequiredArgsConstructor
@JsonDeserialize(using = CustomListingStatusDeserializer.class)
public enum ListingStatus {
    DRAFT("Draft"),
    PROVISIONAL("Provisional"),
    FIXED("Fixed"),
    CANCEL("Cancel"),
    NONE("None");

    private final String label;
}
