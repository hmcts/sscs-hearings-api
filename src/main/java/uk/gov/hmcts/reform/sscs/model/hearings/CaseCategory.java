package uk.gov.hmcts.reform.sscs.model.hearings;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.exceptions.ValidationError;
import uk.gov.hmcts.reform.sscs.validator.EnumPattern;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseCategory {

    @NotEmpty(message = ValidationError.CATEGORY_TYPE_EMPTY)
    @EnumPattern(enumClass = CaseCategoryType.class, fieldName = "categoryType")
    private String categoryType;

    @NotEmpty(message = ValidationError.CATEGORY_VALUE_EMPTY)
    @Size(max = 70, message = ValidationError.CATEGORY_VALUE)
    private String categoryValue;
}
