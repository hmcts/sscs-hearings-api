package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingDuration;
import uk.gov.hmcts.reform.sscs.reference.data.service.HearingDurationsService;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

public class HearingsDurationMappingTest  extends HearingsMappingBase {
    @Mock
    private HearingDurationsService hearingDurations;

    @Mock
    private ReferenceDataServiceHolder referenceDataServiceHolder;


    @DisplayName("when a valid adjournCaseDuration and adjournCaseDurationUnits is given getHearingDuration returns the correct duration Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "120,minutes,120",
        "1,sessions,165",
    }, nullValues = {"null"})
    void getHearingDuration(String adjournCaseDuration, String adjournCaseDurationUnits, int expected) {

        SscsCaseData caseData = SscsCaseData.builder()
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .adjournCaseNextHearingListingDurationType("nonStandardTimeSlot")
            .adjournCaseNextHearingListingDuration(adjournCaseDuration)
            .adjournCaseNextHearingListingDurationUnits(adjournCaseDurationUnits)
            .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder().build())
                        .build())
            .build();
        int result = HearingsDurationMapping.getHearingDuration(caseData, referenceDataServiceHolder);

        assertEquals(expected, result);
    }

    @DisplayName("when a invalid adjournCaseDuration or adjournCaseDurationUnits is given getHearingDuration returns the default duration Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "null,null",
        "null,60",
        "1,test",
    }, nullValues = {"null"})
    void getHearingDuration(String adjournCaseDuration, String adjournCaseDurationUnits) {
        // TODO Finish Test when method done
        given(hearingDurations.getHearingDuration(BENEFIT_CODE, ISSUE_CODE))
            .willReturn(new HearingDuration(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                                            60, 75, 30
            ));

        given(referenceDataServiceHolder.getHearingDurations()).willReturn(hearingDurations);

        SscsCaseData caseData = SscsCaseData.builder()
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .adjournCaseNextHearingListingDuration(adjournCaseDuration)
            .adjournCaseNextHearingListingDurationUnits(adjournCaseDurationUnits)
            .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder().build())
                        .build())
            .build();
        int result = HearingsDurationMapping.getHearingDuration(caseData, referenceDataServiceHolder);

        assertEquals(30, result);
    }

    @DisplayName("When an invalid adjournCaseDuration and adjournCaseDurationUnits is given and overrideDuration "
        + "is not present then override the duration of hearing")
    @ParameterizedTest
    @CsvSource(value = {
        "null,75",
        "0,75",
        "-1, 75"
    }, nullValues = {"null"})
    void getHearingDurationWillNotReturnOverrideDurationWhenPresent(Integer overrideDuration, int expectedResult) {
        given(hearingDurations.getHearingDuration(BENEFIT_CODE, ISSUE_CODE))
            .willReturn(new HearingDuration(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                                            60, 75, 30
            ));

        given(hearingDurations.addExtraTimeIfNeeded(eq(60), eq(BenefitCode.PIP_NEW_CLAIM), eq(Issue.DD), any()))
            .willReturn(75);

        given(referenceDataServiceHolder.getHearingDurations()).willReturn(hearingDurations);

        SscsCaseData caseData = SscsCaseData.builder()
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .adjournCaseNextHearingListingDuration(null)
            .adjournCaseNextHearingListingDurationUnits(null)
            .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder()
                                            .wantsToAttend("Yes")
                                            .build())
                        .build())
            .schedulingAndListingFields(SchedulingAndListingFields.builder()
                                            .overrideFields(OverrideFields.builder()
                                                                .duration(overrideDuration)
                                                                .build())
                                            .build())
            .build();

        int result = HearingsDurationMapping.getHearingDuration(caseData, referenceDataServiceHolder);

        assertThat(result).isEqualTo(expectedResult);
    }

    @DisplayName("When an invalid adjournCaseDuration and adjournCaseDurationUnits is given and overrideDuration "
        + "is present then override the duration of hearing")
    @Test
    void getHearingDurationWillReturnOverrideDurationWhenPresent() {
        SscsCaseData caseData = SscsCaseData.builder()
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .adjournCaseNextHearingListingDuration(null)
            .adjournCaseNextHearingListingDurationUnits(null)
            .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder()
                                            .wantsToAttend("Yes")
                                            .build())
                        .build())
            .schedulingAndListingFields(SchedulingAndListingFields.builder()
                                            .overrideFields(OverrideFields.builder()
                                                                .duration(60)
                                                                .build())
                                            .build())
            .build();

        int result = HearingsDurationMapping.getHearingDuration(caseData, referenceDataServiceHolder);

        assertThat(result).isEqualTo(60);
    }

    @DisplayName("When the benefit or issue code is null getHearingDurationBenefitIssueCodes returns null Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "null,null",
        "002,null",
        "null,DD",
    }, nullValues = {"null"})
    void getHearingDurationBenefitIssueCodesPaper(String benefitCode, String issueCode) {

        given(hearingDurations.getHearingDuration(benefitCode, issueCode)).willReturn(null);

        given(referenceDataServiceHolder.getHearingDurations()).willReturn(hearingDurations);

        SscsCaseData caseData = SscsCaseData.builder()
            .benefitCode(benefitCode)
            .issueCode(issueCode)
            .appeal(Appeal.builder()
                        .hearingSubtype(HearingSubtype.builder().build())
                        .hearingOptions(HearingOptions.builder().build())
                        .build())
            .build();

        Integer result = HearingsDurationMapping.getHearingDurationBenefitIssueCodes(
            caseData,
            referenceDataServiceHolder
        );

        assertThat(result).isNull();
    }

    @DisplayName("When wantsToAttend for the Appeal is null and the hearing type is paper "
        + "getHearingDurationBenefitIssueCodes return the correct paper durations")
    @Test
    void getHearingDurationBenefitIssueCodesPaper() {

        given(hearingDurations.getHearingDuration(BENEFIT_CODE, ISSUE_CODE))
            .willReturn(new HearingDuration(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                                            60, 75, 30
            ));

        given(referenceDataServiceHolder.getHearingDurations()).willReturn(hearingDurations);

        SscsCaseData caseData = SscsCaseData.builder()
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .appeal(Appeal.builder()
                        .hearingSubtype(HearingSubtype.builder().build())
                        .hearingOptions(HearingOptions.builder()
                                            .wantsToAttend("No")
                                            .build())
                        .build())
            .build();

        Integer result = HearingsDurationMapping.getHearingDurationBenefitIssueCodes(
            caseData,
            referenceDataServiceHolder
        );

        assertThat(result).isEqualTo(30);
    }

    @DisplayName("When wantsToAttend for the Appeal is Yes and languageInterpreter is null "
        + "getHearingDurationBenefitIssueCodes return the correct face to face durations")
    @Test
    void getHearingDurationBenefitIssueCodesFaceToFace() {
        given(hearingDurations.getHearingDuration(BENEFIT_CODE, ISSUE_CODE))
            .willReturn(new HearingDuration(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                                            60, 75, 30
            ));

        given(hearingDurations.addExtraTimeIfNeeded(eq(60), eq(BenefitCode.PIP_NEW_CLAIM), eq(Issue.DD), any()))
            .willReturn(60);

        given(referenceDataServiceHolder.getHearingDurations()).willReturn(hearingDurations);

        SscsCaseData caseData = SscsCaseData.builder()
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder()
                                            .wantsToAttend("Yes")
                                            .build())
                        .build())
            .build();

        Integer result = HearingsDurationMapping.getHearingDurationBenefitIssueCodes(
            caseData,
            referenceDataServiceHolder
        );

        assertThat(result).isEqualTo(60);
    }

    @DisplayName("When wantsToAttend for the Appeal is Yes "
        + "getHearingDurationBenefitIssueCodes return the correct interpreter durations")
    @Test
    void getHearingDurationBenefitIssueCodesInterpreter() {
        given(hearingDurations.getHearingDuration(BENEFIT_CODE, ISSUE_CODE))
            .willReturn(new HearingDuration(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                                            60, 75, 30
            ));

        given(hearingDurations.addExtraTimeIfNeeded(eq(75), eq(BenefitCode.PIP_NEW_CLAIM), eq(Issue.DD), any()))
            .willReturn(75);

        given(referenceDataServiceHolder.getHearingDurations()).willReturn(hearingDurations);

        SscsCaseData caseData = SscsCaseData.builder()
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder()
                                            .wantsToAttend("Yes")
                                            .languageInterpreter("Yes")
                                            .build())
                        .build())
            .build();

        Integer result = HearingsDurationMapping.getHearingDurationBenefitIssueCodes(
            caseData,
            referenceDataServiceHolder
        );

        assertThat(result).isEqualTo(75);
    }

    @DisplayName("When wantsToAttend for the Appeal is No and the hearing type is not paper "
        + "getHearingDurationBenefitIssueCodes return the correct paper durations")
    @Test
    void getHearingDurationBenefitIssueCodesNotAttendNotPaper() {

        given(hearingDurations.getHearingDuration(BENEFIT_CODE, ISSUE_CODE))
            .willReturn(new HearingDuration(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                                            60, 75, 30
            ));

        given(referenceDataServiceHolder.getHearingDurations()).willReturn(hearingDurations);

        SscsCaseData caseData = SscsCaseData.builder()
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .appeal(Appeal.builder()
                        .hearingSubtype(HearingSubtype.builder().build())
                        .hearingOptions(HearingOptions.builder()
                                            .wantsToAttend("No")
                                            .build())
                        .build())
            .dwpIsOfficerAttending("Yes")
            .build();

        Integer result = HearingsDurationMapping.getHearingDurationBenefitIssueCodes(
            caseData,
            referenceDataServiceHolder
        );

        assertThat(result).isNull();
    }

}
