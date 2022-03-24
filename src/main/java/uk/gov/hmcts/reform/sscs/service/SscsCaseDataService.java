package uk.gov.hmcts.reform.sscs.service;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

@Getter
@Builder
public class SscsCaseDataService {
    private SscsCaseData sscsCaseData;
}
