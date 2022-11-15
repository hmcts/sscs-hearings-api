package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import uk.gov.hmcts.reform.sscs.ccd.domain.AdjournCaseNextHearingDurationType;
import uk.gov.hmcts.reform.sscs.ccd.domain.AdjournCaseNextHearingDurationUnits;
import uk.gov.hmcts.reform.sscs.ccd.domain.Adjournment;
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
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingDuration;
import uk.gov.hmcts.reform.sscs.reference.data.service.HearingDurationsService;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.sscs.ccd.domain.AdjournCaseNextHearingDurationUnits.MINUTES;
import static uk.gov.hmcts.reform.sscs.ccd.domain.AdjournCaseNextHearingDurationUnits.SESSIONS;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HearingsDurationMappingTest extends HearingsMappingBase {
    public static final int DURATION_FACE_TO_FACE = 60;
    public static final int DURATION_INTERPRETER = 75;
    public static final int DURATION_PAPER = 40;
    @Mock
    private HearingDurationsService hearingDurations;

    private SscsCaseData adjournCaseBuilder(
        Integer adjournCaseDuration,
        AdjournCaseNextHearingDurationUnits adjournCaseDurationUnits
    ) {
        return SscsCaseData.builder()
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .adjournment(Adjournment.builder()
                .nextHearingListingDurationType(AdjournCaseNextHearingDurationType.NON_STANDARD)
                .nextHearingListingDuration(adjournCaseDuration)
                .nextHearingListingDurationUnits(adjournCaseDurationUnits)
                .isAdjournmentInProgress(YesNo.YES)
                .build())
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

    @Mock
    private ReferenceDataServiceHolder referenceDataServiceHolder;

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class AdjournmentFlagEnabled {

        @BeforeEach
        void setUp() {
            given(referenceDataServiceHolder.isAdjournmentFlagEnabled()).willReturn(true);
        }

        @DisplayName("When a valid adjournCaseDuration and adjournCaseDurationUnits is given "
            + "getHearingDuration returns the correct duration Parameterized Tests")
        @ParameterizedTest
        @MethodSource
        void getHearingDuration(Integer adjournCaseDuration, AdjournCaseNextHearingDurationUnits adjournCaseDurationUnits, int expected) {
            SscsCaseData caseData = adjournCaseBuilder(
                adjournCaseDuration,
                adjournCaseDurationUnits
            );

            Integer result = HearingsDurationMapping.getHearingDuration(caseData, referenceDataServiceHolder);

            assertThat(result).isEqualTo(expected);
        }

        private Stream<Arguments> getHearingDuration() {
            return Stream.of(
              Arguments.of(120, MINUTES, 120),
              Arguments.of(70, MINUTES, 70),
              Arguments.of(1, SESSIONS, 165),
              Arguments.of(2, SESSIONS, 330),
              Arguments.of(3, SESSIONS, 495)
            );
        }

        @DisplayName("When adjournment flag is enabled but getHearingDurationAdjournment returns null "
            + "uses default hearing duration")
        @Test
        void getHearingDurationAdjournedFeatureFlagEnabled() {
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

        @DisplayName("When a valid duration is given but adjournCaseDurationUnits is not provided "
            + "getHearingDuration returns the default adjournment duration")
        @Test
        void getHearingDurationNullUnits() {
            given(hearingDurations.getHearingDuration(BENEFIT_CODE, ISSUE_CODE))
                .willReturn(new HearingDuration(
                    BenefitCode.PIP_NEW_CLAIM,
                    Issue.DD,
                    DURATION_FACE_TO_FACE,
                    DURATION_INTERPRETER,
                    DURATION_PAPER
                ));

            given(referenceDataServiceHolder.getHearingDurations()).willReturn(hearingDurations);
            SscsCaseData caseData = adjournCaseBuilder(2, null);

            int result = HearingsDurationMapping.getHearingDuration(caseData, referenceDataServiceHolder);

            assertThat(result).isEqualTo(DURATION_PAPER);
        }

        @DisplayName("When an invalid adjournCaseDuration and valid adjournCaseDurationUnits is given "
            + "getHearingDuration a null pointer exception is thrown")
        @ParameterizedTest
        @MethodSource
        void getHearingDurationFailure(Integer adjournCaseDuration, AdjournCaseNextHearingDurationUnits adjournCaseDurationUnits) {
            SscsCaseData caseData = adjournCaseBuilder(adjournCaseDuration, adjournCaseDurationUnits);

            assertThatThrownBy(() -> HearingsDurationMapping.getHearingDuration(caseData, referenceDataServiceHolder))
                .isInstanceOf(NullPointerException.class);
        }

        private Stream<Arguments> getHearingDurationFailure() {
            return Stream.of(
              Arguments.of(null, SESSIONS),
              Arguments.of(0, SESSIONS),
              Arguments.of(0, MINUTES),
              Arguments.of(null, MINUTES)
            );
        }


        @DisplayName("When getAdjournCaseNextHearingListingDurationType is non standard and  "
            + "nextHearingListingDuration is blank, getHearingDurationAdjournment returns null")
        @Test
        void getHearingDurationAdjournment_nextHearingListingDurationIsBlank() {
            SscsCaseData caseData = adjournCaseBuilder(null, SESSIONS);

            Integer result = HearingsDurationMapping.getHearingDurationAdjournment(caseData, referenceDataServiceHolder);

            assertThat(result).isNull();
        }

        @DisplayName("When getAdjournCaseNextHearingListingDurationType is standard "
            + "getHearingDurationAdjournment returns null")
        @Test
        void getHearingDurationAdjournment_nextHearingListingDurationTypeIsStandard() {
            SscsCaseData caseData = adjournCaseBuilder(null, SESSIONS);
            caseData.getAdjournment().setNextHearingListingDurationType(AdjournCaseNextHearingDurationType.STANDARD);

            Integer result = HearingsDurationMapping.getHearingDurationAdjournment(caseData, referenceDataServiceHolder);

            assertThat(result).isNull();
        }
    }

    @DisplayName("When adjournment flag is disabled getHearingDurationAdjournment returns null")
    @Test
    void getHearingDurationAdjournedFeatureFlagDisabled() {
        given(referenceDataServiceHolder.isAdjournmentFlagEnabled()).willReturn(false);

        SscsCaseData caseData = adjournCaseBuilder(100, MINUTES);

        Integer result = HearingsDurationMapping.getHearingDurationAdjournment(caseData, referenceDataServiceHolder);

        assertThat(result).isNull();
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class HearingDurationsProvided {

        @BeforeEach
        void setUp() {
            given(hearingDurations.getHearingDuration(BENEFIT_CODE, ISSUE_CODE))
                .willReturn(new HearingDuration(
                BenefitCode.PIP_NEW_CLAIM,
                Issue.DD,
                DURATION_FACE_TO_FACE,
                DURATION_INTERPRETER,
                DURATION_PAPER
            ));
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
                .adjournment(Adjournment.builder()
                    .nextHearingListingDuration(null)
                    .nextHearingListingDurationUnits(null)
                    .build())
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


        @DisplayName("When wantsToAttend for the Appeal is no and the hearing type is paper "
            + "getHearingDurationBenefitIssueCodes return the correct paper durations")
        @Test
        void getHearingDurationBenefitIssueCodesPaper() {

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

        @DisplayName("When wantsToAttend for the Appeal is No and the hearing type is paper "
            + "getHearingDurationBenefitIssueCodes return the correct paper durations")
        @Test
        void getHearingDurationBenefitIssueCodesNotAttendNotPaper() {

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

            assertThat(result).isEqualTo(DURATION_PAPER);
        }

    }

    @DisplayName("When an invalid adjournCaseDuration and adjournCaseDurationUnits is given and overrideDuration "
        + "is present then override the duration of hearing")
    @Test
    void getHearingDurationWillReturnOverrideDurationWhenPresent() {
        SscsCaseData caseData = SscsCaseData.builder()
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .adjournment(Adjournment.builder()
                .nextHearingListingDuration(null)
                .nextHearingListingDurationUnits(null)
                .build())
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
