package uk.gov.hmcts.reform.sscs.service;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

@Data
@Builder
public class SscsCaseDataService {
    private SscsCaseData sscsCaseData;
}
