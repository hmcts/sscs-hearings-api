package uk.gov.hmcts.reform.sscs.domain.wrapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class HmcHearingWrapper {  // /hearing/ (GET) - response

    @JsonProperty("requestDetails")
    private RequestDetails requestDetails;
    @JsonProperty("hearingDetails")
    private HearingDetails hearingDetails;
    @JsonProperty("caseDetails")
    private CaseDetails caseDetails;
    @JsonProperty("partyDetails")
    private List<Party> partyDetails;
    @JsonProperty("hearingResponse")
    private HearingResponse hearingResponse;
}
