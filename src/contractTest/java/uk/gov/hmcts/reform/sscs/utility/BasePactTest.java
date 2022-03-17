package uk.gov.hmcts.reform.sscs.utility;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class BasePactTest {

    private static final String FORMATYYYYMMDDHHMMSSSSSSZ = "yyyy-MM-dd'T'HH:mm:SS";
    private static final String STATUS_OPTIONS_STRING = "HEARING_REQUESTED|UPDATE_REQUESTED|"
         + "UPDATE_SUBMITTED|AWAITING_LISTING|LISTED|CANCELLATION_REQUESTED|EXCEPTION";

    public static PactDslJsonBody generatePostHearingsJsonBody(String statusMessage) {

        PactDslJsonBody pactDslJsonBody = genericCreateHearingJsonBody(statusMessage, LocalDateTime.now());

        return pactDslJsonBody;
    }

    public static PactDslJsonBody genericCreateHearingJsonBody(String statusMessage, LocalDateTime timeStamp) {
        PactDslJsonBody pactDslJsonBody = new PactDslJsonBody();

        addStatusMessage(pactDslJsonBody, statusMessage);

        pactDslJsonBody
            .integerType("versionNumber", "022")
            .integerType("hearingRequestID", "^[a-zA-Z0-9]{1,30}$", "1880163574")
            .stringMatcher("status", STATUS_OPTIONS_STRING, "HEARING_REQUESTED")
            .datetime("timeStamp", FORMATYYYYMMDDHHMMSSSSSSZ, timeStamp.atZone(ZoneId.systemDefault()).toInstant())
            .asBody();

        return pactDslJsonBody;
    }

    private static void addStatusMessage(PactDslJsonBody pactDslJsonBody, String statusMessage) {
        pactDslJsonBody
            .stringType("status_message", statusMessage);
    }
}
