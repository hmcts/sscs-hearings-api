package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.exceptions.ValidationError;
import uk.gov.hmcts.reform.sscs.validator.EnumPattern;

import javax.validation.constraints.NotEmpty;

@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingLocations {

    @NotEmpty(message = ValidationError.LOCATION_TYPE_EMPTY)
    private String locationType;

    @EnumPattern(enumClass = LocationType.class, fieldName = "locationId")
    private String locationId;
}
