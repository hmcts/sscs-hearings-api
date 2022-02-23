package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jxl.write.DateTime;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestDetails {

    @JsonProperty("requestTimeStamp")
    private String requestTimeStamp;
    @JsonProperty("versionNumber")
    private Number versionNumber;

    public String getRequestTimeStamp() {
        return requestTimeStamp;
    }

    public void setRequestTimeStamp(String requestTimeStamp) {
        this.requestTimeStamp = requestTimeStamp;
    }

    public Number getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Number versionNumber) {
        this.versionNumber = versionNumber;
    }
}
