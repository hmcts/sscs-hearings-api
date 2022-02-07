package uk.gov.hmcts.reform.sscs.domain.wrapper;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class RequestDetails {
    @JsonProperty("hearingRequestID")
    private String hearingRequestId;
    @JsonProperty("status")
    private String status;
    @JsonProperty("timeStamp")
    private LocalDateTime timeStamp;
    @JsonProperty("versionNumber")
    private Number versionNumber;
    @JsonProperty("partiesNotified")
    private String partiesNotified;

}
