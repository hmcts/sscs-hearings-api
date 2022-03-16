package uk.gov.hmcts.reform.sscs.model.hmc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import uk.gov.hmcts.reform.sscs.exception.ValidationError;

import javax.validation.constraints.NotNull;
import java.util.List;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingGetResponse {

    @NotNull(message = ValidationError.REQUEST_DETAILS_NULL_EMPTY)
    private RequestDetails requestDetails;

    @NotNull(message = ValidationError.HEARING_DETAILS_NULL_EMPTY)
    private HearingDetails hearingDetails;

    @NotNull(message = ValidationError.CASE_DETAILS_NULL_EMPTY)
    private CaseDetails caseDetails;

    @NotNull(message = ValidationError.PARTY_DETAILS_LIST_NULL_EMPTY)
    private List<PartyDetails> partyDetails;

    @NotNull(message = ValidationError.HEARING_RESPONSE_NULL_EMPTY)
    private HearingResponse hearingResponse;

}
