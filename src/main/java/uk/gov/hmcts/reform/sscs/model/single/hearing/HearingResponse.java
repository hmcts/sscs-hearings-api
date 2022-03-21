package uk.gov.hmcts.reform.sscs.model.single.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingResponse {

    @JsonProperty("hearingRequestID")
    private Long hearingRequestId;

    private String status;

    private LocalDateTime timeStamp;

    private Number versionNumber;
}
