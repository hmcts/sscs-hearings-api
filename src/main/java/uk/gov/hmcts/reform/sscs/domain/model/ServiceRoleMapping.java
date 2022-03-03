package uk.gov.hmcts.reform.sscs.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import uk.gov.hmcts.reform.sscs.utils.MappingField;
import uk.gov.hmcts.reform.sscs.utils.TrimStringFields;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRoleMapping extends CaseWorkerDomain {

    @MappingField(columnName = "Service ID", position = 1)
    @NotEmpty
    @JsonProperty(value = "service_code")
    @JsonDeserialize(using = TrimStringFields.class)
    String serviceId;

    @MappingField(columnName = "Role")
    @NotNull
    @JsonProperty(value = "role")
    Integer roleId;

    @MappingField(columnName = "IDAM Roles")
    @NotEmpty
    @JsonProperty(value = "idam_roles")
    @JsonDeserialize(using = TrimStringFields.class)
    String idamRoles;
}
