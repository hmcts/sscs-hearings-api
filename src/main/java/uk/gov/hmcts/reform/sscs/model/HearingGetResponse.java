package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingGetResponse {
    private RequestDetails requestDetails;
    private HearingDetails hearingDetails;
    private CaseDetails caseDetails;
    private PartyDetails partyDetails;
    private HearingResponse hearingResponse;
}
