package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingSubtype;
import uk.gov.hmcts.reform.sscs.ccd.domain.OverrideFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.Postponement;
import uk.gov.hmcts.reform.sscs.ccd.domain.SchedulingAndListingFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingWindow;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.YES;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsWindowMapping.DAYS_TO_ADD_HEARING_WINDOW_DWP_RESPONDED;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsWindowMapping.DAYS_TO_ADD_HEARING_WINDOW_TODAY;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsWindowMapping.DAYS_TO_ADD_HEARING_WINDOW_TODAY_POSTPONEMENT;

class HearingsWindowMappingTest {

    @DisplayName("When a valid dwp Response Date is given, buildHearingWindow returns the date plus 28 days")
    @Test
    void testBuildHearingWindow() {
        SscsCaseData caseData = SscsCaseData.builder()
            .dwpResponseDate("2021-12-01")
            .build();

        HearingWindow result = HearingsWindowMapping.buildHearingWindow(caseData);

        assertThat(result)
            .isNotNull()
            .extracting("firstDateTimeMustBe", "dateRangeEnd")
            .containsOnlyNulls();

        assertThat(result.getDateRangeStart())
            .isEqualTo("2021-12-29");
    }

    @DisplayName("When a valid override Hearing Window given, buildHearingWindow returns the override Hearing Window")
    @ParameterizedTest
    @CsvSource(value = {
        "2022-01-01T12:00,2022-02-01,2022-03-01",
        "2022-01-01T12:00,2022-02-01,null",
        "2022-01-01T12:00,null,2022-03-01",
        "2022-01-01T12:00,null,null",
        "null,2022-02-01,2022-03-01",
        "null,2022-02-01,null",
        "null,null,2022-03-01",
    }, nullValues = "null")
    void testBuildHearingWindowOverride(LocalDateTime firstDateTimeMustBe, LocalDate dateRangeStart, LocalDate dateRangeEnd) {
        SscsCaseData caseData = SscsCaseData.builder()
            .dwpResponseDate("2021-12-01")
            .schedulingAndListingFields(SchedulingAndListingFields.builder()
                .overrideFields(OverrideFields.builder()
                    .hearingWindow(uk.gov.hmcts.reform.sscs.ccd.domain.HearingWindow.builder()
                        .firstDateTimeMustBe(firstDateTimeMustBe)
                        .dateRangeStart(dateRangeStart)
                        .dateRangeEnd(dateRangeEnd)
                        .build())
                    .build())
                .build())
            .build();

        HearingWindow result = HearingsWindowMapping.buildHearingWindow(caseData);

        assertThat(result.getFirstDateTimeMustBe()).isEqualTo(firstDateTimeMustBe);
        assertThat(result.getDateRangeStart()).isEqualTo(dateRangeStart);
        assertThat(result.getDateRangeEnd()).isEqualTo(dateRangeEnd);
    }

    @DisplayName("When a null override Hearing Window is given, buildHearingWindow returns the default value")
    @Test
    void testBuildHearingWindowOverride() {
        SscsCaseData caseData = SscsCaseData.builder()
            .dwpResponseDate("2021-12-01")
            .schedulingAndListingFields(SchedulingAndListingFields.builder()
                .overrideFields(OverrideFields.builder()
                    .hearingWindow(uk.gov.hmcts.reform.sscs.ccd.domain.HearingWindow.builder()
                        .firstDateTimeMustBe(null)
                        .dateRangeStart(null)
                        .dateRangeEnd(null)
                        .build())
                    .build())
                .build())
            .build();

        HearingWindow result = HearingsWindowMapping.buildHearingWindow(caseData);

        assertThat(result.getFirstDateTimeMustBe()).isNull();
        assertThat(result.getDateRangeStart()).isEqualTo("2021-12-29");
        assertThat(result.getDateRangeEnd()).isNull();
    }

    @DisplayName("When case with valid DWP_RESPOND event getHearingWindowStart returns a window starting within 28 days of the event's date")
    @ParameterizedTest
    @CsvSource(value = {
        "2021-12-01,YES,2021-12-02",
        "2021-12-01,NO,2021-12-29",
    }, nullValues = {"null"})
    void testGetHearingWindowStart(String dwpResponded, YesNo isUrgent, LocalDate expected) {
        SscsCaseData caseData = SscsCaseData.builder()
            .dwpResponseDate(dwpResponded)
            .urgentCase(isUrgent)
            .build();

        LocalDate result = HearingsWindowMapping.getHearingWindowStart(caseData);

        assertThat(result).isEqualTo(expected);
    }

