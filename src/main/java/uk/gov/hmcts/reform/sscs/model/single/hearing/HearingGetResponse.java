package uk.gov.hmcts.reform.sscs.model.single.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingGetResponse {

    @NonNull
    private RequestDetails requestDetails;

    @NonNull
    private HearingDetails hearingDetails;

    @NonNull
    private CaseDetails caseDetails;

    @NonNull
    private List<PartyDetails> partyDetails;

    @NonNull
    private HearingResponse hearingResponse;

}
