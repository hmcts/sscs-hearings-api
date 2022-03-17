package uk.gov.hmcts.reform.sscs.model.single.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.validator.EnumPattern;

@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UnavailabilityDayOfWeek {

    @JsonProperty("DOW")
    @EnumPattern(enumClass = DayOfWeek.class, fieldName = "dayOfWeek")
    private String dayOfWeek;

    @JsonProperty("DOWUnavailabilityType")
    @EnumPattern(enumClass = DayOfWeekUnavailabilityType.class, fieldName = "dayOfWeekUnavailabilityType")
    private String dayOfWeekUnavailabilityType;
}
