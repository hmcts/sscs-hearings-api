package uk.gov.hmcts.reform.sscs.model.hearings;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingResponse {

    @JsonProperty("hearingRequestID")
    private Long hearingRequestId;

    private String status;

    private String timeStamp;

    private Integer versionNumber;
}
