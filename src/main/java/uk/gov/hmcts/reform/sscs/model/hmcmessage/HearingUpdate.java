package uk.gov.hmcts.reform.sscs.model.hmcmessage;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
public class HearingUpdate {

    private String hearingResponseReceivedDateTime;
    private String hearingEventBroadcastDateTime;
    private String hmcStatus;
    private HearingListingStatus hearingListingStatus;
    private String nextHearingDate;
    private String listAssistCaseStatus;
    private String listAssistSessionID;
    private String hearingVenueId;
    private String hearingRoomId;
    private String hearingJudgeId;

    @SuppressWarnings("PMD.ExcessiveParameterList")
    public HearingUpdate(@JsonProperty("hearingResponseReceivedDateTime") String hearingResponseReceivedDateTime,
                         @JsonProperty("hearingEventBroadcastDateTime") String hearingEventBroadcastDateTime,
                         @JsonProperty("HMCStatus") String hmcStatus,
                         @JsonProperty("hearingListingStatus") HearingListingStatus hearingListingStatus,
                         @JsonProperty("nextHearingDate") String nextHearingDate,
                         @JsonProperty("ListAssistCaseStatus") String listAssistCaseStatus,
                         @JsonProperty("ListAssistSessionID") String listAssistSessionID,
                         @JsonProperty("hearingVenueId") String hearingVenueId,
                         @JsonProperty("hearingRoomId") String hearingRoomId,
                         @JsonProperty("hearingJudgeId") String hearingJudgeId
    ) {
        this.hearingResponseReceivedDateTime = hearingResponseReceivedDateTime;
        this.hearingEventBroadcastDateTime = hearingEventBroadcastDateTime;
        this.hmcStatus = hmcStatus;
        this.hearingListingStatus = hearingListingStatus;
        this.nextHearingDate = nextHearingDate;
        this.listAssistCaseStatus = listAssistCaseStatus;
        this.listAssistSessionID = listAssistSessionID;
        this.hearingVenueId = hearingVenueId;
        this.hearingRoomId = hearingRoomId;
        this.hearingJudgeId = hearingJudgeId;

    }

    public LocalDateTime getHearingResponseReceivedDateTime() {
        return LocalDateTime.parse(this.hearingEventBroadcastDateTime);
    }

    public LocalDateTime getHearingEventBroadcastDateTime() {
        return LocalDateTime.parse(this.hearingEventBroadcastDateTime);
    }

    public LocalDate getNextHearingDate() {
        return LocalDate.parse(this.nextHearingDate);
    }
}
