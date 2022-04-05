package uk.gov.hmcts.reform.sscs.model.servicehearingvalues;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@SuppressWarnings("PMD.TooManyFields")
@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class ServiceHearingValues {

    private String caseName;
    private boolean autoListFlag;
    private String hearingType;
    private String caseType;
    private List<String> caseSubTypes;
    @JsonProperty("SvhHearingWindow")
    private HearingWindow hearingWindow;
    private Integer duration;
    private String hearingPriorityType;
    private Integer numberOfPhysicalAttendees;
    private boolean hearingInWelshFlag;
    @JsonProperty("SvhHearingLocations")
    private List<HearingLocation> hearingLocations;
    private Boolean caseAdditionalSecurityFlag;
    private List<String> facilitiesRequired;
    private String listingComments;
    private String hearingRequester;
    private boolean privateHearingRequiredFlag;
    @JsonProperty("SvhPanelRequirements")
    private PanelRequirements panelRequirements;
    private String leadJudgeContractType;
    @JsonProperty("SvhJudiciary")
    private Judiciary judiciary;
    private boolean hearingIsLinkedFlag;
    @JsonProperty("SvhParties")
    private List<PartyDetails> parties;
    @JsonProperty("SvhCaseFlags")
    private CaseFlags caseFlags;
    @JsonProperty("SvhScreenFlow")
    private List<ScreenNavigation> screenFlow;
    @JsonProperty("SvhVocabulary")
    private List<Vocabulary> vocabulary;
    private String hmctsServiceID;
}
