package uk.gov.hmcts.reform.sscs.utility;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class HearingResponsePactUtil {

    private static final Logger logger = LoggerFactory.getLogger(HearingResponsePactUtil.class);

    private static final String FORMATYYYYMMDDHHMMSSSSSSZ = "yyyy-MM-dd'T'HH:mm:SSSSSS";
    private static final String STATUS_OPTIONS_STRING = "HEARING_REQUESTED|UPDATE_REQUESTED|"
        + "UPDATE_SUBMITTED|AWAITING_LISTING|LISTED|CANCELLATION_REQUESTED|"
        + "EXCEPTION";

    private HearingResponsePactUtil() {
    }

    /**
     * generate Pact JSON body.
     * @return PactDslJsonBody Pact Dsl JSON body
     */
    public static PactDslJsonBody generatePostHearingsJsonBody(String statusMessage) {

        PactDslJsonBody pactDslJsonBody = genericCreateHearingJsonBody(statusMessage, LocalDateTime.now());

        logger.debug("pactDslJsonBody: {}", pactDslJsonBody);
        return pactDslJsonBody;
    }

    public static PactDslJsonBody genericCreateHearingJsonBody(String statusMessage, LocalDateTime timeStamp) {
        PactDslJsonBody pactDslJsonBody = new PactDslJsonBody();

        addStatusMessage(pactDslJsonBody, statusMessage);

        pactDslJsonBody
            .integerType("versionNumber", "022")
            .integerType("hearingRequestID", "^[a-zA-Z0-9]{1,30}$","1880163574")
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
