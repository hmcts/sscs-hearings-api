package uk.gov.hmcts.reform.sscs.model.single.hearing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.sscs.validator.EnumPattern;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Data
@RequiredArgsConstructor
@Builder
public class HmcHearingResponse {

    @JsonProperty("hearingRequestID")
    private Long hearingRequestId;

    private String status;

    private LocalDateTime timeStamp;

    private long versionNumber;

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
	
}
