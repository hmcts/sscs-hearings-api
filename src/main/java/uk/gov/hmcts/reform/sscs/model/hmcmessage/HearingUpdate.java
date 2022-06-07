package uk.gov.hmcts.reform.sscs.model.hmcmessage;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HearingUpdate {

    @JsonProperty("HMCStatus")
    private String hmcStatus;

    @JsonProperty("ListAssistCaseStatus")
    private String listAssistCaseStatus;

    private String hearingResponseReceivedDateTime;
    private String hearingEventBroadcastDateTime;

    private HearingListingStatus hearingListingStatus;
    private String nextHearingDate;

    private String listAssistSessionID;

    private String hearingVenueId;

    private String hearingRoomId;

    private String hearingJudgeId;

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
