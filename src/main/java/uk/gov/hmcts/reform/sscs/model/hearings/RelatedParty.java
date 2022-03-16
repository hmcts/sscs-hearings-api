package uk.gov.hmcts.reform.sscs.model.hearings;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RelatedParty {

    private String relatedPartyID;

    private String relationshipType;

}
