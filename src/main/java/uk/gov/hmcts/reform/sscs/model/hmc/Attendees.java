package uk.gov.hmcts.reform.sscs.model.hmc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import uk.gov.hmcts.reform.sscs.exceptions.ValidationError;

import javax.validation.constraints.Size;

@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Attendees {
    @Size(max = 40, message = ValidationError.PARTY_ID_LENGTH)
    private String partyID;
    @Size(max = 60, message = ValidationError.HEARING_SUB_CHANNEL_MAX_LENGTH)
    private String hearingSubChannel;

}
