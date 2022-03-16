package uk.gov.hmcts.reform.sscs.model.hearings;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IndividualDetails {

    private String title;

    private String firstName;

    private String lastName;

    private String preferredHearingChannel;

    private String interpreterLanguage;

    private List<String> reasonableAdjustments;

    private Boolean vulnerableFlag;


    private String vulnerabilityDetails;

    private String hearingChannelEmail;

    private String hearingChannelPhone;

    private List<RelatedParty> relatedParties;
}
