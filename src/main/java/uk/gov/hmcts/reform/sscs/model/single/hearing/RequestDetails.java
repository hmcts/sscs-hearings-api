package uk.gov.hmcts.reform.sscs.model.single.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestDetails {

    private Number versionNumber;

    private String hearingRequestID;

    private String status;

    private LocalDateTime timeStamp;

    private String hearingGroupRequestId;

    private LocalDateTime partiesNotified;


}
