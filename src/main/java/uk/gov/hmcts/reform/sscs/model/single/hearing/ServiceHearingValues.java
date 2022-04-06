<<<<<<<< HEAD:src/main/java/uk/gov/hmcts/reform/sscs/model/service/hearingvalues/ServiceHearingValues.java
package uk.gov.hmcts.reform.sscs.model.service.hearingvalues;
========
package uk.gov.hmcts.reform.sscs.model.single.hearing;
>>>>>>>> 01e19bc (SSCS-10245 Delete similar object HearingLocation and refactor the code):src/main/java/uk/gov/hmcts/reform/sscs/model/single/hearing/ServiceHearingValues.java

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sscs.model.servicehearingvalues.*;
import uk.gov.hmcts.reform.sscs.model.servicehearingvalues.HearingWindow;
import uk.gov.hmcts.reform.sscs.model.servicehearingvalues.PanelRequirements;
import uk.gov.hmcts.reform.sscs.model.servicehearingvalues.PartyDetails;

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
    private HearingWindow hearingWindow;
    private Integer duration;
    private String hearingPriorityType;
    private Integer numberOfPhysicalAttendees;
    private boolean hearingInWelshFlag;
    private List<HearingLocations> hearingLocations;
    private Boolean caseAdditionalSecurityFlag;
    private List<String> facilitiesRequired;
    private String listingComments;
    private String hearingRequester;
    private boolean privateHearingRequiredFlag;
    private PanelRequirements panelRequirements;
    private String leadJudgeContractType;
    private Judiciary judiciary;
    private boolean hearingIsLinkedFlag;
    private List<PartyDetails> parties;
    private CaseFlags caseFlags;
    private List<ScreenNavigation> screenFlow;
    private List<Vocabulary> vocabulary;
}
