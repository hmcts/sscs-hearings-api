package uk.gov.hmcts.reform.sscs.model.single.hearing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ListingStatus {
    DRAFT("Draft"),
    PROVISIONAL("Provisional"),
    FIXED("Fixed");

    private final String listingStatusLabel;

}
