package uk.gov.hmcts.reform.sscs.model.hearings;

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

    @NotNull
    @Size(max = 30)
    private Long hearingRequestId;


    @NotNull
    @Size(max = 100)
    private String status;

    @NotNull
    private String timeStamp;

    @NotNull
    @Size(max = 100)
    private Integer versionNumber;
}
