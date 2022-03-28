package uk.gov.hmcts.reform.sscs.model.single.hearing;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Data
@RequiredArgsConstructor
@Builder
public class UnavailabilityRange {

    private LocalDate unavailableFromDate;

    private LocalDate unavailableToDate;
}
