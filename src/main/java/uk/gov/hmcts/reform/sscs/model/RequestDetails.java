package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.sscs.exceptions.ValidationError;

import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestDetails {

    @NotNull(message = ValidationError.REQUEST_TIMESTAMP_NULL_EMPTY)
    private LocalDateTime requestTimeStamp;
    @NotNull(message =  ValidationError.REQUEST_VERSION_NUMBER_NULL_EMPTY)
    private Integer versionNumber;

}
