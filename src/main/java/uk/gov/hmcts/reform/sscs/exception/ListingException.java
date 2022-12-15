package uk.gov.hmcts.reform.sscs.exception;

import lombok.Getter;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Getter
@ResponseStatus(BAD_REQUEST)
public class ListingException extends InvalidMappingException {
    private static final long serialVersionUID = -5687439455391806310L;

    public static final String SUMMARY = "Missing Listing Requirement";
    private final String description;

    public ListingException(String message) {
        super(message);
        this.description = message;
    }
}
