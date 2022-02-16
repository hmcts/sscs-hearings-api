package uk.gov.hmcts.reform.sscs.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

/**
 * Default endpoints per application.
 */
@RestController
public class RootController {

    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * Root GET endpoint.
     *
     * <p>Azure application service has a hidden feature of making requests to root endpoint when
     * "Always On" is turned on.
     * This is the endpoint to deal with that and therefore silence the unnecessary 404s as a response code.
     *
     * @return Welcome message from the service.
     */
    @GetMapping("/")
    public ResponseEntity<String> welcome() {
        String out = String.format("<div><h1>%1$s</h1><p>Welcome to %1$s</p><p>Health: <a "
            + "href='/health'>/health</a></p><p>Swagger API Docs: <a href='/swagger-ui/index.html'>/swagger-ui/index.html</a></p></div>",applicationName);
        return ok(out);
    }
}
