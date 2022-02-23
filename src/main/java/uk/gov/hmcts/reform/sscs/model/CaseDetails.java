package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseDetails {

    @JsonProperty("hmctsServiceCode")
    private String hmctsServiceCode;
    @JsonProperty("caseRef")
    private String caseRef;
    @JsonProperty("requestTimeStamp")
    private String requestTimeStamp;
    @JsonProperty("externalCaseReference")
    private String externalCaseReference;
    @JsonProperty("caseDeepLink")
    private String caseDeepLink;
    @JsonProperty("hmctsInternalCaseName")
    private String hmctsInternalCaseName;
    @JsonProperty("publicCaseName")
    private String publicCaseName;
    @JsonProperty("caseAdditionalSecurityFlag")
    private String caseAdditionalSecurityFlag;
    @JsonProperty("caseInterpreterRequiredFlag")
    private String caseInterpreterRequiredFlag;
    @JsonProperty("caseCategories")
    private CaseCategory caseCategories;
    @JsonProperty("caseManagementLocationCode")
    private String caseManagementLocationCode;
    @JsonProperty("caserestrictedFlag")
    private boolean caserestrictedFlag;
    @JsonProperty("caseSLAStartDate")
    private String caseSLAStartDate;

    public String getHmctsServiceCode() {
        return hmctsServiceCode;
    }

    public void setHmctsServiceCode(String hmctsServiceCode) {
        this.hmctsServiceCode = hmctsServiceCode;
    }

    public String getCaseRef() {
        return caseRef;
    }

    public void setCaseRef(String caseRef) {
        this.caseRef = caseRef;
    }

    public String getRequestTimeStamp() {
        return requestTimeStamp;
    }

    public void setRequestTimeStamp(String requestTimeStamp) {
        this.requestTimeStamp = requestTimeStamp;
    }

    public String getExternalCaseReference() {
        return externalCaseReference;
    }

    public void setExternalCaseReference(String externalCaseReference) {
        this.externalCaseReference = externalCaseReference;
    }

    public String getCaseDeepLink() {
        return caseDeepLink;
    }

    public void setCaseDeepLink(String caseDeepLink) {
        this.caseDeepLink = caseDeepLink;
    }

    public String getHmctsInternalCaseName() {
        return hmctsInternalCaseName;
    }

    public void setHmctsInternalCaseName(String hmctsInternalCaseName) {
        this.hmctsInternalCaseName = hmctsInternalCaseName;
    }

    public String getPublicCaseName() {
        return publicCaseName;
    }

    public void setPublicCaseName(String publicCaseName) {
        this.publicCaseName = publicCaseName;
    }

    public String getCaseAdditionalSecurityFlag() {
        return caseAdditionalSecurityFlag;
    }

    public void setCaseAdditionalSecurityFlag(String caseAdditionalSecurityFlag) {
        this.caseAdditionalSecurityFlag = caseAdditionalSecurityFlag;
    }

    public String getCaseInterpreterRequiredFlag() {
        return caseInterpreterRequiredFlag;
    }

    public void setCaseInterpreterRequiredFlag(String caseInterpreterRequiredFlag) {
        this.caseInterpreterRequiredFlag = caseInterpreterRequiredFlag;
    }

    public CaseCategory getCaseCategories() {
        return caseCategories;
    }

    public void setCaseCategories(CaseCategory caseCategories) {
        this.caseCategories = caseCategories;
    }

    public String getCaseManagementLocationCode() {
        return caseManagementLocationCode;
    }

    public void setCaseManagementLocationCode(String caseManagementLocationCode) {
        this.caseManagementLocationCode = caseManagementLocationCode;
    }

    public boolean isCaserestrictedFlag() {
        return caserestrictedFlag;
    }

    public void setCaserestrictedFlag(boolean caserestrictedFlag) {
        this.caserestrictedFlag = caserestrictedFlag;
    }

    public String getCaseSLAStartDate() {
        return caseSLAStartDate;
    }

    public void setCaseSLAStartDate(String caseSLAStartDate) {
        this.caseSLAStartDate = caseSLAStartDate;
    }
}
