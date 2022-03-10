package uk.gov.hmcts.reform.sscs.exception;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class InvalidHeaderException extends Exception  {
    private static final long serialVersionUID = 7849412332464241373L;

    public InvalidHeaderException(Exception ex) {
        super(ex);
    }
}
