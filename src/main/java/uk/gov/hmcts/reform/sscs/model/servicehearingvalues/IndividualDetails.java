package uk.gov.hmcts.reform.sscs.model.servicehearingvalues;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
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
    private List<RelatedParties> relatedParties;
}
