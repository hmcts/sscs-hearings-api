package uk.gov.hmcts.reform.sscs.model.single.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingWindow {

    private LocalDate dateRangeStart;
    private LocalDate dateRangeEnd;
    private LocalDateTime firstDateTimeMustBe;
}
