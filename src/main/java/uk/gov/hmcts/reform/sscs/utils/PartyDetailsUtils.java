package uk.gov.hmcts.reform.sscs.utils;

import uk.gov.hmcts.reform.sscs.ccd.domain.OtherParty;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingsPartiesMapping;
import uk.gov.hmcts.reform.sscs.model.single.hearing.IndividualDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.OrganisationDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PartyType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class PartyDetailsUtils {

    private PartyDetailsUtils() {
        throw new IllegalStateException("Utility class");
    }

    // TODO check with what is done in SSCS-10321-Create-Hearing-POST-Mapping when it is merged
    public static IndividualDetails getIndividualDetails(OtherParty party, SscsCaseData sscsCaseData) {
        // Line added to suppress PMD error, sscsCaseData would be needed to get relatedParties
        sscsCaseData.getAppeal();
        if (PartyDetailsUtils.getPartyType(party).equals(PartyType.IND)) {
            return IndividualDetails.builder()
                    .firstName(party.getName() == null ? null : party.getName().getFirstName())
                    .lastName(party.getName() == null ? null :  party.getName().getLastName())
                    .preferredHearingChannel(HearingUtils.getPartyChannel(party.getHearingSubtype()))
                    .interpreterLanguage(party.getHearingOptions() == null ? null
                            : party.getHearingOptions().getLanguages())
                    .reasonableAdjustments(HearingsPartiesMapping.getIndividualReasonableAdjustments(party.getHearingOptions()))
                    .vulnerableFlag(HearingsPartiesMapping.isIndividualVulnerableFlag())
                    .vulnerabilityDetails(HearingsPartiesMapping.getIndividualVulnerabilityDetails())
                    .hearingChannelEmail(party.getHearingSubtype() == null ? null
                            : Collections.singletonList(party.getHearingSubtype().getHearingVideoEmail()))
                    .hearingChannelPhone(party.getHearingSubtype() == null ? null
                            : Collections.singletonList(party.getHearingSubtype().getHearingTelephoneNumber()))

                    // TODO missing mapping et them from the method  in SSCS-10245-send-epimsID-to-HMC,
                    // call with the order HearingsMapping ->  updateIds(wrapper), buildRelatedParties(wrapper)
                    .relatedParties(new ArrayList<>())
                    .build();
        }
        return IndividualDetails.builder()
                .build();
    }




    /**
     * Skeleton method to get OrganisationDetails.
     * Using Individuals for now.
     *
     * @param party an OtherParty object
     * @return OrganisationDetails object
     */
    public static OrganisationDetails getOrganisationDetails(OtherParty party) {
        // Line added to suppress PMD error
        party.getHearingOptions();
        return OrganisationDetails.builder()
                .build();
    }


    /**
     * Presuming Individual for now.
     *
     * @param party an OtherParty
     * @return return PartyType
     */
    public static PartyType getPartyType(OtherParty party) {
        // Line added to suppress PMD error
        party.getHearingOptions();
        return PartyType.IND;
    }
}
