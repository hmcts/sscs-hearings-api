package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.BenefitCode;
import uk.gov.hmcts.reform.sscs.ccd.domain.CcdValue;
import uk.gov.hmcts.reform.sscs.ccd.domain.ElementDisputed;
import uk.gov.hmcts.reform.sscs.ccd.domain.ElementDisputedDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingSubtype;
import uk.gov.hmcts.reform.sscs.ccd.domain.Issue;
import uk.gov.hmcts.reform.sscs.ccd.domain.OtherParty;
import uk.gov.hmcts.reform.sscs.ccd.domain.OverrideFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.SchedulingAndListingFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingDuration;
import uk.gov.hmcts.reform.sscs.reference.data.service.HearingDurationsService;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

class HearingsDurationMappingTest extends HearingsMappingBase {
    public static final int DURATION_FACE_TO_FACE = 60;
    public static final int DURATION_INTERPRETER = 75;
    public static final int DURATION_PAPER = 40;
    @Mock
    private HearingDurationsService hearingDurations;

    private SscsCaseData adjourningCaseBuilder(String adjournCaseDuration, String adjournCaseDurationUnits) {
        return SscsCaseData.builder()
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .adjournCaseNextHearingListingDurationType("nonStandardTimeSlot")
            .adjournCaseNextHearingListingDuration(adjournCaseDuration)
            .adjournCaseNextHearingListingDurationUnits(adjournCaseDurationUnits)
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder().build())
                .build())
            .build();
    }

    private SscsCaseData wantsToAttendCaseBuilder(String wantsToAttend) {
        return SscsCaseData.builder()
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend(wantsToAttend)
                    .build())
                .build())
            .build();
    }

    @NotNull
    private static HearingDuration generateHearingDuration() {
        return new HearingDuration(
            BenefitCode.PIP_NEW_CLAIM,
            Issue.DD,
            DURATION_FACE_TO_FACE,
            DURATION_INTERPRETER,
            DURATION_PAPER
        );
    }

    private void adjournmentFlagEnabled(boolean value) {
        given(referenceDataServiceHolder.isAdjournmentFlagEnabled()).willReturn(value);
    }

    @Mock
    private ReferenceDataServiceHolder referenceDataServiceHolder;

    @DisplayName("When a valid adjournCaseDuration and adjournCaseDurationUnits is given "
        + "getHearingDuration returns the correct duration Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "120,minutes,120",
        "70,minutes,70",
        "1,sessions,165",
        "2,sessions,330",
        "3,sessions,495",
    }, nullValues = {"null"})
    void getHearingDuration(String adjournCaseDuration, String adjournCaseDurationUnits, int expected) {
        adjournmentFlagEnabled(true);

        SscsCaseData caseData = adjourningCaseBuilder(adjournCaseDuration, adjournCaseDurationUnits);

        int result = HearingsDurationMapping.getHearingDuration(caseData, referenceDataServiceHolder);

        assertThat(result).isEqualTo(expected);
    }

    @DisplayName("When an invalid adjournCaseDuration or adjournCaseDurationUnits is given "
        + "getHearingDuration returns the default duration Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "null,null",
        "null,60",
        "1,test",
        "0,minutes",
        "0,sessions"
    }, nullValues = {"null"})
    void getHearingDuration(String adjournCaseDuration, String adjournCaseDurationUnits) {
        // TODO Finish Test when method done
        adjournmentFlagEnabled(true);

        given(hearingDurations.getHearingDuration(BENEFIT_CODE, ISSUE_CODE))
            .willReturn(generateHearingDuration());

        given(referenceDataServiceHolder.getHearingDurations()).willReturn(hearingDurations);
        SscsCaseData caseData = adjourningCaseBuilder(adjournCaseDuration, adjournCaseDurationUnits);

        int result = HearingsDurationMapping.getHearingDuration(caseData, referenceDataServiceHolder);

        assertThat(result).isEqualTo(DURATION_PAPER);
    }

    @DisplayName("When adjournment flag is disabled getHearingDurationAdjournment returns null")
    @Test
    void getHearingDurationAdjournedFeatureFlagDisabled() {
        adjournmentFlagEnabled(false);

        SscsCaseData caseData = adjourningCaseBuilder("100", "minutes");

        Integer result = HearingsDurationMapping.getHearingDurationAdjournment(caseData, referenceDataServiceHolder);

        assertThat(result).isNull();
    }


    @DisplayName("When adjournment flag is enabled but getHearingDurationAdjournment returns null "
        + "uses default duration")
    @Test
    void getHearingDurationAdjournedFeatureFlagEnabled() {
        adjournmentFlagEnabled(true);

        given(referenceDataServiceHolder.getHearingDurations()).willReturn(hearingDurations);

        given(hearingDurations.getHearingDuration(BENEFIT_CODE, ISSUE_CODE)).willReturn(null);

        SscsCaseData caseData = SscsCaseData.builder()
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend("Yes")
                    .build())
                .build())
            .build();

        Integer durationAdjourned = HearingsDurationMapping.getHearingDurationAdjournment(
            caseData, referenceDataServiceHolder);
        assertThat(durationAdjourned).isNull();

        Integer result = HearingsDurationMapping.getHearingDuration(
            caseData,
            referenceDataServiceHolder
        );

        assertThat(result).isEqualTo(HearingsDurationMapping.DURATION_DEFAULT);
    }

    @DisplayName("When an invalid adjournCaseDuration and valid adjournCaseDurationUnits is given "
        + "getHearingDuration a null pointer exception is thrown")
    @ParameterizedTest
    @CsvSource(value = {
        ",sessions",
        "0,sessions",
        "0,minutes",
    }, nullValues = {"null"})
    void getHearingDurationFailure(String adjournCaseDuration, String adjournCaseDurationUnits) {
        SscsCaseData caseData = adjourningCaseBuilder(adjournCaseDuration, adjournCaseDurationUnits);

        assertThatThrownBy(() -> HearingsDurationMapping.getHearingDuration(caseData, referenceDataServiceHolder))
            .isInstanceOf(NullPointerException.class);
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
            .willReturn(generateHearingDuration());

        given(hearingDurations.addExtraTimeIfNeeded(
            eq(DURATION_FACE_TO_FACE),
            eq(BenefitCode.PIP_NEW_CLAIM),
            eq(Issue.DD),
            any()
        ))
            .willReturn(DURATION_INTERPRETER);

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

    @DisplayName("When getAdjournCaseNextHearingListingDurationType is non standard and  "
        + "nextHearingListingDuration is blank, getHearingDurationAdjournment returns null")
    @Test
    void getHearingDurationAdjournment_nextHearingListingDurationIsBlank() {
        adjournmentFlagEnabled(true);

        SscsCaseData caseData = adjourningCaseBuilder("", "1");

        Integer result = HearingsDurationMapping.getHearingDurationAdjournment(caseData, referenceDataServiceHolder);

        assertThat(result).isNull();
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
                    .duration(DURATION_FACE_TO_FACE)
                    .build())
                .build())
            .build();

        int result = HearingsDurationMapping.getHearingDuration(caseData, referenceDataServiceHolder);

        assertThat(result).isEqualTo(DURATION_FACE_TO_FACE);
    }

    @DisplayName("When getAdjournCaseNextHearingListingDurationType is non standard and  "
        + "nextHearingListingDuration is blank, getHearingDurationAdjournment returns null")
    @Test
    void getHearingDurationAdjournment_nextHearingListingDurationIsBlank() {
        adjournmentFlagEnabled(true);

        SscsCaseData caseData = adjourningCaseBuilder("", "1");

        Integer result = HearingsDurationMapping.getHearingDurationAdjournment(caseData, referenceDataServiceHolder);

        assertThat(result).isNull();
    }

    @DisplayName("When the benefit or issue code is null "
        + "getHearingDurationBenefitIssueCodes returns null Parameterized Tests")
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

    @DisplayName("When wantsToAttend for the Appeal is no and the hearing type is paper "
        + "getHearingDurationBenefitIssueCodes return the correct paper durations")
    @Test
    void getHearingDurationBenefitIssueCodesPaper() {

        given(hearingDurations.getHearingDuration(BENEFIT_CODE, ISSUE_CODE))
            .willReturn(generateHearingDuration());

        given(referenceDataServiceHolder.getHearingDurations()).willReturn(hearingDurations);

        SscsCaseData caseData = wantsToAttendCaseBuilder("No");

        Integer result = HearingsDurationMapping.getHearingDurationBenefitIssueCodes(
            caseData,
            referenceDataServiceHolder
        );

        assertThat(result).isEqualTo(DURATION_PAPER);
    }

    @DisplayName("When wantsToAttend for the Appeal is no and the hearing type is not paper "
        + "getHearingDurationBenefitIssueCodes returns null")
    @Test
    void getHearingDurationBenefitIssueCodesNotPaper() {

        given(hearingDurations.getHearingDuration(BENEFIT_CODE, ISSUE_CODE))
            .willReturn(generateHearingDuration());

        given(referenceDataServiceHolder.getHearingDurations()).willReturn(hearingDurations);

        List<CcdValue<OtherParty>> otherParties = List.of(new CcdValue<>(
            OtherParty.builder()
            .hearingOptions(HearingOptions.builder()
                .wantsToAttend("yes")
                .build())
            .hearingSubtype(HearingSubtype.builder()
                .wantsHearingTypeTelephone("yes")
                .hearingTelephoneNumber("123123")
                .build())
            .build())
        );

        SscsCaseData caseData = SscsCaseData.builder()
            .otherParties(otherParties)
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend("no")
                    .build())
                .build())
            .build();

        Integer result = HearingsDurationMapping.getHearingDurationBenefitIssueCodes(
            caseData,
            referenceDataServiceHolder
        );

        assertThat(result).isNull();
    }

    @DisplayName("When wantsToAttend for the Appeal is no and the hearing type is not paper "
        + "getHearingDurationBenefitIssueCodes returns null")
    @Test
    void getHearingDurationBenefitIssueCodesNotPaper() {

        given(hearingDurations.getHearingDuration(BENEFIT_CODE, ISSUE_CODE))
            .willReturn(new HearingDuration(BenefitCode.PIP_NEW_CLAIM, Issue.DD, 60, 75, 30));

        given(referenceDataServiceHolder.getHearingDurations()).willReturn(hearingDurations);

        List<CcdValue<OtherParty>> otherParties = List.of(new CcdValue<>(
            OtherParty.builder()
            .hearingOptions(HearingOptions.builder()
                .wantsToAttend("yes")
                .build())
            .hearingSubtype(HearingSubtype.builder()
                .wantsHearingTypeTelephone("yes")
                .hearingTelephoneNumber("123123")
                .build())
            .build())
        );

        SscsCaseData caseData = SscsCaseData.builder()
            .otherParties(otherParties)
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend("no")
                    .build())
                .build())
            .build();

        Integer result = HearingsDurationMapping.getHearingDurationBenefitIssueCodes(
            caseData,
            referenceDataServiceHolder
        );

        assertThat(result).isNull();
    }

    @DisplayName("When wantsToAttend for the Appeal is Yes and languageInterpreter is null "
        + "getHearingDurationBenefitIssueCodes return the correct face to face durations")
    @Test
    void getHearingDurationBenefitIssueCodesFaceToFace() {
        given(hearingDurations.getHearingDuration(BENEFIT_CODE, ISSUE_CODE))
            .willReturn(generateHearingDuration());

        given(hearingDurations.addExtraTimeIfNeeded(
            eq(DURATION_FACE_TO_FACE),
            eq(BenefitCode.PIP_NEW_CLAIM),
            eq(Issue.DD),
            any()
        ))
            .willReturn(DURATION_FACE_TO_FACE);

        given(referenceDataServiceHolder.getHearingDurations()).willReturn(hearingDurations);

        SscsCaseData caseData = wantsToAttendCaseBuilder("Yes");

        Integer result = HearingsDurationMapping.getHearingDurationBenefitIssueCodes(
            caseData,
            referenceDataServiceHolder
        );

        assertThat(result).isEqualTo(DURATION_FACE_TO_FACE);
    }

    @DisplayName("When wantsToAttend for the Appeal is Yes "
        + "getHearingDurationBenefitIssueCodes return the correct interpreter durations")
    @Test
    void getHearingDurationBenefitIssueCodesInterpreter() {
        given(hearingDurations.getHearingDuration(BENEFIT_CODE, ISSUE_CODE))
            .willReturn(generateHearingDuration());

        given(hearingDurations.addExtraTimeIfNeeded(
            eq(DURATION_INTERPRETER),
            eq(BenefitCode.PIP_NEW_CLAIM),
            eq(Issue.DD),
            any()
        ))
            .willReturn(DURATION_INTERPRETER);

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

        assertThat(result).isEqualTo(DURATION_INTERPRETER);
    }

    @DisplayName("getElementsDisputed returns empty list when elementDisputed is Null")
    @Test
    void getElementsDisputedNull() {
        SscsCaseData caseData = SscsCaseData.builder().build();

        List<String> result = HearingsDurationMapping.getElementsDisputed(caseData);

        assertThat(result).isEmpty();
    }

    @DisplayName("getElementsDisputed returns a List of elements of all elements in "
        + "each of the elementDisputed fields in SscsCaseData")
    @Test
    void getElementsDisputed() {
        ElementDisputed elementDisputed = ElementDisputed.builder()
            .value(ElementDisputedDetails.builder()
                .issueCode("WC")
                .outcome("Test")
                .build())
            .build();
        SscsCaseData caseData = SscsCaseData.builder()
            .elementsDisputedGeneral(List.of(elementDisputed))
            .elementsDisputedSanctions(List.of(elementDisputed))
            .elementsDisputedOverpayment(List.of(elementDisputed))
            .elementsDisputedHousing(List.of(elementDisputed))
            .elementsDisputedChildCare(List.of(elementDisputed))
            .elementsDisputedCare(List.of(elementDisputed))
            .elementsDisputedChildElement(List.of(elementDisputed))
            .elementsDisputedChildDisabled(List.of(elementDisputed))
            .elementsDisputedLimitedWork(List.of(elementDisputed))
            .build();
        List<String> result = HearingsDurationMapping.getElementsDisputed(caseData);

        assertThat(result)
            .hasSize(9)
            .containsOnly("WC");
    }

}
