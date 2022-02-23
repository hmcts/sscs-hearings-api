package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingPayload {

    @JsonProperty("requestDetails")
    private RequestDetails requestDetails;
    @JsonProperty("hearingDetails")
    private HearingDetails hearingDetails;
    @JsonProperty("caseDetails")
    private CaseDetails caseDetails;
    @JsonProperty("partyDetails")
    private PartyDetails partyDetails;

    public RequestDetails getRequestDetails() {
        return requestDetails;
    }

    public void setRequestDetails(RequestDetails requestDetails) {
        this.requestDetails = requestDetails;
    }

    public HearingDetails getHearingDetails() {
        return hearingDetails;
    }

    public void setHearingDetails(HearingDetails hearingDetails) {
        this.hearingDetails = hearingDetails;
    }

    public CaseDetails getCaseDetails() {
        return caseDetails;
    }

    public void setCaseDetails(CaseDetails caseDetails) {
        this.caseDetails = caseDetails;
    }

    public PartyDetails getPartyDetails() {
        return partyDetails;
    }

    public void setPartyDetails(PartyDetails partyDetails) {
        this.partyDetails = partyDetails;
    }
}
