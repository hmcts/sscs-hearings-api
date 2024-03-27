package uk.gov.hmcts.reform.sscs.exception;

import lombok.Getter;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Getter
@ResponseStatus(BAD_REQUEST)
public class ListingException extends CaseException {
    @Serial
    private static final long serialVersionUID = -5687439455391806310L;
    protected String summary = "Listing Exception";
    protected String description;

    public ListingException(String message) {
        super(message);
        this.description = message;
    }
}
