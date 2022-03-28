package uk.gov.hmcts.reform.sscs.model.single.hearing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;


@Data
@RequiredArgsConstructor
@Builder
public class HmcHearingResponse {

    @JsonProperty("hearingRequestID")
    private Long hearingRequestId;

    private String status;

    private LocalDateTime timeStamp;

    private Number versionNumber;
}
