package uk.gov.hmcts.reform.sscs.domain.wrapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class HmcHearingDeleteWrapper {  // /hearing/ (DELETE) - invocation

    @JsonProperty("cancellationReasonCode")
    private String cancellationReasonCode;
    @JsonProperty("versionNumber")
    private Number versionNumber;

}
