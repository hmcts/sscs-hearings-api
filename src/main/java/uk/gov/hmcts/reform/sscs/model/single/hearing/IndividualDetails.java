package uk.gov.hmcts.reform.sscs.model.single.hearing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndividualDetails {

    private String title;

    private String firstName;

    private String title;

    private String lastName;

    private String preferredHearingChannel;

    private String interpreterLanguage;

    private List<String> reasonableAdjustments;

    private boolean vulnerableFlag;

    private String vulnerabilityDetails;

    private List<String> hearingChannelEmail;

    private List<String> hearingChannelPhone;

    private List<RelatedParty> relatedParties;

    private String custodyStatus;

    private String otherReasonableAdjustmentDetails;

}
