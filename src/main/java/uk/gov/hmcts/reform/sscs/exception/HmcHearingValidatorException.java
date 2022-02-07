package uk.gov.hmcts.reform.sscs.exception;

import com.networknt.schema.ValidationMessage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Set;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY,
        reason = "Invalid hmc validation")
public class HmcHearingValidatorException extends RuntimeException {

    private Set<ValidationMessage> validationErrors;

    public HmcHearingValidatorException(String message, Set<ValidationMessage> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }

    public Set<ValidationMessage> getValidationErrors() {
        return this.validationErrors;
    }
}
