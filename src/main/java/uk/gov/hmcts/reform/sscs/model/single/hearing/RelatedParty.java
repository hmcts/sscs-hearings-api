package uk.gov.hmcts.reform.sscs.model.single.hearing;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@Builder
public class RelatedParty {

    private String relatedPartyID;

    private String relationshipType;

}
