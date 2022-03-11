package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.exceptions.ValidationError;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UnavailabilityRange {

    @NotNull(message = ValidationError.UNAVAILABLE_FROM_DATE_EMPTY)
    private String unavailableFromDate;

    @NotNull(message = ValidationError.UNAVAILABLE_TO_DATE_EMPTY)
    private String unavailableToDate;
}
