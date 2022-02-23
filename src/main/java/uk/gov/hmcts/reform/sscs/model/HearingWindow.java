package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingWindow {

    @JsonProperty("dateRangeStart")
    private String dateRangeStart;
    @JsonProperty("dateRangeEnd")
    private String dateRangeEnd;
    @JsonProperty("firstDateTimeMustBe")
    private String firstDateTimeMustBe;

    public String getDateRangeStart() {
        return dateRangeStart;
    }

    public void setDateRangeStart(String dateRangeStart) {
        this.dateRangeStart = dateRangeStart;
    }

    public String getDateRangeEnd() {
        return dateRangeEnd;
    }

    public void setDateRangeEnd(String dateRangeEnd) {
        this.dateRangeEnd = dateRangeEnd;
    }

    public String getFirstDateTimeMustBe() {
        return firstDateTimeMustBe;
    }

    public void setFirstDateTimeMustBe(String firstDateTimeMustBe) {
        this.firstDateTimeMustBe = firstDateTimeMustBe;
    }
}
