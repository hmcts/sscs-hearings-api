package uk.gov.hmcts.reform.sscs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

import java.util.Map;

public class BasePactTesting {

    public static final String PROVIDER_NAME = "sscsHearingApiProvider";
    public static final String CONSUMER_NAME = "sscsHearingApiConsumer";
    protected static final Logger logger = LoggerFactory.getLogger(BasePactTesting.class);
    protected static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    protected static final String IDAM_OAUTH2_TOKEN = "pact-test-idam-token";
    protected static final String SERVICE_AUTHORIZATION_TOKEN = "pact-test-s2s-token";
    protected static final String CONTENT_TYPE = "Content-Type";
    protected static final String APPLICATION_JSON = "application/json";

    protected static final Map<String, String> headers = Map.of(
        HttpHeaders.AUTHORIZATION, IDAM_OAUTH2_TOKEN,
        SERVICE_AUTHORIZATION, SERVICE_AUTHORIZATION_TOKEN,
        CONTENT_TYPE, APPLICATION_JSON
    );


}
