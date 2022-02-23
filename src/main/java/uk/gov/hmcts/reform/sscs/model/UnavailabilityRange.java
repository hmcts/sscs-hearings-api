package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UnavailabilityRange {

    @JsonProperty("unavailableFromDate")
    private String unavailableFromDate;
    @JsonProperty("unavailableToDate")
    private String unavailableToDate;

    public String getUnavailableFromDate() {
        return unavailableFromDate;
    }

    public void setUnavailableFromDate(String unavailableFromDate) {
        this.unavailableFromDate = unavailableFromDate;
    }

    public String getUnavailableToDate() {
        return unavailableToDate;
    }

    public void setUnavailableToDate(String unavailableToDate) {
        this.unavailableToDate = unavailableToDate;
    }
}
