package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RelatedParty {

    @JsonProperty("relatedPartyID")
    private String relatedPartyID;
    @JsonProperty("relationshipType")
    private String relationshipType;

    public String getRelatedPartyID() {
        return relatedPartyID;
    }

    public void setRelatedPartyID(String relatedPartyID) {
        this.relatedPartyID = relatedPartyID;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }
}
