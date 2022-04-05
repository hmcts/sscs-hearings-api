package uk.gov.hmcts.reform.sscs.model.servicehearingvalues;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RequirementType;
import uk.gov.hmcts.reform.sscs.validator.EnumPattern;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class PanelPreference {

    private String memberID;
    @JsonProperty("SvhMemberType")
    private MemberType memberType;
    @JsonProperty("SvhRequirementType")
    @EnumPattern(enumClass = RequirementType.class, fieldName = "requirementType")
    private RequirementType requirementType;
}
