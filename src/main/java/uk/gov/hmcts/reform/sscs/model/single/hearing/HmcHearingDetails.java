package uk.gov.hmcts.reform.sscs.model.single.hearing;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
@Builder
@SuppressWarnings("PMD.TooManyFields")
public class HmcHearingDetails {

    private boolean autolistFlag;

    private String hearingType;

    private HearingWindow hearingWindow;

    private Number duration;

    private List<String> nonStandardHearingDurationReasons;

    private String hearingPriorityType;

    private Number numberOfPhysicalAttendees;

    private boolean hearingInWelshFlag;

    private List<HmcHearingLocation> hmcHearingLocations;

    private List<String> facilitiesRequired;

    private String listingComments;

    private String hearingRequester;

    private boolean privateHearingRequiredFlag;

    private String leadJudgeContractType;

    private PanelRequirements panelRequirements;

    private boolean hearingIsLinkedFlag;

    private String amendReasonCode;
}
