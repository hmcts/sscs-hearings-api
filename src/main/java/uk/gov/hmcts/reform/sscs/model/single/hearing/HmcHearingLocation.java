package uk.gov.hmcts.reform.sscs.model.single.hearing;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.sscs.validator.EnumPattern;

@Data
@RequiredArgsConstructor
@Builder
public class HmcHearingLocation {

    private String locationType;

    @EnumPattern(enumClass = LocationType.class, fieldName = "locationId")
    private String locationId;
}
