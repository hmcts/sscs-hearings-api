package uk.gov.hmcts.reform.sscs.model.single.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class HearingDaySchedule {
    private LocalDateTime hearingStartDateTime;
    
    private LocalDateTime hearingEndDateTime;

    private String listAssistSessionID;

    private String hearingVenueId;

    private String hearingRoomId;

    private String hearingJudgeId;

    private String panelMemberId;

    private List<Attendees> attendees;
}
