package uk.gov.hmcts.reform.sscs.model.single.hearing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
@Builder
public class HmcHearingRequestPayload {

    private HmcRequestDetails hmcRequestDetails;

    private HmcHearingDetails hmcHearingDetails;

    private HmcHearingCaseDetails hmcHearingCaseDetails;

    @JsonProperty("partyDetails")
    private List<PartyDetails> partiesDetails;
}
