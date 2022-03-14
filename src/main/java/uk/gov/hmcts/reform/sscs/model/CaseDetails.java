package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;
import uk.gov.hmcts.reform.sscs.exception.ValidationError;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseDetails {

    @NotEmpty(message = ValidationError.HMCTS_SERVICE_CODE_EMPTY_INVALID)
    @Pattern(regexp = "^\\w{4}$", message = ValidationError.HMCTS_SERVICE_CODE_EMPTY_INVALID)
    private String hmctsServiceCode;

    @NotEmpty(message = ValidationError.CASE_REF_EMPTY)
    @Pattern(regexp = "^\\d{16}$", message = ValidationError.CASE_REF_INVALID)
    private String caseRef;

    @NotNull(message = ValidationError.REQUEST_TIMESTAMP_EMPTY)
    private LocalDateTime requestTimeStamp;

    @Size(max = 70, message = ValidationError.EXTERNAL_CASE_REFERENCE_MAX_LENGTH)
    private String externalCaseReference;

    @NotEmpty(message = ValidationError.CASE_DEEP_LINK_EMPTY)
    @Size(max = 1024, message = ValidationError.CASE_DEEP_LINK_MAX_LENGTH)
    @URL(message = ValidationError.CASE_DEEP_LINK_INVALID)
    private String caseDeepLink;

    @NotEmpty(message = ValidationError.HMCTS_INTERNAL_CASE_NAME_EMPTY)
    @Size(max = 1024, message = ValidationError.HMCTS_INTERNAL_CASE_NAME_MAX_LENGTH)
    private String hmctsInternalCaseName;

    @NotEmpty(message = ValidationError.PUBLIC_CASE_NAME_EMPTY)
    @Size(max = 1024, message = ValidationError.PUBLIC_CASE_NAME_MAX_LENGTH)
    private String publicCaseName;

    private Boolean caseAdditionalSecurityFlag;

    private Boolean caseInterpreterRequiredFlag;

    @Valid
    @NotNull(message = ValidationError.CASE_CATEGORY_EMPTY)
    @NotEmpty(message = ValidationError.INVALID_CASE_CATEGORIES)
    private List<CaseCategory> caseCategories;

    @NotEmpty(message = ValidationError.CASE_MANAGEMENT_LOCATION_CODE_EMPTY)
    @Size(max = 40, message = ValidationError.CASE_MANAGEMENT_LOCATION_CODE_MAX_LENGTH)
    private String caseManagementLocationCode;

    @JsonProperty("caserestrictedFlag")
    @NotNull(message = ValidationError.CASE_RESTRICTED_FLAG_NULL_EMPTY)
    private boolean caseRestrictedFlag;

    @JsonProperty("caseSLAStartDate")
    @NotNull(message = ValidationError.CASE_SLA_START_DATE_EMPTY)
    private LocalDate caseSlaStartDate;



}
