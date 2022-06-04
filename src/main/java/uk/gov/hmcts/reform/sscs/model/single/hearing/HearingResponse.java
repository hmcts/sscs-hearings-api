package uk.gov.hmcts.reform.sscs.model.single.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.ListAssistCaseStatus;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.ListingStatus;

import java.time.LocalDateTime;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingResponse {

    @JsonProperty("hearingRequestID")
    private Long hearingRequestId;

    private String status;

    private LocalDateTime timeStamp;

    private Long versionNumber;

    private String listAssistTransactionID;

    private LocalDateTime receivedDateTime;

    @JsonProperty("laCaseStatus")
    private ListAssistCaseStatus listAssistCaseStatus;

    private ListingStatus listingStatus;

    private String hearingCancellationReason;

    private LocalDateTime partiesNotified;

    private Integer requestVersion;

    @JsonProperty("hearingDaySchedule")
    private List<HearingDaySchedule> hearingSessions;

    private JsonNode serviceData;

}
