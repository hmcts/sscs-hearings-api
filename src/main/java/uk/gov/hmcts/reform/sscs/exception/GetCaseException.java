package uk.gov.hmcts.reform.sscs.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ResponseStatus(NOT_FOUND)
public class GetCaseException extends CaseException  {
    private static final long serialVersionUID = -7206725950985350023L;

    public GetCaseException(String message) {
        super(message);
    }
}
