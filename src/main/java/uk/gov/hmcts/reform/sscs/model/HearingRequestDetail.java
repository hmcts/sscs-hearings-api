package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingRequestDetail {

    @JsonProperty("hearingRequestID")
    private String hearingRequestID;
    @JsonProperty("status")
    private String status;
    @JsonProperty("timeStamp")
    private String timeStamp;
    @JsonProperty("versionNumber")
    private String versionNumber;


    public String getHearingRequestID() {
        return hearingRequestID;
    }

    public void setHearingRequestID(String hearingRequestID) {
        this.hearingRequestID = hearingRequestID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }
}
