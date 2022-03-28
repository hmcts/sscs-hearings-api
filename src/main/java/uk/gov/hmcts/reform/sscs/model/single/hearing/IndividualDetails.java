package uk.gov.hmcts.reform.sscs.model.single.hearing;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
@Builder
public class IndividualDetails {

    private String title;

    private String firstName;

    private String lastName;

    private String preferredHearingChannel;

    private String interpreterLanguage;

    private List<String> reasonableAdjustments;

    private boolean vulnerableFlag;

    private String vulnerabilityDetails;

    private String hearingChannelEmail;

    private String hearingChannelPhone;

    private List<RelatedParty> relatedParties;
}
