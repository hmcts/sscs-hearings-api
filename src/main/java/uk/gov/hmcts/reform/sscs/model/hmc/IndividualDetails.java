package uk.gov.hmcts.reform.sscs.model.hmc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.exception.ValidationError;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.reform.sscs.exception.ValidationError.REASONABLE_ADJUSTMENTS_MAX_LENGTH_MSG;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IndividualDetails {

    @NotNull(message = ValidationError.TITLE_EMPTY)
    @Size(max = 40, message = ValidationError.TITLE_MAX_LENGTH)
    private String title;

    @NotNull(message = ValidationError.FIRST_NAME_EMPTY)
    @Size(max = 100, message = ValidationError.FIRST_NAME_MAX_LENGTH)
    private String firstName;

    @NotNull(message = ValidationError.LAST_NAME_EMPTY)
    @Size(max = 100, message = ValidationError.LAST_NAME_MAX_LENGTH)
    private String lastName;

    @Size(max = 70, message = ValidationError.PREFERRED_HEARING_CHANNEL_MAX_LENGTH)
    private String preferredHearingChannel;

    @Size(max = 10, message = ValidationError.INTERPRETER_LANGUAGE_MAX_LENGTH)
    private String interpreterLanguage;

    private List<@Size(max = 10, message = REASONABLE_ADJUSTMENTS_MAX_LENGTH_MSG)String> reasonableAdjustments;

    private Boolean vulnerableFlag;

    @Size(max = 256, message = ValidationError.VULNERABLE_DETAILS_MAX_LENGTH)
    private String vulnerabilityDetails;

    @Size(max = 120, message = ValidationError.HEARING_CHANNEL_EMAIL_MAX_LENGTH)
    @Email(message = ValidationError.HEARING_CHANNEL_EMAIL_INVALID)
    private String hearingChannelEmail;

    @Size(max = 30, message = ValidationError.HEARING_CHANNEL_PHONE_MAX_LENGTH)
    @Pattern(regexp = "^\\+?(?:[0-9] ?){6,14}[0-9]$",
        message = ValidationError.HEARING_CHANNEL_PHONE_INVALID)
    private String hearingChannelPhone;

    @Valid
    private List<RelatedParty> relatedParties;
}
