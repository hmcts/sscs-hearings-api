package uk.gov.hmcts.reform.sscs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IndividualDetails {

    private String title;
    private String firstName;
    private String lastName;
    private String preferredHearingChannel;
    private String interpreterLanguage;
    private String reasonableAdjustments;
    private  boolean vulnerableFlag;
    private String vulnerabilityDetails;
    private  String hearingChannelEmail;
    private String hearingChannelPhone;
    private RelatedParty relatedParties;
}
