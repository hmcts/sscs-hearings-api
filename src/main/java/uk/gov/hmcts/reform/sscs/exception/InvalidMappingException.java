package uk.gov.hmcts.reform.sscs.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ResponseStatus(BAD_REQUEST)
public class InvalidMappingException extends CaseException {
    private static final long serialVersionUID = -5687439455391806310L;

    public InvalidMappingException(String message) {
        super(message);
    }
}
