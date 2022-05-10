package uk.gov.hmcts.reform.sscs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
@AllArgsConstructor
@Data
public class HmcFailureMessage implements Message {

    private String requestType;
    private Long caseID;
    private LocalDateTime timeStamp;
    private String errorCode;
    private String errorMessage;
    private String body;
}
