package uk.gov.hmcts.reform.sscs.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@ResponseStatus(FORBIDDEN)
public class AuthorisationException extends Exception  {

    private static final long serialVersionUID = 3004290031592292746L;

    public AuthorisationException(Exception ex) {
        super(ex);
    }
}
