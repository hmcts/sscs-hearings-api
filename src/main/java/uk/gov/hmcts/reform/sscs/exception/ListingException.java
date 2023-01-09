package uk.gov.hmcts.reform.sscs.exception;

import lombok.Getter;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Getter
@ResponseStatus(BAD_REQUEST)
public class ListingException extends CaseException {
    private static final long serialVersionUID = -5687439455391806310L;
    protected String summary = "Missing Listing Requirement";
    protected String description;

    public ListingException(String message) {
        super(message);
        this.description = message;
    }
}
