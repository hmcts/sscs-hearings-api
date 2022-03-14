package uk.gov.hmcts.reform.sscs.model.hmc;

import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;

@Getter
public enum ListingStatus {
    DRAFT("Draft"),
    PROVISIONAL("Provisional"),
    FIXED("Fixed");

    private final String listingStatusLabel;


    ListingStatus(String listingStatusLabel) {
        this.listingStatusLabel = listingStatusLabel;
    }


    public static ListingStatus getByLabel(String label) {
        return Arrays.stream(ListingStatus.values())
            .filter(eachListingStatus -> eachListingStatus.toString().toLowerCase(Locale.ROOT)
                .equals(label.toLowerCase(Locale.ROOT))).findAny().orElse(null);
    }
}
