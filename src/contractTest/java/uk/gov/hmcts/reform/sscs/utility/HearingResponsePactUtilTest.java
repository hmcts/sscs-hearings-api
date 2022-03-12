package uk.gov.hmcts.reform.sscs.utility;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertTrue;


class HearingResponsePactUtilTest {

    @Test
    void getPostHearingsJsonBody() {
        PactDslJsonBody pactDslJsonBody = HearingResponsePactUtil.generatePostHearingsJsonBody("HEARING_REQUESTED");

        assertTrue(pactDslJsonBody.toString().contains("\"hearingRequestID\":"));
        assertTrue(pactDslJsonBody.toString().contains("\"status\":"));
        assertTrue(pactDslJsonBody.toString().contains("\"timeStamp\":"));
        assertTrue(pactDslJsonBody.toString().contains("\"versionNumber\":"));
    }

}
