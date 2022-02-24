package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseDetails {

    private String hmctsServiceCode;
    private String caseRef;
    private String requestTimeStamp;
    private String externalCaseReference;
    private String caseDeepLink;
    private String hmctsInternalCaseName;
    private String publicCaseName;
    private String caseAdditionalSecurityFlag;
    private String caseInterpreterRequiredFlag;
    private CaseCategory caseCategories;
    private String caseManagementLocationCode;
    private boolean caserestrictedFlag;
    @JsonProperty("caseSLAStartDate")
    private String caseSlaStartDate;

}
