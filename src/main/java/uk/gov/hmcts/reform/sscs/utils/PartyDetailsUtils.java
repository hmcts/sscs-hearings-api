package uk.gov.hmcts.reform.sscs.utils;

import uk.gov.hmcts.reform.sscs.ccd.domain.OtherParty;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;
import uk.gov.hmcts.reform.sscs.model.single.hearing.IndividualDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.OrganisationDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PartyType;

import java.util.ArrayList;
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
                    .title(party.getName() == null ? null : party.getName().getTitle())
                    .firstName(party.getName() == null ? null : party.getName().getFirstName())
                    .lastName(party.getName() == null ? null :  party.getName().getLastName())
                    .preferredHearingChannel(HearingUtils.getPartyChannel(party.getHearingSubtype()))
                    .interpreterLanguage(party.getHearingOptions() == null ? null
                            : party.getHearingOptions().getLanguages())
                    .reasonableAdjustments(getReasonableAdjustments(party))
                    .vulnerableFlag(false)
                    .vulnerabilityDetails(null)
                    .hearingChannelEmail(party.getHearingSubtype() == null ? null
                            : party.getHearingSubtype().getHearingVideoEmail())
                    .hearingChannelPhone(party.getHearingSubtype() == null ? null
                            : party.getHearingSubtype().getHearingTelephoneNumber())

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
     * Returning the List of reasonable adjustments for an OtherParty.
     *
     * @param party an OtherParty
     * @return a List of Strings representing reasonable adjustments
     */
    public static List<String> getReasonableAdjustments(OtherParty party) {
        List<String> reasonableAdjustments = new ArrayList<>();
        if (Objects.nonNull(party.getReasonableAdjustment())
                && YesNo.isYes(party.getReasonableAdjustment().getWantsReasonableAdjustment())) {
            reasonableAdjustments.add(party.getReasonableAdjustment().getReasonableAdjustmentRequirements());
        }
        return reasonableAdjustments;
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
