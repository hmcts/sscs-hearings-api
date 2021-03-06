package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.Mock;
import uk.gov.hmcts.reform.sscs.ccd.domain.Benefit;
import uk.gov.hmcts.reform.sscs.ccd.domain.BenefitCode;
import uk.gov.hmcts.reform.sscs.ccd.domain.DynamicList;
import uk.gov.hmcts.reform.sscs.ccd.domain.DynamicListItem;
import uk.gov.hmcts.reform.sscs.ccd.domain.Issue;
import uk.gov.hmcts.reform.sscs.ccd.domain.OverrideFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.PanelMember;
import uk.gov.hmcts.reform.sscs.ccd.domain.PanelMemberType;
import uk.gov.hmcts.reform.sscs.ccd.domain.ReservedToMember;
import uk.gov.hmcts.reform.sscs.ccd.domain.SchedulingAndListingFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.SessionCategory;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsIndustrialInjuriesData;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PanelPreference;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PanelRequirements;
import uk.gov.hmcts.reform.sscs.reference.data.model.SessionCategoryMap;
import uk.gov.hmcts.reform.sscs.reference.data.service.SessionCategoryMapService;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.params.provider.EnumSource.Mode.INCLUDE;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.YES;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.RequirementType.MUST_INCLUDE;

class HearingsPanelMappingTest extends HearingsMappingBase {

    public static final String JUDGE_ID = "2000";
    public static final String JUDGE_ROLE_TYPE = "64";
    public static final String JUDGE_ID_JUDGE_ROLE_TYPE = JUDGE_ID + "|" + JUDGE_ROLE_TYPE;
    @Mock
    private SessionCategoryMapService sessionCategoryMaps;

    @Mock
    private ReferenceDataServiceHolder referenceDataServiceHolder;

    @DisplayName("When no data is given getPanelRequirements returns the valid but empty PanelRequirements")
    @Test
    void testGetPanelRequirements() {
        given(referenceDataServiceHolder.getSessionCategoryMaps()).willReturn(sessionCategoryMaps);

        SscsCaseData caseData = SscsCaseData.builder().build();

        PanelRequirements result = HearingsPanelMapping.getPanelRequirements(caseData, referenceDataServiceHolder);

        assertThat(result).isNotNull();
        assertThat(result.getRoleTypes()).isEmpty();
        assertThat(result.getAuthorisationTypes()).isEmpty();
        assertThat(result.getAuthorisationSubTypes()).isEmpty();
        assertThat(result.getPanelPreferences()).isEmpty();
        assertThat(result.getPanelSpecialisms()).isEmpty();
    }

    @DisplayName("getRoleTypes returns an empty list when benefit is not Industrial Injuries Disablement Benefit or CHILD_SUPPORT ")
    @Test
    void shouldReturn_EmptyRoleTypeList_When_Benefit_Not_industrialInjuriesDisablementBenefit_or_ChildSupport() {
        List<String> result = HearingsPanelMapping.getRoleTypes(Benefit.ATTENDANCE_ALLOWANCE.getBenefitCode());
        assertThat(result).isEmpty();
    }

    @DisplayName("getRoleTypes returns PanelMemberType.TRIBUNALS_MEMBER_MEDICAL reference when benefit is Industrial Injuries Disablement Benefit ")
    @Test
    void shouldReturn_TribunalsMember_MedicalReference_When_Benefit_is_IndustrialInjuriesDisablementBenefit() {
        List<String> result = HearingsPanelMapping.getRoleTypes(Benefit.IIDB.getBenefitCode());
        assertThat(result).contains(PanelMemberType.TRIBUNALS_MEMBER_MEDICAL.getReference());
    }

    @DisplayName("getRoleTypes returns PanelMemberType.TRIBUNALS_MEMBER_FINANCIALLY_QUALIFIED reference when benefit is CHILD_SUPPORT ")
    @Test
    void shouldReturn_TribunalsMember_Financially_Qualified_When_Benefit_is_ChildSupport() {
        List<String> result = HearingsPanelMapping.getRoleTypes(Benefit.CHILD_SUPPORT.getBenefitCode());
        assertThat(result).contains(PanelMemberType.TRIBUNALS_MEMBER_FINANCIALLY_QUALIFIED.getReference());
    }

