package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UnavailabilityDOW {

    @JsonProperty("DOW")
    private String dOW;
    @JsonProperty("DOWUnavailabilityType")
    private String dOWUnavailabilityType;

    public String getdOW() {
        return dOW;
    }

    public void setdOW(String dOW) {
        this.dOW = dOW;
    }

    public String getdOWUnavailabilityType() {
        return dOWUnavailabilityType;
    }

    public void setdOWUnavailabilityType(String dOWUnavailabilityType) {
        this.dOWUnavailabilityType = dOWUnavailabilityType;
    }

}
