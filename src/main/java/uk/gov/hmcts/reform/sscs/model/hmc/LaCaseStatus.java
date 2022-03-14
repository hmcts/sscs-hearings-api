package uk.gov.hmcts.reform.sscs.model.hmc;

import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;

@Getter
public enum LaCaseStatus {
    CASE_CREATED("Case Created"),
    AWAITING_LISTING("Awaiting Listing"),
    LISTED("Listed"),
    PENDING_RELISTING("Pending Relisting"),
    HEARING_COMPLETED("Hearing Completed"),
    CASE_CLOSED("Case Closed");

    private final String laCaseStatusLabel;


    LaCaseStatus(String laCaseStatusLabel) {
        this.laCaseStatusLabel = laCaseStatusLabel;
    }


    public static LaCaseStatus getByLabel(String label) {
        return Arrays.stream(LaCaseStatus.values())
            .filter(eachLaCaseStatus -> eachLaCaseStatus.toString().toLowerCase(Locale.ROOT)
                .equals(label.toLowerCase(Locale.ROOT))).findAny().orElse(null);
    }


}
