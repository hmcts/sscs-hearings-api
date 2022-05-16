package uk.gov.hmcts.reform.sscs.utils;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.single.hearing.IndividualDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.OrganisationDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PartyType;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;



class PartyDetailsUtilsTest {

    @Test
    void shouldGetPartyType() {
        // given otherParty in setup()
        OtherParty otherParty = Mockito.mock(OtherParty.class);
        // when

        //then
        assertEquals(PartyType.IND, PartyDetailsUtils.getPartyType(otherParty));
    }

    @Test
    void shouldGetReasonableAdjustments() {
        // given otherParty
        OtherParty otherParty = Mockito.mock(OtherParty.class);
        ReasonableAdjustmentDetails reasonableAdjustmentDetails = Mockito.mock(ReasonableAdjustmentDetails.class);
        String adjustments = "Some adjustments...";
        // when
        Mockito.when(otherParty.getReasonableAdjustment()).thenReturn(reasonableAdjustmentDetails);
        Mockito.when(reasonableAdjustmentDetails.getWantsReasonableAdjustment()).thenReturn(YesNo.YES);
        Mockito.when(reasonableAdjustmentDetails.getReasonableAdjustmentRequirements()).thenReturn(adjustments);
        //then
        List<String> reasonableAdjustments = PartyDetailsUtils.getReasonableAdjustments(otherParty);
        assertFalse(reasonableAdjustments.isEmpty());
        assertEquals(1, reasonableAdjustments.size());
        assertEquals("Some adjustments...", reasonableAdjustments.stream().findFirst().orElseThrow());
    }

    @Test
    void shouldGetOrganisationDetails() {
        // given
        OtherParty otherParty = Mockito.mock(OtherParty.class);
        HearingOptions hearingOptions = Mockito.mock(HearingOptions.class);
        // when
        Mockito.when(otherParty.getHearingOptions()).thenReturn(hearingOptions);
        //then
        OrganisationDetails organisationDetails = PartyDetailsUtils.getOrganisationDetails(otherParty);
        assertNotNull(organisationDetails);
        assertNull(organisationDetails.getName());
        assertNull(organisationDetails.getOrganisationType());
        assertNull(organisationDetails.getCftOrganisationID());
    }

    @Test
    void shouldGetIndividualDetails() {
        // given
        Appeal appeal = Mockito.mock(Appeal.class);
        Name name = Mockito.mock(Name.class);
        HearingSubtype hearingSubtype = Mockito.mock(HearingSubtype.class);
        HearingOptions hearingOptions = Mockito.mock(HearingOptions.class);
        ReasonableAdjustmentDetails reasonableAdjustmentDetails = Mockito.mock(ReasonableAdjustmentDetails.class);
        OtherParty otherParty = Mockito.mock(OtherParty.class);
        SscsCaseData sscsCaseData = Mockito.mock(SscsCaseData.class);
        // when
        Mockito.when(sscsCaseData.getAppeal()).thenReturn(appeal);
        Mockito.when(name.getTitle()).thenReturn("Mr");
        Mockito.when(name.getFirstName()).thenReturn("Barny");
        Mockito.when(name.getLastName()).thenReturn("Boulderstone");
        Mockito.when(otherParty.getName()).thenReturn(name);
        Mockito.when(hearingSubtype.isWantsHearingTypeFaceToFace()).thenReturn(true);
        Mockito.when(hearingSubtype.getHearingVideoEmail()).thenReturn("test2@gmail.com");
        Mockito.when(hearingSubtype.getHearingTelephoneNumber()).thenReturn("0999733735");
        Mockito.when(otherParty.getHearingSubtype()).thenReturn(hearingSubtype);
        Mockito.when(hearingOptions.getLanguages()).thenReturn("Telugu");
        Mockito.when(otherParty.getHearingOptions()).thenReturn(hearingOptions);
        Mockito.when(otherParty.getReasonableAdjustment()).thenReturn(reasonableAdjustmentDetails);
        Mockito.when(reasonableAdjustmentDetails.getWantsReasonableAdjustment()).thenReturn(YesNo.YES);
        String adjustments = "Some adjustments...";
        Mockito.when(reasonableAdjustmentDetails.getReasonableAdjustmentRequirements()).thenReturn(adjustments);
        //then
        IndividualDetails individualDetails = PartyDetailsUtils.getIndividualDetails(otherParty, sscsCaseData);
        assertEquals("Mr", individualDetails.getTitle());
        assertEquals("Barny", individualDetails.getFirstName());
        assertEquals("Boulderstone", individualDetails.getLastName());
        assertEquals(HearingUtils.FACE_TO_FACE, individualDetails.getPreferredHearingChannel());
        assertEquals("Telugu", individualDetails.getInterpreterLanguage());
        assertEquals(1, individualDetails.getReasonableAdjustments().size());
        assertEquals("Some adjustments...",
                individualDetails.getReasonableAdjustments().stream().findFirst().orElseThrow());
        assertFalse(individualDetails.isVulnerableFlag());
        assertNull(individualDetails.getVulnerabilityDetails());
        assertEquals("test2@gmail.com", individualDetails.getHearingChannelEmail());
        assertEquals("0999733735", individualDetails.getHearingChannelPhone());
    }
}