package uk.gov.hmcts.reform.sscs.model.single.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingGetResponse {

    private RequestDetails requestDetails;

    private HearingDetails hearingDetails;

    private CaseDetails caseDetails;

    private List<PartyDetails> partyDetails;

    private HearingResponse hearingResponse;

}
