package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingWindow {

    private String dateRangeStart;
    private String dateRangeEnd;
    private String firstDateTimeMustBe;
}
