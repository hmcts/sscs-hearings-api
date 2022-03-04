package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.sscs.exceptions.ValidationError;
import uk.gov.hmcts.reform.sscs.validator.EnumPattern;

import javax.validation.constraints.NotEmpty;

@Builder(toBuilder = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingLocations {

    @NotEmpty(message = ValidationError.LOCATION_TYPE_EMPTY)
    private String locationType;

    @EnumPattern(enumClass = LocationType.class, fieldName = "locationId")
    private String locationId;
}
