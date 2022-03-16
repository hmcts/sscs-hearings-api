package uk.gov.hmcts.reform.sscs.model.hmc;

import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;

@Getter
public enum CaseCategoryType {

    CASETYPE("caseType"),
    CASESUBTYPE("caseSubType");

    private final String catagoryLabel;

    CaseCategoryType(String catagoryLabel) {
        this.catagoryLabel = catagoryLabel;
    }

    public static CaseCategoryType getByLabel(String label) {
        return Arrays.stream(CaseCategoryType.values())
            .filter(eachCategory -> eachCategory.toString().toLowerCase(Locale.ROOT)
                .equals(label.toLowerCase(Locale.ROOT))).findAny().orElse(null);

    }
}
