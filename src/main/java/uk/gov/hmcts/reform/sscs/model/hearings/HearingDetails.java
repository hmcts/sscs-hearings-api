package uk.gov.hmcts.reform.sscs.model.hearings;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("PMD.TooManyFields")
public class HearingDetails {

    private boolean autolistFlag;

    private String hearingType;

    private HearingWindow hearingWindow;

    private Integer duration;

    private List<String> nonStandardHearingDurationReasons;

    private String hearingPriorityType;

    private Integer numberOfPhysicalAttendees;

    private boolean hearingInWelshFlag;

    private List<HearingLocations> hearingLocations;

    private List<String> facilitiesRequired;

    private String listingComments;

    private String hearingRequester;

    private boolean privateHearingRequiredFlag;

    private String leadJudgeContractType;

    private PanelRequirements panelRequirements;

    private boolean hearingIsLinkedFlag;
}
