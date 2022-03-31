package uk.gov.hmcts.reform.sscs.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HearingsRequestMappingTest {

    private static final long HEARING_REQUEST_ID = 12345;
    private static final String HMC_STATUS = "TestStatus";
    private static final long VERSION = 1;
    private static final String CANCELLATION_REASON_CODE = "TestCancelCode";
    private static final long CASE_ID = 1625080769409918L;
    private static final long MISSING_CASE_ID = 99250807409918L;

    @DisplayName("When a valid hearing wrapper is given buildHearingRequestDetails returns the correct Hearing Request")
    @Test
    void buildHearingRequestDetails() {
        // TODO Finish Test when method done
    }
}
