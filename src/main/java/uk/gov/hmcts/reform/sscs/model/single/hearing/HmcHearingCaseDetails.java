package uk.gov.hmcts.reform.sscs.model.single.hearing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
@Builder
public class HmcHearingCaseDetails {

    private String hmctsServiceCode;

    private String caseRef;

    private String externalCaseReference;

    private String caseDeepLink;

    private String hmctsInternalCaseName;

    private String publicCaseName;

    private boolean caseAdditionalSecurityFlag;

    private boolean caseInterpreterRequiredFlag;

    private List<CaseCategory> caseCategories;

    private String caseManagementLocationCode;

    @JsonProperty("caserestrictedFlag")
    private boolean caseRestrictedFlag;

    @JsonProperty("caseSLAStartDate")
    private String caseSlaStartDate;

}
