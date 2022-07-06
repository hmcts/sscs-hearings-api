package uk.gov.hmcts.reform.sscs.model.service.linkedcases;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinkedCase {
    private String caseReference;
    private String caseName;
    private List<String> reasonsForLink;
}
