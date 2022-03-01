package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingResponse {


    private String listAssistTransactionID;
    private String receivedDateTime;
    private Number responseVersion;
    private String laCaseStatus;
    private String listingStatus;
    private String hearingCancellationReason;
    private HearingDaySchedule hearingDaySchedule;
}
