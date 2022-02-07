package uk.gov.hmcts.reform.sscs.domain.wrapper;

import java.util.List;

public class HearingDaySchedule {
    private String hearingStartDateTime;
    private String hearingEndDateTime;
    private String listAssistSessionID;
    private String hearingVenueId;
    private String hearingRoomId;
    private String hearingJudgeId;
    private List<String> panelMemberIds;
    private List<Attendee> attendees;
}
