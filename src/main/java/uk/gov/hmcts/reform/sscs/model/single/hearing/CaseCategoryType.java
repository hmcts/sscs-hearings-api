package uk.gov.hmcts.reform.sscs.model.single.hearing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CaseCategoryType {

    CASETYPE("caseType"),
    CASESUBTYPE("caseSubType");

    private final String catagoryLabel;
}
