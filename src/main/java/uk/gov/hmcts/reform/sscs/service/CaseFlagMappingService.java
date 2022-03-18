package uk.gov.hmcts.reform.sscs.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.helpers.CaseFlagMappingServiceHelper;
import uk.gov.hmcts.reform.sscs.model.DescendantCaseData;

import javax.validation.constraints.NotNull;

@Service
public class CaseFlagMappingService {

    public DescendantCaseData updateHmcCaseData(@NotNull SscsCaseDataService caseDataService) {
        SscsCaseData caseData = caseDataService.getSscsCaseData();
        CaseFlagMappingServiceHelper helper = new CaseFlagMappingServiceHelper();
        return DescendantCaseData
            .DescendantCaseDataBuilder()
            .caseFlags(helper.mapCaseFlags(caseData))
            .build();
    }
}
