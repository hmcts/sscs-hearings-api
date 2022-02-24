package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UnavailabilityDoW {

    @JsonProperty("DOW")
    private String dow;
    @JsonProperty("DOWUnavailabilityType")
    private String dowUnavailabilityType;
}
