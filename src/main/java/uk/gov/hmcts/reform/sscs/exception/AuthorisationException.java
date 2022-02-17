package uk.gov.hmcts.reform.sscs.exception;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class AuthorisationException extends RuntimeException {

    private static final long serialVersionUID = 3004290031592292746L;

    public AuthorisationException(Exception ex) {
        super(ex);
    }
}
