package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;


@Builder(toBuilder = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingWindow {

    private String dateRangeStart;
    private String dateRangeEnd;
    private String firstDateTimeMustBe;
}
