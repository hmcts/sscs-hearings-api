package uk.gov.hmcts.reform.sscs.model.hmc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.validator.EnumPattern;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UnavailabilityDoW {

    @JsonProperty("DOW")
    @EnumPattern(enumClass = Dow.class, fieldName = "dow")
    private String dow;

    @JsonProperty("DOWUnavailabilityType")
    @EnumPattern(enumClass = DowUnavailabilityType.class, fieldName = "dowUnavailabilityType")
    private String dowUnavailabilityType;
}
