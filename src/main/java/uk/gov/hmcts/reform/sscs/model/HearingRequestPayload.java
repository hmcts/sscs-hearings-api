package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.exception.ValidationError;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingRequestPayload {

    @Valid
    @NotNull(message = ValidationError.INVALID_REQUEST_DETAILS)
    private RequestDetails requestDetails;

    @Valid
    @NotNull(message = ValidationError.INVALID_HEARING_DETAILS)
    private HearingDetails hearingDetails;

    @Valid
    @NotNull(message = ValidationError.INVALID_CASE_DETAILS)
    private CaseDetails caseDetails;

    @Valid
    private List<PartyDetails> partyDetails;
}
