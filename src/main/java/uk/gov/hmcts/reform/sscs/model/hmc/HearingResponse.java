package uk.gov.hmcts.reform.sscs.model.hmc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.exception.ValidationError;
import uk.gov.hmcts.reform.sscs.validator.EnumPattern;

import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingResponse {

    @JsonProperty("hearingRequestID")
    @NotNull
    @Size(max = 30)
    private Long hearingRequestId;

    @NotNull
    @Size(max = 100)
    private String status;

    @NotNull
    private LocalDateTime timeStamp;

    @NotNull
    @Size(max = 100)
    private String versionNumber;

    @NotNull(message = ValidationError.LIST_ASSIST_TRANSACTION_ID_NULL_EMPTY)
    @Size(max = 30, message = ValidationError.LIST_ASSIST_TRANSACTION_ID_MAX_LENGTH)
    private String listAssistTransactionID;

    @NotNull(message = ValidationError.RECEIVED_DATE_TIME_NULL_EMPTY)
    private LocalDateTime receivedDateTime;

    @NotNull(message = ValidationError.RESPONSE_VERSION_NULL_EMPTY)
    private Integer responseVersion;

    @NotNull(message = ValidationError.LA_CASE_STATUS_NULL_EMPTY)
    @EnumPattern(enumClass = LaCaseStatus.class, fieldName = "laCaseStatus")
    private String laCaseStatus;

    @EnumPattern(enumClass = ListingStatus.class, fieldName = "listingStatus")
    private String listingStatus;

    @Size(max = 30, message = ValidationError.HEARING_CANCELLATION_REASON_MAX_LENGTH)
    private String hearingCancellationReason;

    @NotNull(message = ValidationError.HEARING_DAY_SCHEDULE_NULL_EMPTY)
    private List<HearingDaySchedule> hearingDaySchedule;

}
