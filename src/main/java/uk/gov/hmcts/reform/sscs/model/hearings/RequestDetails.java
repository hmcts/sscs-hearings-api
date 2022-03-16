package uk.gov.hmcts.reform.sscs.model.hearings;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.exceptions.ValidationError;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestDetails {

    @NotNull(message = ValidationError.REQUEST_TIMESTAMP_NULL_EMPTY)
    private String requestTimeStamp;

    @NotNull(message =  ValidationError.REQUEST_VERSION_NUMBER_NULL_EMPTY)
    private Integer versionNumber;

}
