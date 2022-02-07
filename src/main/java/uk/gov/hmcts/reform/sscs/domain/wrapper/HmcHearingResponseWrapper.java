package uk.gov.hmcts.reform.sscs.domain.wrapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import uk.gov.hmcts.reform.sscs.ccd.domain.Hearing;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class HmcHearingResponseWrapper {  // /hearing/ (POST), /hearing/ (PUT) and /hearing/ (DELETE)  - response

    @JsonProperty("hearingRequestID")
    private String hearingRequestId;
    @JsonProperty("status")
    private String status;
    @JsonProperty("timeStamp")
    private LocalDateTime timeStamp;
    @JsonProperty("versionNumber")
    private Number versionNumber;
}
