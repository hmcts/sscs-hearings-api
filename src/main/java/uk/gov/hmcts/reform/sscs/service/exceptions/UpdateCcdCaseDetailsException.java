package uk.gov.hmcts.reform.sscs.service.exceptions;

public class UpdateCcdCaseDetailsException extends Exception {
    private static final long serialVersionUID = -315707861582772008L;
    private final Exception ex;

    public UpdateCcdCaseDetailsException(String message, Exception ex) {
        super(message);
        this.ex = ex;
    }


    public Exception getException() {
        return ex;
    }
}
