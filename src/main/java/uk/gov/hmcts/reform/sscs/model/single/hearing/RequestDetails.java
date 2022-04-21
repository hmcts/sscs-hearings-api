package uk.gov.hmcts.reform.sscs.model.single.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestDetails {

    private long versionNumber;

    private String hearingRequestID;

    private String status;

    private LocalDateTime timestamp;

    private String hearingGroupRequestId;

    private LocalDateTime partiesNotified;

}
