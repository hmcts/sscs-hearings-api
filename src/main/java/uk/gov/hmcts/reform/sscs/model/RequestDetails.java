package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.exceptions.ValidationError;

import java.time.LocalDateTime;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestDetails {

    @NotNull(message = ValidationError.REQUEST_TIMESTAMP_NULL_EMPTY)
    private LocalDateTime requestTimeStamp;
    @NotNull(message =  ValidationError.REQUEST_VERSION_NUMBER_NULL_EMPTY)
    private Integer versionNumber;
    @NotNull(message = ValidationError.HEARING_REQUEST_ID_NULL_EMPTY)
    @Size(max = 30, message = ValidationError.HEARING_REQUEST_ID_MAX_LENGTH)
    private String hearingRequestID;
    @NotNull(message = ValidationError.STATUS_NULL_EMPTY)
    @Size(max = 30, message = ValidationError.STATUS_MAX_LENGTH)
    private String status;
    @NotNull(message = ValidationError.HEARING_REQUEST_RECEIVED_DATETIME_NULL_EMPTY)
    private LocalDateTime timeStamp;
    @Size(max = 30, message = ValidationError.HEARING_GROUP_REQUEST_ID_MAX_LENGTH)
    private String hearingGroupRequestId;
    private LocalDateTime partiesNotified;
}
