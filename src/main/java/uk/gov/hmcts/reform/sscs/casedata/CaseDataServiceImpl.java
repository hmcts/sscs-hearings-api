package uk.gov.hmcts.reform.sscs.casedata;

import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

public class CaseDataServiceImpl implements CaseDataService {

    public void setHearingID(SscsCaseData sscsCaseData, String hearingID){
        sscsCaseData.setHearingID(hearingID);
    }

}
