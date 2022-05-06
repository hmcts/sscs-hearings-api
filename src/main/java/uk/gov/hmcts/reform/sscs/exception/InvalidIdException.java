package uk.gov.hmcts.reform.sscs.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ResponseStatus(BAD_REQUEST)
public class InvalidIdException extends CaseException  {
    private static final long serialVersionUID = 3697481406610087224L;

    public InvalidIdException(String message, NumberFormatException err) {
        super(String.format("%s: %s",message,err.getMessage()));
    }
}
