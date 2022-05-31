package uk.gov.hmcts.reform.sscs.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ResponseStatus(BAD_REQUEST)
public class InvalidHearingDataException extends Exception {
    private static final long serialVersionUID = -4089879478303228007L;

    public InvalidHearingDataException(String message) {
        super(message);
    }
}
