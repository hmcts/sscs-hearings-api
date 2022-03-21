package uk.gov.hmcts.reform.sscs.utility;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;

import java.time.LocalDateTime;

public class BasePactTest {

    private static final String STATUS_OPTIONS_STRING = "HEARING_REQUESTED|UPDATE_REQUESTED|"
         + "UPDATE_SUBMITTED|AWAITING_LISTING|LISTED|CANCELLATION_REQUESTED|EXCEPTION";

    private BasePactTest() {
    }

    public static PactDslJsonBody generateHearingsJsonBody(String statusMessage) {
        return genericCreateHearingJsonBody(statusMessage, LocalDateTime.now());
    }

    public static PactDslJsonBody genericCreateHearingJsonBody(String statusMessage, LocalDateTime timeStamp) {
        PactDslJsonBody pactDslJsonBody = new PactDslJsonBody();

        addStatusMessage(pactDslJsonBody, statusMessage);

        pactDslJsonBody
            .integerType("versionNumber", "022")
            .integerType("hearingRequestID", "^[a-zA-Z0-9]{1,30}$", "1880163574")
            .stringMatcher("status", STATUS_OPTIONS_STRING, "HEARING_REQUESTED")
            .stringMatcher("timeStamp", "2030-08-20T12:40:00")
            .asBody();

        return pactDslJsonBody;
    }

    private static void addStatusMessage(PactDslJsonBody pactDslJsonBody, String statusMessage) {
        pactDslJsonBody
            .stringType("status_message", statusMessage);
    }
}
