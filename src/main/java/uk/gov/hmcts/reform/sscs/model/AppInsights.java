package uk.gov.hmcts.reform.sscs.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class AppInsights {

    private String requestType;
    private String caseID;
    private LocalDateTime timeStamp;
    private String errorCode;
    private String errorMessage;
}
