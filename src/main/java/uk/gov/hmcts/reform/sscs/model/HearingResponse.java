package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingResponse {

    @JsonProperty("hearingRequestID")
    @Size(max = 30)
    @NotNull
    private Long hearingRequestId;

    @Size(max = 100)
    @NotNull
    private String status;

    @NotNull
    private String timeStamp;

    @NotNull
    @Size(max = 100)
    private Integer versionNumber;
}
