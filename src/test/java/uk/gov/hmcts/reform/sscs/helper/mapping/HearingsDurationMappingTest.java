package uk.gov.hmcts.reform.sscs.helper.mapping;

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
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingDuration;
import uk.gov.hmcts.reform.sscs.reference.data.service.HearingDurationsService;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

class HearingsDurationMappingTest extends HearingsMappingBase {

    @Mock
    private HearingDurationsService hearingDurations;

    @Mock
    private ReferenceDataServiceHolder referenceDataServiceHolder;

    @DisplayName("When an invalid adjournCaseDuration and adjournCaseDurationUnits is given and overrideDuration "
        + "is not present then override the duration of hearing")
    @ParameterizedTest
    @CsvSource(value = {
        "null,75",
        "0,75",
        "-1, 75"
    }, nullValues = {"null"})
    void getHearingDurationWillNotReturnOverrideDurationWhenPresent(Integer overrideDuration, int expectedResult) {
        given(referenceDataServiceHolder.getHearingDurations()).willReturn(hearingDurations);
        given(hearingDurations.getHearingDuration(BENEFIT_CODE, ISSUE_CODE))
            .willReturn(new HearingDuration(
                BenefitCode.PIP_NEW_CLAIM,
                Issue.DD,
                DURATION_FACE_TO_FACE,
                DURATION_INTERPRETER,
                DURATION_PAPER
            ));
        given(hearingDurations.addExtraTimeIfNeeded(
            eq(DURATION_FACE_TO_FACE),
            eq(BenefitCode.PIP_NEW_CLAIM),
            eq(Issue.DD),
            any()
        ))
            .willReturn(DURATION_INTERPRETER);

        caseData.getAppeal().getHearingOptions().setWantsToAttend("Yes");
        caseData.getSchedulingAndListingFields().getOverrideFields().setDuration(overrideDuration);

        int result = HearingsDurationMapping.getHearingDuration(caseData, referenceDataServiceHolder);

        assertThat(result).isEqualTo(expectedResult);
    }


    @DisplayName("When wantsToAttend for the Appeal is no and the hearing type is paper "
        + "getHearingDurationBenefitIssueCodes return the correct paper durations")
    @Test
    void getHearingDurationBenefitIssueCodesPaper() {
        given(referenceDataServiceHolder.getHearingDurations()).willReturn(hearingDurations);
        given(hearingDurations.getHearingDuration(BENEFIT_CODE, ISSUE_CODE))
            .willReturn(new HearingDuration(
                BenefitCode.PIP_NEW_CLAIM,
                Issue.DD,
                DURATION_FACE_TO_FACE,
                DURATION_INTERPRETER,
                DURATION_PAPER
            ));

        caseData.getAppeal().getHearingOptions().setWantsToAttend("No");

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
        given(hearingDurations.getHearingDuration(BENEFIT_CODE, ISSUE_CODE))
            .willReturn(new HearingDuration(
                BenefitCode.PIP_NEW_CLAIM,
                Issue.DD,
                DURATION_FACE_TO_FACE,
                DURATION_INTERPRETER,
                DURATION_PAPER
            ));

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

        caseData.setOtherParties(otherParties);
        caseData.getAppeal().getHearingOptions().setWantsToAttend("No");

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
        given(referenceDataServiceHolder.getHearingDurations()).willReturn(hearingDurations);
        given(hearingDurations.getHearingDuration(BENEFIT_CODE, ISSUE_CODE))
            .willReturn(new HearingDuration(
                BenefitCode.PIP_NEW_CLAIM,
                Issue.DD,
                DURATION_FACE_TO_FACE,
                DURATION_INTERPRETER,
                DURATION_PAPER
            ));
        given(hearingDurations.addExtraTimeIfNeeded(
            eq(DURATION_FACE_TO_FACE),
            eq(BenefitCode.PIP_NEW_CLAIM),
            eq(Issue.DD),
            any()
        ))
            .willReturn(DURATION_FACE_TO_FACE);

        caseData.getAppeal().getHearingOptions().setWantsToAttend("Yes");

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
        given(referenceDataServiceHolder.getHearingDurations()).willReturn(hearingDurations);
        given(hearingDurations.getHearingDuration(BENEFIT_CODE, ISSUE_CODE))
            .willReturn(new HearingDuration(
                BenefitCode.PIP_NEW_CLAIM,
                Issue.DD,
                DURATION_FACE_TO_FACE,
                DURATION_INTERPRETER,
                DURATION_PAPER
            ));
        given(hearingDurations.addExtraTimeIfNeeded(
            eq(DURATION_INTERPRETER),
            eq(BenefitCode.PIP_NEW_CLAIM),
            eq(Issue.DD),
            any()
        ))
            .willReturn(DURATION_INTERPRETER);

        caseData.getAppeal().getHearingOptions().setWantsToAttend("Yes");
        caseData.getAppeal().getHearingOptions().setLanguageInterpreter("Yes");

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
        given(hearingDurations.getHearingDuration(BENEFIT_CODE, ISSUE_CODE))
            .willReturn(new HearingDuration(
                BenefitCode.PIP_NEW_CLAIM,
                Issue.DD,
                DURATION_FACE_TO_FACE,
                DURATION_INTERPRETER,
                DURATION_PAPER
            ));

        caseData.getAppeal().getHearingOptions().setWantsToAttend("No");
        caseData.setDwpIsOfficerAttending("Yes");

        Integer result = HearingsDurationMapping.getHearingDurationBenefitIssueCodes(
            caseData,
            referenceDataServiceHolder
        );

        assertThat(result).isEqualTo(DURATION_PAPER);
    }

    @DisplayName("When an invalid adjournCaseDuration and adjournCaseDurationUnits is given and overrideDuration "
        + "is present then override the duration of hearing")
    @Test
    void getHearingDurationWillReturnOverrideDurationWhenPresent() {

        caseData.getAppeal().getHearingOptions().setWantsToAttend("Yes");
        caseData.getSchedulingAndListingFields().getOverrideFields().setDuration(DURATION_FACE_TO_FACE);

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
    void getHearingDurationBenefitIssueCodesPaperWithNullSources(String benefitCode, String issueCode) {

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
