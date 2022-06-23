package uk.gov.hmcts.reform.sscs.model.service.linkedcases;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceLinkedCases {
    private String caseReference;
    private String caseName;
    private List<String> reasonsForLink;
}
