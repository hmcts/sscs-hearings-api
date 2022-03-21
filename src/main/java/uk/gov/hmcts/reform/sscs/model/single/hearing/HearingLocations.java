package uk.gov.hmcts.reform.sscs.model.single.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.validator.EnumPattern;

@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingLocations {

    private String locationType;

    @EnumPattern(enumClass = LocationType.class, fieldName = "locationId")
    private String locationId;
}
