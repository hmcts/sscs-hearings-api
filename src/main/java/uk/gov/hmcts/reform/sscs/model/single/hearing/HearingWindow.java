package uk.gov.hmcts.reform.sscs.model.single.hearing;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
@Builder
public class HearingWindow {

    private LocalDate dateRangeStart;
    private LocalDate dateRangeEnd;
    private LocalDateTime firstDateTimeMustBe;
}
