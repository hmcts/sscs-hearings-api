package uk.gov.hmcts.reform.sscs.model.single.hearing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CaseCategoryType {

    CATEGORY_TYPE("caseType"),
    CASE_SUBTYPE("caseSubType");

    private final String categoryLabel;
}
