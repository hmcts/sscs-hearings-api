package uk.gov.hmcts.reform.sscs.service;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.sscs.exception.AuthorisationException;
import uk.gov.hmcts.reform.sscs.exception.InvalidHeaderException;

@Slf4j
@Service
public class AuthorisationService {

    public static final String SERVICE_AUTHORISATION_HEADER = "ServiceAuthorization";

    private final ServiceAuthorisationApi serviceAuthorisationApi;

    public AuthorisationService(ServiceAuthorisationApi serviceAuthorisationApi) {
        this.serviceAuthorisationApi = serviceAuthorisationApi;
    }

    public void authorise(String serviceAuthHeader) throws AuthorisationException, InvalidHeaderException {
        try {
            log.debug("About to authorise request");
            serviceAuthorisationApi.getServiceName(serviceAuthHeader);
            log.debug("Request authorised");
        } catch (FeignException exc) {
            if ("Invalid authorization header".equalsIgnoreCase(exc.getMessage())) {
                InvalidHeaderException headExc = new InvalidHeaderException(exc);
                log.error("Authorisation failed with status {}", + exc.status(), headExc);
                throw headExc;
            } else {
                AuthorisationException authExc = new AuthorisationException(exc);
                log.error("Authorisation failed with status {}", + exc.status(), authExc);
                throw authExc;
            }
        }
    }
}
