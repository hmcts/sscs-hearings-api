package uk.gov.hmcts.reform.sscs.exception;

public class GetCaseException extends Exception  {
    private static final long serialVersionUID = -7206725950985350023L;

    public GetCaseException(Throwable cause) {
        super(cause);
    }

    public GetCaseException(String message) {
        super(message);
    }
}
