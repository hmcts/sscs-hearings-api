package uk.gov.hmcts.reform.sscs.domain.wrapper;

import uk.gov.hmcts.reform.sscs.ccd.domain.HmcCaseCategory;

import java.time.LocalDateTime;
import java.util.List;

public class CaseDetails {

    private String hmctsServiceCode;
    private String caseRef;
    private LocalDateTime requestTimeStamp;
    private String externalCaseReference;
    private String caseDeepLink;
    private String hmctsInternalCaseName;
    private String publicCaseName;
    private boolean caseAdditionalSecurityFlag;
    private boolean caseInterpreterRequiredFlag;
    private List<HmcCaseCategory> caseCategories;
    private String caseManagementLocationCode;
    private boolean caserestrictedFlag;
    private String caseSLAStartDate;
}