    @DisplayName("getAuthorisationTypes returns an empty list")
    @Test
    void testGetAuthorisationTypes() {
        List<String> result = HearingsPanelMapping.getAuthorisationTypes();

        assertThat(result).isEmpty();
    }

    @DisplayName("getAuthorisationSubTypes returns an empty list")
    @Test
    void testGetAuthorisationSubTypes() {
        List<String> result = HearingsPanelMapping.getAuthorisationSubTypes();

        assertThat(result).isEmpty();
    }

    @DisplayName("When isReservedToMember override is Yes and a reserved member is given, getPanelPreferences returns a List with the requested Judges details")
    @Test
    void testGetPanelPreferences() {

        DynamicList reservedMember = new DynamicList(new DynamicListItem(JUDGE_ID_JUDGE_ROLE_TYPE, "Judge Dredd"), List.of());

        SscsCaseData caseData = SscsCaseData.builder()
            .schedulingAndListingFields(SchedulingAndListingFields.builder()
                .overrideFields(OverrideFields.builder()
                    .reservedToJudge(ReservedToMember.builder()
                        .isReservedToMember(YES)
                        .reservedMember(reservedMember)
                        .build())
                    .build())
                .build())
            .build();
        List<PanelPreference> result = HearingsPanelMapping.getPanelPreferences(caseData);

        assertThat(result)
            .hasSize(1)
            .extracting("memberID", "memberType", "requirementType")
            .containsExactlyInAnyOrder(tuple(JUDGE_ID, JUDGE_ROLE_TYPE, MUST_INCLUDE));
    }

    @DisplayName("When isReservedToMember override is not Yes getPanelPreferences returns an empty list")
    @ParameterizedTest
    @EnumSource(
        value = YesNo.class,
        names = {"NO"},
        mode = INCLUDE)
    @NullSource
    void testGetPanelPreferences(YesNo value) {

        DynamicList reservedMember = new DynamicList(new DynamicListItem(JUDGE_ID, "Judge Dredd"), List.of());

        SscsCaseData caseData = SscsCaseData.builder()
            .schedulingAndListingFields(SchedulingAndListingFields.builder()
                .overrideFields(OverrideFields.builder()
                    .reservedToJudge(ReservedToMember.builder()
                        .isReservedToMember(value)
                        .reservedMember(reservedMember)
                        .build())
                    .build())
                .build())
            .build();

        List<PanelPreference> result = HearingsPanelMapping.getPanelPreferences(caseData);

        assertThat(result).isEmpty();
    }

    @DisplayName("When when the reserved member item has the wrong formatted code, getPanelPreferences returns an empty list")
    @Test
    void testGetPanelPreferencesWrongCodeFormat() {

        DynamicList reservedMember = new DynamicList(new DynamicListItem("wrong format", "Judge Dredd"), List.of());

        SscsCaseData caseData = SscsCaseData.builder()
            .schedulingAndListingFields(SchedulingAndListingFields.builder()
                .overrideFields(OverrideFields.builder()
                    .reservedToJudge(ReservedToMember.builder()
                        .isReservedToMember(YES)
                        .reservedMember(reservedMember)
                        .build())
                    .build())
                .build())
            .build();

        List<PanelPreference> result = HearingsPanelMapping.getPanelPreferences(caseData);

        assertThat(result).isEmpty();
    }

    @DisplayName("When isReservedToMember override is Yes and but no reserved member is given, getPanelPreferences returns an empty list")
    @Test
    void testGetPanelPreferencesJudgeNull() {

        DynamicList reservedMember = new DynamicList(null, List.of());

        SscsCaseData caseData = SscsCaseData.builder()
            .schedulingAndListingFields(SchedulingAndListingFields.builder()
                .overrideFields(OverrideFields.builder()
                    .reservedToJudge(ReservedToMember.builder()
                        .isReservedToMember(YES)
                        .reservedMember(reservedMember)
                        .build())
                    .build())
                .build())
            .build();

        List<PanelPreference> result = HearingsPanelMapping.getPanelPreferences(caseData);

        assertThat(result).isEmpty();
    }

    @DisplayName("When overrideFields are null getPanelPreferences returns an empty list")
    @Test
    void testGetPanelPreferencesOverrideFieldsNull() {
        SscsCaseData caseData = SscsCaseData.builder().build();

        List<PanelPreference> result = HearingsPanelMapping.getPanelPreferences(caseData);

        assertThat(result).isEmpty();
    }