    @DisplayName("When dwpResponseDate is blank, hearing date should be a day after current date")
    @Test
    void testBuildHearingWindowResponseBlank() {
        SscsCaseData caseData = SscsCaseData.builder().build();

        HearingWindow result = HearingsWindowMapping.buildHearingWindow(caseData);

        assertThat(result).isNotNull();

        LocalDate expected = LocalDate.now().plusDays(DAYS_TO_ADD_HEARING_WINDOW_TODAY);
        assertThat(result.getDateRangeStart()).isEqualTo(expected);

        assertThat(result.getFirstDateTimeMustBe()).isNull();
        assertThat(result.getDateRangeEnd()).isNull();

    }

    @DisplayName("When case has dwpResponseDate and not urgent returns start date of 28 days after dwpResponseDate")
    @Test
    void testBuildHearingWindowNotAutoListUrgent() {
        SscsCaseData caseData = SscsCaseData.builder()
            .dwpResponseDate(LocalDate.now().toString())
            .appeal(new Appeal(null, null, null, new HearingOptions(YES, null, null, null, null, null, null, null, null, null), null, null, null, null, new HearingSubtype(YES, "07444123456", null, null, null), null))
            .build();
        HearingWindow result = HearingsWindowMapping.buildHearingWindow(caseData);

        assertThat(result).isNotNull();

        LocalDate expected = LocalDate.now().plusDays(DAYS_TO_ADD_HEARING_WINDOW_DWP_RESPONDED);
        assertThat(result.getDateRangeStart()).isEqualTo(expected);

        assertThat(result.getFirstDateTimeMustBe()).isNull();
        assertThat(result.getDateRangeEnd()).isNull();
    }

    @DisplayName("When case is an urgent case, buildHearingWindow returns start date of 1 days after dwpResponseDate")
    @Test
    void testBuildHearingWindowNotAutoListIsUrgent() {
        SscsCaseData caseData = SscsCaseData.builder()
            .dwpResponseDate("2021-12-01")
            .urgentCase(YES)
            .build();
        HearingWindow result = HearingsWindowMapping.buildHearingWindow(caseData);

        assertThat(result).isNotNull();

        assertThat(result.getDateRangeStart()).isEqualTo("2021-12-02");

        assertThat(result.getFirstDateTimeMustBe()).isNull();
        assertThat(result.getDateRangeEnd()).isNull();
    }

    @DisplayName("When case' s hearing channel is paper and not urgent "
        + "it should return 28 days after dwp response date.")
    @Test
    void testBuildHearingWindowHearingChannelPaper() {
        SscsCaseData caseData = SscsCaseData.builder()
            .dwpResponseDate("2021-12-01")
            .appeal(Appeal.builder().build())
            .build();

        HearingWindow result = HearingsWindowMapping.buildHearingWindow(caseData);
        assertThat(result).isNotNull();
        assertThat(result.getDateRangeStart()).isEqualTo("2021-12-29");
        assertThat(result.getFirstDateTimeMustBe()).isNull();
        assertThat(result.getDateRangeEnd()).isNull();
    }


    @DisplayName("When .. is given getFirstDateTimeMustBe returns the valid LocalDateTime")
    @Test
    void testBetFirstDateTimeMustBe() {
        // TODO Finish Test when method done
        LocalDateTime result = HearingsWindowMapping.getFirstDateTimeMustBe();

        assertThat(result).isNull();
    }

    @DisplayName("When unprocessedPostponement is Yes getFirstDateTimeMustBe returns the valid LocalDateTime")
    @Test
    void testGetHearingWindowStartPostponement() {
        SscsCaseData caseData = SscsCaseData.builder()
            .postponement(Postponement.builder()
                .unprocessedPostponement(YES)
                .build())
            .build();

        LocalDate result = HearingsWindowMapping.getHearingWindowStart(caseData);

        LocalDate expected = LocalDate.now().plusDays(DAYS_TO_ADD_HEARING_WINDOW_TODAY_POSTPONEMENT);
        assertThat(result).isEqualTo(expected);
    }

    @DisplayName("When unprocessedPostponement is Yes isPostponementGranted returns true")
    @Test
    void testIsPostponementGrantedUnprocessed() {
        SscsCaseData caseData = SscsCaseData.builder()
            .postponement(Postponement.builder()
                .unprocessedPostponement(YES)
                .build())
            .build();

        boolean result = HearingsWindowMapping.isCasePostponed(caseData);

        assertThat(result).isTrue();
    }

    @DisplayName("When unprocessedPostponementRequest is not Yes isPostponementGranted returns false")
    @ParameterizedTest
    @EnumSource(
        value = YesNo.class,
        mode = EnumSource.Mode.INCLUDE,
        names = {"NO"})
    @NullSource
    void testIsPostponementGranted(YesNo value) {
        SscsCaseData caseData = SscsCaseData.builder()
            .postponement(Postponement.builder()
                .unprocessedPostponement(value)
                .build())
            .build();

        boolean result = HearingsWindowMapping.isCasePostponed(caseData);

        assertThat(result).isFalse();
    }
}
