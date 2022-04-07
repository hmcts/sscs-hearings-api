package uk.gov.hmcts.reform.sscs.messaging;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
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