    @DisplayName("When a case is given with a second doctor getPanelRequirements returns the valid PanelRequirements")
    @ParameterizedTest
    @CsvSource(value = {
        "cardiologist,eyeSurgeon,1|3",
        "null,carer,2",
    }, nullValues = {"null"})
    void testGetPanelSpecialisms(String doctorSpecialism, String doctorSpecialismSecond, String expected) {

        SessionCategoryMap sessionCategoryMap = new SessionCategoryMap(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
            true, false, SessionCategory.CATEGORY_06, null
        );

        SscsCaseData caseData = SscsCaseData.builder()
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .sscsIndustrialInjuriesData(SscsIndustrialInjuriesData.builder()
                .panelDoctorSpecialism(doctorSpecialism)
                .secondPanelDoctorSpecialism(doctorSpecialismSecond)
                .build())
            .build();

        List<String> result = HearingsPanelMapping.getPanelSpecialisms(caseData, sessionCategoryMap);

        List<String> expectedList = splitCsvParamArray(expected);
        assertThat(result)
            .containsExactlyInAnyOrderElementsOf(expectedList);

    }

    @DisplayName("When a case is given with no second doctor getPanelRequirements returns the valid PanelRequirements")
    @ParameterizedTest
    @CsvSource(value = {
        "generalPractitioner,4",
    }, nullValues = {"null"})
    void testGetPanelSpecialisms(String doctorSpecialism, String expected) {

        SessionCategoryMap sessionCategoryMap = new SessionCategoryMap(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
            false, false, SessionCategory.CATEGORY_05, null
        );

        SscsCaseData caseData = SscsCaseData.builder()
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .sscsIndustrialInjuriesData(SscsIndustrialInjuriesData.builder()
                .panelDoctorSpecialism(doctorSpecialism)
                .build())
            .build();

        List<String> result = HearingsPanelMapping.getPanelSpecialisms(caseData, sessionCategoryMap);

        List<String> expectedList = splitCsvParamArray(expected);
        assertThat(result)
            .containsExactlyInAnyOrderElementsOf(expectedList);

    }

    @DisplayName("When an case has a null doctor specialism return an empty list.")
    @Test
    void testWhenAnCaseHasAnNullDoctorSpecialismReturnAnEmptyList() {

        SessionCategoryMap sessionCategoryMap = new SessionCategoryMap(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
            false, false, SessionCategory.CATEGORY_05, null
        );

        SscsCaseData caseData = SscsCaseData.builder()
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .sscsIndustrialInjuriesData(SscsIndustrialInjuriesData.builder()
                .panelDoctorSpecialism("doesntexist")
                .build())
            .build();

        List<String> result = HearingsPanelMapping.getPanelSpecialisms(caseData, sessionCategoryMap);

        List<String> expectedList = Collections.emptyList();
        assertThat(result)
            .containsExactlyInAnyOrderElementsOf(expectedList);

    }

    @DisplayName("When a case benefit is CHILD_SUPPORT then return empty list.")
    @Test
    void testWhenAnCaseBenefitChildSupportReturnAnEmptyList() {

        SessionCategoryMap sessionCategoryMap = new SessionCategoryMap(BenefitCode.CHILD_SUPPORT_ASSESSMENTS, Issue.DD,
                                                                       false, false, SessionCategory.CATEGORY_05, null
        );

        SscsCaseData caseData = SscsCaseData.builder()
            .benefitCode(CHILD_SUPPORT_BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .sscsIndustrialInjuriesData(SscsIndustrialInjuriesData.builder()
                                            .panelDoctorSpecialism("doesntexist")
                                            .build())
            .build();

        List<String> result = HearingsPanelMapping.getPanelSpecialisms(caseData, sessionCategoryMap);

        List<String> expectedList = Collections.emptyList();
        assertThat(result)
            .containsExactlyInAnyOrderElementsOf(expectedList);

    }


    @DisplayName("When a non doctor panel member is given getPanelMemberSpecialism returns the valid reference")
    @ParameterizedTest
    @EnumSource(
        value = PanelMember.class,
        mode = EnumSource.Mode.EXCLUDE,
        names = {"MQPM1", "MQPM2"}
    )
    void testGetPanelMemberSpecialism(PanelMember value) {
        String result = HearingsPanelMapping.getPanelMemberSpecialism(value, null, null);

        assertThat(result).isEqualTo(value.getReference());

    }
}
