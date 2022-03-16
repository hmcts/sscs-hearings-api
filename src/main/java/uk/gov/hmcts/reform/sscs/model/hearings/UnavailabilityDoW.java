package uk.gov.hmcts.reform.sscs.model.hearings;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.validator.EnumPattern;

@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UnavailabilityDoW {

    @JsonProperty("DOW")
    @EnumPattern(enumClass = DayOfWeek.class, fieldName = "dow")
    private String dayOfWeek;

    @JsonProperty("DOWUnavailabilityType")
    @EnumPattern(enumClass = DayOfWeekUnavailabilityType.class, fieldName = "dowUnavailabilityType")
    private String dowUnavailabilityType;
}
