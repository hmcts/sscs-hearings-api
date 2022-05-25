package uk.gov.hmcts.reform.sscs.model.service.hearingvalues;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.model.single.hearing.CaseCategory;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingLocations;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingWindow;

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
    private String caseNamePublic;
    private boolean autoListFlag;
    private String hearingType;
    private String caseType;
    private List<CaseCategory> caseCategories;
    @JsonProperty("hearingWindow")
    private HearingWindow hearingWindow;
    private Integer duration;
    private String hearingPriorityType;
    private Integer numberOfPhysicalAttendees;
    private boolean hearingInWelshFlag;
    @JsonProperty("hearingLocations")
    private List<HearingLocations> hearingLocations;
    private Boolean caseAdditionalSecurityFlag;
    private List<String> facilitiesRequired;
    private String listingComments;
    private String hearingRequester;
    private boolean privateHearingRequiredFlag;
    @JsonProperty("panelRequirements")
    private PanelRequirements panelRequirements;
    private String leadJudgeContractType;
    @JsonProperty("judiciary")
    private Judiciary judiciary;
    private boolean hearingIsLinkedFlag;
    @JsonProperty("parties")
    private List<PartyDetails> parties;
    @JsonProperty("caseFlags")
    private CaseFlags caseFlags;
    @JsonProperty("screenFlow")
    private List<ScreenNavigation> screenFlow;
    @JsonProperty("vocabulary")
    private List<Vocabulary> vocabulary;
    private String hmctsServiceID;
}
