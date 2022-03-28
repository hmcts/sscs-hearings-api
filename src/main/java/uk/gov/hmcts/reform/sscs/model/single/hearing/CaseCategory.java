package uk.gov.hmcts.reform.sscs.model.single.hearing;

import lombok.*;
import uk.gov.hmcts.reform.sscs.validator.EnumPattern;

@Data
@RequiredArgsConstructor
@Builder
public class CaseCategory {

    @EnumPattern(enumClass = CaseCategoryType.class, fieldName = "categoryType")
    private String categoryType;

    private String categoryValue;
}
