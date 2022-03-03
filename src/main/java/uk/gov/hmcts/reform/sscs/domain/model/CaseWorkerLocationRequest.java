package uk.gov.hmcts.reform.sscs.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.sscs.utils.TrimStringFields;

@Getter
@Setter
@Builder(builderMethodName = "caseWorkersLocationRequest")
public class CaseWorkerLocationRequest {

    @JsonProperty("location_id")
    private Integer locationId;
    @JsonProperty("location")
    @JsonDeserialize(using = TrimStringFields.class)
    private String location;
    @JsonProperty("is_primary")
    private boolean isPrimaryFlag;

    @JsonCreator
    public CaseWorkerLocationRequest(@JsonProperty("location_id") Integer locationId,
                              @JsonProperty("location") String location,
                              @JsonProperty("is_primary") boolean isPrimaryFlag) {

        this.locationId = locationId;
        this.location = location;
        this.isPrimaryFlag = isPrimaryFlag;
    }

}
