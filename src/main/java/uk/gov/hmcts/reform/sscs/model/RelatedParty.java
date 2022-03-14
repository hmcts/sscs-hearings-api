package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.exceptions.ValidationError;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RelatedParty {

    @NotEmpty(message = ValidationError.RELATED_PARTY_EMPTY)
    @Size(max = 15, message = ValidationError.RELATED_PARTY_MAX_LENGTH)
    private String relatedPartyID;

    @NotEmpty(message = ValidationError.RELATIONSHIP_TYPE_EMPTY)
    @Size(max = 10, message = ValidationError.RELATIONSHIP_TYPE_MAX_LENGTH)
    private String relationshipType;

}
