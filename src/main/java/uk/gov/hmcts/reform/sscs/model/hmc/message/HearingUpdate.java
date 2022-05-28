package uk.gov.hmcts.reform.sscs.model.hmc.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.ListAssistCaseStatus;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.ListingStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HearingUpdate {

    private LocalDateTime hearingResponseReceivedDateTime;
    private LocalDateTime hearingEventBroadcastDateTime;
    private HmcStatus hmcStatus;
    @JsonProperty("hearingListingStatus")
    private ListingStatus listingStatus;
    private LocalDateTime nextHearingDate;
    private ListAssistCaseStatus listAssistCaseStatus;
    private String listAssistSessionID;
    private String hearingVenueId;
    private String hearingRoomId;
    private String hearingJudgeId;
}
