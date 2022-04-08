package uk.gov.hmcts.reform.sscs.model.single.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.validator.EnumPattern;

import java.time.LocalDateTime;
import java.util.List;


@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingResponse {

    @JsonProperty("hearingRequestID")
    private Long hearingRequestId;

    private String status;

    private LocalDateTime timeStamp;

    private Number versionNumber;

    private String listAssistTransactionID;

    private LocalDateTime receivedDateTime;

    private Integer responseVersion;

    @JsonProperty("laCaseStatus")
    @EnumPattern(enumClass = ListingCaseStatus.class, fieldName = "listingCaseStatus")
    private String listingCaseStatus;

    @EnumPattern(enumClass = ListingStatus.class, fieldName = "listingStatus")
    private String listingStatus;

    private String hearingCancellationReason;

    private List<HearingDaySchedule> hearingDaySchedule;

    private LocalDateTime partiesNotified;

    private Integer requestVersion;

    private ServiceData serviceData;

}
