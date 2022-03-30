package uk.gov.hmcts.reform.sscs.model.single.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.validator.EnumPattern;

import java.time.LocalDateTime;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingResponse {

    @JsonProperty("hearingRequestID")
    private Long hearingRequestId;

    private String status;

    private LocalDateTime timeStamp;

    private Long versionNumber;

    private String listAssistTransactionID;

    private LocalDateTime receivedDateTime;

    @JsonProperty("laCaseStatus")
    @EnumPattern(enumClass = ListingCaseStatus.class, fieldName = "listingCaseStatus")
    private String listingCaseStatus;

    @EnumPattern(enumClass = ListingStatus.class, fieldName = "listingStatus")
    private String listingStatus;

    private String hearingCancellationReason;

    private LocalDateTime partiesNotified;

    private Integer requestVersion;

    private List<HearingDaySchedule> hearingDaySchedule;

    private JsonNode serviceData;

}
