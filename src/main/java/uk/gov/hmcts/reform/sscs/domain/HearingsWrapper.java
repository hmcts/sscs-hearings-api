package uk.gov.hmcts.reform.sscs.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

@SuppressWarnings("checkstyle:RightCurly")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HearingsWrapper {
    private final String caseName;
    private final boolean autolistFlag;
    private final String hearingType;
    private final String caseType;
    private final String caseSubTypes;

    public HearingsWrapper(String caseName, boolean autolistFlag, String hearingType,
                           String caseType, String caseSubTypes) {
        this.caseName = caseName;
        this.autolistFlag = autolistFlag;
        this.hearingType = hearingType;
        this.caseType = caseType;
        this.caseSubTypes = caseSubTypes;
    }

    @ApiModelProperty(example = "Bugs v Elmer", required = true)
    @JsonProperty("caseName")
    public String getCaseName() {
        return caseName;
    }

    @ApiModelProperty(example = "true", required = true)
    @JsonProperty("autolistFlag")
    public boolean isAutolistFlag() {
        return autolistFlag;
    }

    @ApiModelProperty(example = "", required = true)
    @JsonProperty("hearingType")
    public String getHearingType() {
        return hearingType;
    }

    @ApiModelProperty(example = "Personal Independence Payment", required = true)
    @JsonProperty("caseType")
    public String getCaseType() {
        return caseType;
    }

    //Additional information about the case displayed on the first page
    @ApiModelProperty(example = "", required = true)
    @JsonProperty("caseSubTypes")
    public String getCaseSubTypes() {
        return caseSubTypes;
    }
}
