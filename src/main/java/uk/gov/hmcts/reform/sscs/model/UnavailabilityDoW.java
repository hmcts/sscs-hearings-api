package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.sscs.validator.EnumPattern;

@Builder(toBuilder = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UnavailabilityDoW {

    @JsonProperty("DOW")
    @EnumPattern(enumClass = Dow.class, fieldName = "dow")
    private String dow;

    @JsonProperty("DOWUnavailabilityType")
    @EnumPattern(enumClass = DowUnavailabilityType.class, fieldName = "dowUnavailabilityType")
    private String dowUnavailabilityType;
}
