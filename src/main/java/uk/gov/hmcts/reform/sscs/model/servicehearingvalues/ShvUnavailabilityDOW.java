package uk.gov.hmcts.reform.sscs.model.servicehearingvalues;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class ShvUnavailabilityDOW {

    @JsonProperty("DOW")
    private DOW dow;
    @JsonProperty("DOWUnavailabilityType")
    private DOWUnavailabilityType dowUnavailabilityType;
}
