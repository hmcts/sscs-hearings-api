package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import uk.gov.hmcts.reform.sscs.ccd.domain.AdjournCaseNextHearingDurationType;
import uk.gov.hmcts.reform.sscs.ccd.domain.AdjournCaseNextHearingDurationUnits;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.BenefitCode;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.Issue;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingDuration;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.sscs.ccd.domain.AdjournCaseNextHearingDurationUnits.SESSIONS;

class HearingsDurationMappingAdjournmentTest extends HearingsMappingBase {

    @Mock
    private ReferenceDataServiceHolder refData;

    private void setAdjournmentDurationAndUnits(Integer duration, AdjournCaseNextHearingDurationUnits units) {
        caseData.getAdjournment().setNextHearingListingDuration(duration);
        caseData.getAdjournment().setNextHearingListingDurationUnits(units);
    }

    @BeforeEach
    void setUp() {
        caseData.getAdjournment().setNextHearingListingDurationType(AdjournCaseNextHearingDurationType.NON_STANDARD);
        caseData.getAdjournment().setAdjournmentInProgress(YesNo.YES);
    }

    @DisplayName("When a valid adjournCaseDuration and adjournCaseDurationUnits is given "
        + "getHearingDuration returns the correct duration Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "120,MINUTES,120",
        "70,MINUTES,70",
        "1,SESSIONS,165",
        "2,SESSIONS,330",
        "3,SESSIONS,495"
    })
    void getHearingDuration(
        Integer adjournCaseDuration,
        AdjournCaseNextHearingDurationUnits adjournCaseDurationUnits,
        int expected) {
        given(refData.isAdjournmentFlagEnabled()).willReturn(true);

        setAdjournmentDurationAndUnits(adjournCaseDuration, adjournCaseDurationUnits);
        Integer result = HearingsDurationMapping.getHearingDuration(caseData, refData);

        assertThat(result).isEqualTo(expected);
    }

    @DisplayName("When adjournment flag is enabled but getHearingDurationAdjournment returns null "
        + "uses default hearing duration")
    @Test
    void getHearingDurationAdjournmentReturnsNullWithFeatureFlagEnabled() {
        given(refData.getHearingDurations()).willReturn(hearingDurations);

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

        Integer durationAdjourned = HearingsDurationMapping.getHearingDurationAdjournment(caseData);
        assertThat(durationAdjourned).isNull();

        Integer result = HearingsDurationMapping.getHearingDuration(
            caseData,
            refData
        );

        assertThat(result).isEqualTo(HearingsDurationMapping.DURATION_DEFAULT);
    }

    @DisplayName("When a valid duration is given but adjournCaseDurationUnits is not provided "
        + "getHearingDuration returns the default adjournment duration")
    @Test
    void getHearingDurationWithNullUnits() {
        given(hearingDurations.getHearingDuration(BENEFIT_CODE, ISSUE_CODE))
            .willReturn(new HearingDuration(
                BenefitCode.PIP_NEW_CLAIM,
                Issue.DD,
                HearingsDurationMappingTest.DURATION_FACE_TO_FACE,
                HearingsDurationMappingTest.DURATION_INTERPRETER,
                HearingsDurationMappingTest.DURATION_PAPER
            ));

        given(refData.getHearingDurations()).willReturn(hearingDurations);
        setAdjournmentDurationAndUnits(2, null);

        int result = HearingsDurationMapping.getHearingDuration(caseData, refData);

        assertThat(result).isEqualTo(HearingsDurationMappingTest.DURATION_PAPER);
    }

    @DisplayName("When an invalid adjournCaseDuration and valid adjournCaseDurationUnits is given "
        + "getHearingDuration a null pointer exception is thrown")
    @ParameterizedTest
    @CsvSource(value = {
        "null,SESSIONS",
        "0,SESSIONS",
        "null,MINUTES",
        "0,MINUTES"
    }, nullValues = {"null"})
    void getHearingDurationWithInvalidUnitsThrowsException(
        Integer adjournCaseDuration,
        AdjournCaseNextHearingDurationUnits adjournCaseDurationUnits
    ) {
        setAdjournmentDurationAndUnits(adjournCaseDuration, adjournCaseDurationUnits);

        assertThatThrownBy(() -> HearingsDurationMapping.getHearingDuration(caseData, refData))
            .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("When getAdjournCaseNextHearingListingDurationType is non standard and  "
        + "nextHearingListingDuration is blank, getHearingDurationAdjournment returns null")
    @Test
    void getHearingDurationAdjournment_nextHearingListingDurationIsBlank() {
        setAdjournmentDurationAndUnits(null, SESSIONS);

        Integer result = HearingsDurationMapping.getHearingDurationAdjournment(caseData);

        assertThat(result).isNull();
    }

    @DisplayName("When getAdjournCaseNextHearingListingDurationType is standard "
        + "getHearingDurationAdjournment returns null")
    @Test
    void getHearingDurationAdjournment_nextHearingListingDurationTypeIsStandard() {
        setAdjournmentDurationAndUnits(null, SESSIONS);
        caseData.getAdjournment().setNextHearingListingDurationType(AdjournCaseNextHearingDurationType.STANDARD);

        Integer result = HearingsDurationMapping.getHearingDurationAdjournment(caseData);

        assertThat(result).isNull();
    }

}
