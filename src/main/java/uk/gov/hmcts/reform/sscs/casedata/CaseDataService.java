package uk.gov.hmcts.reform.sscs.casedata;

import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

public interface CaseDataService {

    public void setHearingID(SscsCaseData sscsCaseData, String hearingID);
}
