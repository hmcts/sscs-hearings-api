package uk.gov.hmcts.reform.sscs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HearingUpdate {

    private String hearingResponseReceivedDateTime;
    private String hearingEventBroadcastDateTime;
    private String hmcStatus;
    private String hearingListingStatus;
    private String nextHearingDate;
    private String listAssistCaseStatus;
    private String listAssistSessionID;
    private String hearingVenueID;
    private String hearingRoomID;
    private String hearingJudgeID;
}
