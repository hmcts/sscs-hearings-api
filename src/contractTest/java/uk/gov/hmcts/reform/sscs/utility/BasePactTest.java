package uk.gov.hmcts.reform.sscs.utility;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;

public class BasePactTest {

    private static final String STATUS_OPTIONS_STRING = "HEARING_REQUESTED|UPDATE_REQUESTED|"
         + "UPDATE_SUBMITTED|AWAITING_LISTING|LISTED|CANCELLATION_REQUESTED|EXCEPTION";


    public  PactDslJsonBody generateHearingsJsonBody(String statusMessage, String responseStatus) {
        return genericHearingJsonBody(statusMessage, responseStatus);
    }

    private  PactDslJsonBody genericHearingJsonBody(String statusMessage, String responseStatus) {
        PactDslJsonBody pactDslJsonBody = new PactDslJsonBody();

        addStatusMessage(pactDslJsonBody, statusMessage);

        pactDslJsonBody
            .integerType("versionNumber", "123")
            .integerType("hearingRequestID", "^[a-zA-Z0-9]{1,30}$", "1880163574")
            .stringMatcher("status", STATUS_OPTIONS_STRING, responseStatus)
            .stringMatcher("timeStamp", "2030-08-20T12:40:00")
            .asBody();

        return pactDslJsonBody;
    }

    protected  void addStatusMessage(PactDslJsonBody pactDslJsonBody, String statusMessage) {
        pactDslJsonBody
            .stringType("status_message", statusMessage);
    }
}
