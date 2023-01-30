package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.sscs.ccd.domain.AdjournCasePanelMembersExcluded;
import uk.gov.hmcts.reform.sscs.ccd.domain.Adjournment;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;
import uk.gov.hmcts.reform.sscs.model.client.JudicialUser;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.RequirementType;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.MemberType;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PanelPreference;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class HearingsPanelMappingAdjournmentTest extends HearingsMappingBase {

    public static final String JUDGE_ID = "2000";
    public static final String JUDGE_ROLE_TYPE = "64";
    public static final String JUDGE_ID_JUDGE_ROLE_TYPE = JUDGE_ID + "|" + JUDGE_ROLE_TYPE;
    public static final String EMAIL_ID_JUDGE = "J1";
    public static final String EMAIL_ID_DISABILITY = "D2";
    public static final String EMAIL_ID_MEDICAL = "M3";

    @Mock
    private ReferenceDataServiceHolder referenceDataServiceHolder;

    JudicialUser judicialUserJudge;
    JudicialUser judicialUserDisability;
    JudicialUser judicialUserMedical;

    PanelPreference expectedPanelPrefJudge;
    PanelPreference expectedPanelPrefDisability;
    PanelPreference expectedPanelPrefMedical;

    @BeforeEach
    void setUp() {
        when(referenceDataServiceHolder.isAdjournmentFlagEnabled()).thenReturn(true);
        judicialUserJudge = JudicialUser.builder()
            .fullName("Judge Beech")
            .emailId(EMAIL_ID_JUDGE)
            .build();

        judicialUserDisability = JudicialUser.builder()
            .fullName("Lucy Stick")
            .emailId(EMAIL_ID_DISABILITY)
            .build();

        judicialUserMedical = JudicialUser.builder()
            .fullName("Dr Roberts")
            .emailId(EMAIL_ID_MEDICAL)
            .build();

        expectedPanelPrefJudge = PanelPreference.builder()
            .memberID(EMAIL_ID_JUDGE)
            .memberType(MemberType.JUDGE.getValue())
            .build();

        expectedPanelPrefDisability = PanelPreference.builder()
            .memberID(EMAIL_ID_DISABILITY)
            .memberType(MemberType.PANEL_MEMBER.getValue())
            .build();

        expectedPanelPrefMedical = PanelPreference.builder()
            .memberID(EMAIL_ID_MEDICAL)
            .memberType(MemberType.PANEL_MEMBER.getValue())
            .build();

        Adjournment adjournment = Adjournment.builder()
            .adjournmentInProgress(YesNo.YES)
            .otherPanelMemberName(judicialUserJudge)
            .disabilityQualifiedPanelMemberName(judicialUserDisability)
            .medicallyQualifiedPanelMemberName(judicialUserMedical)
            .build();
        caseData.setAdjournment(adjournment);

    }

    @DisplayName("When adjournment feature flag is disabled, returns an empty list")
    @Test
    void testGetPanelPreferences_adjournmentFlagDisabled() {
        when(referenceDataServiceHolder.isAdjournmentFlagEnabled()).thenReturn(false);
        List<PanelPreference> result = HearingsPanelMapping.getPanelPreferences(caseData, referenceDataServiceHolder);

        assertThat(result).isEmpty();
    }

    @DisplayName("When adjournmentInProgress is NO, returns an empty list")
    @Test
    void testGetPanelPreferences_adjournmentNotInProgress() {
        caseData.getAdjournment().setAdjournmentInProgress(YesNo.NO);
        List<PanelPreference> result = HearingsPanelMapping.getPanelPreferences(caseData, referenceDataServiceHolder);

        assertThat(result).isEmpty();
    }

    @DisplayName("When panel members are excluded, returns list of three PanelPreferences "
        + "marked with RequirementType exclude")
    @Test
    void testGetPanelPreferences_exclude() {
        Adjournment adjournment = caseData.getAdjournment();
        adjournment.setPanelMembersExcluded(AdjournCasePanelMembersExcluded.YES);
        expectedPanelPrefDisability.setRequirementType(RequirementType.EXCLUDE);
        expectedPanelPrefMedical.setRequirementType(RequirementType.EXCLUDE);
        expectedPanelPrefJudge.setRequirementType(RequirementType.EXCLUDE);

        List<PanelPreference> result = HearingsPanelMapping.getPanelPreferences(caseData, referenceDataServiceHolder);

        assertThat(result)
            .hasSize(3)
            .containsOnly(expectedPanelPrefJudge, expectedPanelPrefDisability, expectedPanelPrefMedical);
    }

    @DisplayName("When panel members are reserved, returns list of three PanelPreferences "
        + "marked with RequirementType reserved")
    @Test
    void testGetPanelPreferences_reserved() {
        Adjournment adjournment = caseData.getAdjournment();
        adjournment.setPanelMembersExcluded(AdjournCasePanelMembersExcluded.RESERVED);
        expectedPanelPrefDisability.setRequirementType(RequirementType.MUST_INCLUDE);
        expectedPanelPrefMedical.setRequirementType(RequirementType.MUST_INCLUDE);
        expectedPanelPrefJudge.setRequirementType(RequirementType.MUST_INCLUDE);

        List<PanelPreference> result = HearingsPanelMapping.getPanelPreferences(caseData, referenceDataServiceHolder);

        assertThat(result)
            .hasSize(3)
            .containsOnly(expectedPanelPrefJudge, expectedPanelPrefDisability, expectedPanelPrefMedical);
    }


}
