package uk.gov.hmcts.reform.sscs.model.hearings;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UnavailabilityDayOfWeek {

    @JsonProperty("DOW")
    private String dayOfWeek;

    @JsonProperty("DOWUnavailabilityType")
    private String dayOfWeekUnavailabilityType;
}
