package uk.gov.hmcts.reform.sscs.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DeadLetter {

    private String requestType;
    private long caseID;
    private LocalDateTime timeStamp;
    private String errorCode;
    private String errorMessage;

    public DeadLetter(String requestType, long caseID, LocalDateTime timeStamp, String errorCode, String errorMessage) {
        this.requestType = requestType;
        this.caseID = caseID;
        this.timeStamp = timeStamp;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
