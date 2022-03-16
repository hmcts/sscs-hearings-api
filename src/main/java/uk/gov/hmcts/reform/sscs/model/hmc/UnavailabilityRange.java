package uk.gov.hmcts.reform.sscs.model.hmc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.exception.ValidationError;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UnavailabilityRange {

    @NotNull(message = ValidationError.UNAVAILABLE_FROM_DATE_EMPTY)
    private String unavailableFromDate;

    @NotNull(message = ValidationError.UNAVAILABLE_TO_DATE_EMPTY)
    private String unavailableToDate;
}
