package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.sscs.exceptions.ValidationError;
import uk.gov.hmcts.reform.sscs.validator.EnumPattern;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Builder(toBuilder = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PanelPreference {

    @NotEmpty(message = ValidationError.MEMBER_ID_EMPTY)
    @Size(max = 70, message = ValidationError.MEMBER_ID_MAX_LENGTH)
    private String memberID;

    @Size(max = 70, message = ValidationError.MEMBER_TYPE_MAX_LENGTH)
    private String memberType;

    @EnumPattern(enumClass = RequirementType.class, fieldName = "requirementType")
    private String requirementType;
}
