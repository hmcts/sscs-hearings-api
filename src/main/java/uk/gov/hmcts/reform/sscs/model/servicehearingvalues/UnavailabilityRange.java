package uk.gov.hmcts.reform.sscs.model.servicehearingvalues;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UnavailabilityRange {

    private String unavailableFromDate;

    private String unavailableToDate;
}
