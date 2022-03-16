package uk.gov.hmcts.reform.sscs.model.hearings;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseDetails {

    private String hmctsServiceCode;

    private String caseRef;

    private String requestTimeStamp;

    private String externalCaseReference;

    private String caseDeepLink;

    private String hmctsInternalCaseName;

    private String publicCaseName;

    private Boolean caseAdditionalSecurityFlag;

    private Boolean caseInterpreterRequiredFlag;

    private List<CaseCategory> caseCategories;

    private String caseManagementLocationCode;

    @JsonProperty("caserestrictedFlag")
    private Boolean caseRestrictedFlag;

    @JsonProperty("caseSLAStartDate")
    private String caseSlaStartDate;

}
