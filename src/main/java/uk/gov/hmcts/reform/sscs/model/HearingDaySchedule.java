package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingDaySchedule {
    private String hearingStartDateTime;
    private String hearingEndDateTime;
    private String listAssistSessionID;
    private String hearingVenueId;
    private String hearingRoomId;
    private String hearingJudgeId;
    private String panelMemberId;
    private Attendees attendees;
}
