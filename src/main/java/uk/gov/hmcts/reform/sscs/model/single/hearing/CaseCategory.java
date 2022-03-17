package uk.gov.hmcts.reform.sscs.model.single.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.validator.EnumPattern;

@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseCategory {

    @EnumPattern(enumClass = CaseCategoryType.class, fieldName = "categoryType")
    private String categoryType;

    private String categoryValue;
}
