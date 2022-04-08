package uk.gov.hmcts.reform.sscs.model.servicehearingvalues;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
    //@JsonProperty("hearingWindow")
    private ShvHearingWindow shvHearingWindow;
    private Integer duration;
    private String hearingPriorityType;
    private Integer numberOfPhysicalAttendees;
    private boolean hearingInWelshFlag;
    //@JsonProperty("hearingLocations")
    private List<ShvHearingLocation> shvHearingLocations;
    private Boolean caseAdditionalSecurityFlag;
    private List<String> facilitiesRequired;
    private String listingComments;
    private String hearingRequester;
    private boolean privateHearingRequiredFlag;
    //@JsonProperty("panelRequirements")
    private ShvPanelRequirements shvPanelRequirements;
    private String leadJudgeContractType;
    //@JsonProperty("judiciary")
    private ShvJudiciary shvJudiciary;
    private boolean hearingIsLinkedFlag;
    //@JsonProperty("parties")
    private List<ShvPartyDetails> shvParties;
    //@JsonProperty("caseFlags")
    private ShvCaseFlags shvCaseFlags;
    //@JsonProperty("screenFlow")
    private List<ShvScreenNavigation> shvScreenFlow;
    //@JsonProperty("vocabulary")
    private List<ShvVocabulary> shvVocabulary;
    private String hmctsServiceID;
}
