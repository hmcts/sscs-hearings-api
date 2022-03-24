package uk.gov.hmcts.reform.sscs.model.single.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class HmcHearingRequestPayload {

    private HmcRequestDetails hmcRequestDetails;

    private HmcHearingRequestDetails hmcHearingRequestDetails;

    private HmcHearingRequestCaseDetails hmcHearingRequestCaseDetails;

    private List<PartyDetails> partyDetails;
}
