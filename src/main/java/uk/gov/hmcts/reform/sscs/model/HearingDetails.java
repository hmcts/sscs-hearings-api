package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.exceptions.ValidationError;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.reform.sscs.exceptions.ValidationError.FACILITIES_REQUIRED_MAX_LENGTH_MSG;
import static uk.gov.hmcts.reform.sscs.exceptions.ValidationError.NON_STANDARD_HEARING_DURATION_REASONS_MAX_LENGTH_MSG;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("PMD.TooManyFields")
public class HearingDetails {

    @NotNull(message = ValidationError.AUTO_LIST_FLAG_NULL_EMPTY)
    private boolean autolistFlag;

    @NotEmpty(message = ValidationError.HEARING_TYPE_NULL_EMPTY)
    @Size(max = 40, message = ValidationError.HEARING_TYPE_MAX_LENGTH)
    private String hearingType;

    @NotNull(message = ValidationError.HEARING_WINDOW_NULL)
    private HearingWindow hearingWindow;

    @NotNull(message = ValidationError.DURATION_EMPTY)
    @Min(value = 0, message = ValidationError.DURATION_MIN_VALUE)
    private Integer duration;

    private List<@Size(max = 70, message = NON_STANDARD_HEARING_DURATION_REASONS_MAX_LENGTH_MSG) String>
        nonStandardHearingDurationReasons;

    @NotEmpty(message = ValidationError.HEARING_PRIORITY_TYPE)
    @Size(max = 60, message = ValidationError.HEARING_PRIORITY_TYPE_MAX_LENGTH)
    private String hearingPriorityType;

    @Min(value = 0, message = ValidationError.NUMBER_OF_PHYSICAL_ATTENDEES_MIN_VALUE)
    private Integer numberOfPhysicalAttendees;

    private boolean hearingInWelshFlag;

    @Valid
    @NotNull(message = ValidationError.HEARING_LOCATION_EMPTY)
    @NotEmpty(message = ValidationError.INVALID_HEARING_LOCATION)
    private List<HearingLocations> hearingLocations;

    private List<@Size(max = 70, message = FACILITIES_REQUIRED_MAX_LENGTH_MSG) String> facilitiesRequired;

    @Size(max = 2000, message = ValidationError.LISTING_COMMENTS_MAX_LENGTH)
    private String listingComments;

    @Size(max = 60, message = ValidationError.HEARING_REQUESTER_MAX_LENGTH)
    private String hearingRequester;

    private boolean privateHearingRequiredFlag;

    @Size(max = 70, message = ValidationError.LEAD_JUDGE_CONTRACT_TYPE_MAX_LENGTH)
    private String leadJudgeContractType;

    @Valid
    @NotNull(message = ValidationError.INVALID_PANEL_REQUIREMENTS)
    private PanelRequirements panelRequirements;

    private boolean hearingIsLinkedFlag;
}
