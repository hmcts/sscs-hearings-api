package uk.gov.hmcts.reform.sscs.model.single.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class HearingRequestPayload {

    private HearingDetails hearingDetails;

    private CaseDetails caseDetails;

    private List<PartyDetails> partyDetails;
}
