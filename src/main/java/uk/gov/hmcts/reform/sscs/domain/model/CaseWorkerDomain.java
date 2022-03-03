package uk.gov.hmcts.reform.sscs.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class CaseWorkerDomain {
    @JsonIgnore
    protected long rowId;
}
