package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.ccd.domain.AmendReason;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.BenefitCode;
import uk.gov.hmcts.reform.sscs.ccd.domain.CcdValue;
import uk.gov.hmcts.reform.sscs.ccd.domain.DynamicList;
import uk.gov.hmcts.reform.sscs.ccd.domain.DynamicListItem;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingInterpreter;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingSubtype;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingWindow;
import uk.gov.hmcts.reform.sscs.ccd.domain.Issue;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.OverrideFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.ReservedToMember;
import uk.gov.hmcts.reform.sscs.ccd.domain.SchedulingAndListingFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.SessionCategory;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingDuration;
import uk.gov.hmcts.reform.sscs.reference.data.model.Language;
import uk.gov.hmcts.reform.sscs.reference.data.model.SessionCategoryMap;
import uk.gov.hmcts.reform.sscs.reference.data.service.HearingDurationsService;
import uk.gov.hmcts.reform.sscs.reference.data.service.SessionCategoryMapService;
import uk.gov.hmcts.reform.sscs.reference.data.service.SignLanguagesService;
import uk.gov.hmcts.reform.sscs.reference.data.service.VerbalLanguagesService;
import uk.gov.hmcts.reform.sscs.service.VenueService;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.sscs.ccd.domain.AmendReason.ADMIN_ERROR;
import static uk.gov.hmcts.reform.sscs.ccd.domain.AmendReason.JUDGE_REQUEST;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.NO;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.YES;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.isYes;

@ExtendWith(MockitoExtension.class)
class OverridesMappingTest {

    public static final String BENEFIT_CODE = "002";
    public static final String ISSUE_CODE = "DD";

    private SscsCaseData caseData;
    private HearingWrapper wrapper;
    @Mock
    private HearingDurationsService hearingDurations;
    @Mock
    private VenueService venueService;
    @Mock
    private VerbalLanguagesService verbalLanguages;
    @Mock
    private SignLanguagesService signLanguages;
    @Mock

    private SessionCategoryMapService sessionCategoryMaps;
    @Mock
    private ReferenceDataServiceHolder referenceData;

    @BeforeEach
    void setUp() {
        caseData = SscsCaseData.builder()
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .appeal(Appeal.builder()
                .appellant(Appellant.builder()
                    .name(Name.builder()
                        .firstName("Appel")
                        .lastName("Lant")
                        .build())
                    .build())
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend("Yes")
                    .build())
                .hearingSubtype(HearingSubtype.builder()
                    .wantsHearingTypeFaceToFace("Yes")
                    .build())
                .build())
            .schedulingAndListingFields(SchedulingAndListingFields.builder()
                .defaultOverrideFields(OverrideFields.builder().build())
                .build())
            .build();

        wrapper = HearingWrapper.builder()
            .caseData(caseData)
            .build();
    }

    @DisplayName("When case data has override fields, getOverrideFields returns them")
    @Test
    void testGetOverrideFields() {
        OverrideFields overrideFields = OverrideFields.builder().build();
        caseData.getSchedulingAndListingFields().setOverrideFields(overrideFields);

        OverrideFields result = OverridesMapping.getOverrideFields(caseData);
        assertThat(result).isEqualTo(overrideFields);
    }

    @DisplayName("When override fields is null, getOverrideFields returns a empty override fields")
    @Test
    void testGetOverrideFieldsNull() {
        caseData.getSchedulingAndListingFields().setOverrideFields(null);

        OverrideFields result = OverridesMapping.getOverrideFields(caseData);
        assertThat(result)
            .isNotNull()
            .extracting(
                "duration",
                "reservedToJudge",
                "appellantInterpreter",
                "appellantHearingChannel",
                "hearingWindow",
                "autoList",
                "hearingVenueEpimsIds",
                "poToAttend")
            .containsOnlyNulls();
    }

    @DisplayName("When case data has a amend reason getAmendReasonCodes return a list with the reason")
    @ParameterizedTest
    @EnumSource(value = AmendReason.class)
    void testGetAmendReasonCodes(AmendReason value) {
        SscsCaseData caseData = SscsCaseData.builder()
            .schedulingAndListingFields(SchedulingAndListingFields.builder()
                .amendReasons(List.of(value))
                .build())
            .build();

        List<AmendReason> result = OverridesMapping.getAmendReasonCodes(caseData);

        assertThat(result)
            .hasSize(1)
            .containsExactlyInAnyOrder(value);
    }

    @DisplayName("When case data has multiple amend reasons getAmendReasonCodes return a list with those reasons")
    @Test
    void testGetAmendReasonCodes() {
        SscsCaseData caseData = SscsCaseData.builder()
            .schedulingAndListingFields(SchedulingAndListingFields.builder()
                .amendReasons(List.of(ADMIN_ERROR, JUDGE_REQUEST))
                .build())
            .build();

        List<AmendReason> result = OverridesMapping.getAmendReasonCodes(caseData);

        assertThat(result)
            .hasSize(2)
            .containsExactlyInAnyOrder(ADMIN_ERROR, JUDGE_REQUEST);
    }

    @DisplayName("When case data no amend reasons getAmendReasonCodes return an empty list")
    @Test
    void testGetAmendReasonCodesEmpty() {
        SscsCaseData caseData = SscsCaseData.builder()
            .schedulingAndListingFields(SchedulingAndListingFields.builder()
                .amendReasons(List.of())
                .build())
            .build();

        List<AmendReason> result = OverridesMapping.getAmendReasonCodes(caseData);

        assertThat(result).isEmpty();
    }

    @DisplayName("When amendReasons is null getAmendReasonCodes return an empty list")
    @Test
    void testGetAmendReasonCodesNull() {
        SscsCaseData caseData = SscsCaseData.builder()
            .schedulingAndListingFields(SchedulingAndListingFields.builder()
                .amendReasons(null)
                .build())
            .build();

        List<AmendReason> result = OverridesMapping.getAmendReasonCodes(caseData);

        assertThat(result).isEmpty();
    }

    @DisplayName("When an valid wrapper is given, getSchedulingAndListingFields returns a populated override fields")
    @Test
    void testSetDefaultOverrideFields() throws InvalidMappingException {
        caseData.getSchedulingAndListingFields().setDefaultOverrideFields(null);
        caseData.getAppeal().getHearingOptions().setLanguageInterpreter("Yes");
        caseData.getAppeal().getHearingOptions().setLanguages("French");

        given(hearingDurations.getHearingDuration(BENEFIT_CODE,ISSUE_CODE))
            .willReturn(new HearingDuration(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                60,75,30));

        given(venueService.getEpimsIdForVenue(caseData.getProcessingVenue())).willReturn("219164");

        given(verbalLanguages.getVerbalLanguage("French"))
            .willReturn(new Language("fre","Test",null,null, List.of()));


        given(referenceData.getHearingDurations()).willReturn(hearingDurations);
        given(referenceData.getVenueService()).willReturn(venueService);
        given(referenceData.getVerbalLanguages()).willReturn(verbalLanguages);

        OverridesMapping.setDefaultOverrideFields(wrapper, referenceData);
        OverrideFields result = caseData.getSchedulingAndListingFields().getDefaultOverrideFields();


        assertThat(result).isNotNull();
        assertThat(result.getDuration()).isNotNull();
        assertThat(result.getReservedToJudge()).isNotNull();
        assertThat(result.getAppellantInterpreter()).isNotNull();
        assertThat(result.getAppellantHearingChannel()).isNotNull();
        assertThat(result.getHearingWindow()).isNotNull();
        assertThat(result.getAutoList()).isNotNull();
        assertThat(result.getHearingVenueEpimsIds()).isNotEmpty();
        assertThat(result.getPoToAttend()).isNotNull();
        assertThat(result.getAppellantHearingChannel()).isEqualTo(HearingChannel.FACE_TO_FACE);
    }

    @DisplayName("When case data is given with reserved to judge, getReservedToJudge returns the correct Reserved To Member")
    @ParameterizedTest
    @ValueSource(strings = {"Yes", "No"})
    @NullAndEmptySource
    void testGetReservedToJudge(String value) {
        caseData.setReservedToJudge(value);

        ReservedToMember result = OverridesMapping.getReservedToJudge(caseData);

        YesNo expected = isYes(value) ? YES : NO;

        assertThat(result).isNotNull();
        assertThat(result.getIsReservedToMember()).isEqualTo(expected);
    }

    @DisplayName("When the appellant wants a language interpreter, getAppellantInterpreter returns "
        + "the Hearing Interpreter with IsInterpreterWanted set to Yes and the correct default language reference in a DynamicList")
    @Test
    void testGetAppellantInterpreter() throws InvalidMappingException {
        Appeal appeal = Appeal.builder()
            .hearingOptions(HearingOptions.builder()
                .languageInterpreter("Yes")
                .languages("French")
                .build())
            .build();

        Language language = new Language("fre", "Test", null, null, List.of());
        given(verbalLanguages.getVerbalLanguage("French"))
            .willReturn(language);

        given(referenceData.getVerbalLanguages()).willReturn(verbalLanguages);

        HearingInterpreter result = OverridesMapping.getAppellantInterpreter(appeal, referenceData);

        assertThat(result).isNotNull();
        assertThat(result.getIsInterpreterWanted()).isEqualTo(YES);
        assertThat(result.getInterpreterLanguage())
            .extracting(DynamicList::getValue)
            .extracting(DynamicListItem::getCode)
            .isEqualTo(language.getReference());
        assertThat(result.getInterpreterLanguage())
            .extracting(DynamicList::getValue)
            .extracting(DynamicListItem::getLabel)
            .isEqualTo("Test");
    }

    @DisplayName("When the appellant wants a language interpreter with a dialect, getAppellantInterpreter returns "
        + "the Hearing Interpreter with IsInterpreterWanted set to Yes and the correct default language reference in a DynamicList")
    @Test
    void testGetAppellantInterpreterDialect() throws InvalidMappingException {
        Appeal appeal = Appeal.builder()
            .hearingOptions(HearingOptions.builder()
                .languageInterpreter("Yes")
                .languages("French")
                .build())
            .build();

        Language language = new Language("fre", "Test", "test", "Test Dialect", List.of());
        given(verbalLanguages.getVerbalLanguage("French"))
            .willReturn(language);

        given(referenceData.getVerbalLanguages()).willReturn(verbalLanguages);

        HearingInterpreter result = OverridesMapping.getAppellantInterpreter(appeal, referenceData);

        assertThat(result).isNotNull();
        assertThat(result.getIsInterpreterWanted()).isEqualTo(YES);
        assertThat(result.getInterpreterLanguage())
            .extracting(DynamicList::getValue)
            .extracting(DynamicListItem::getCode)
            .isEqualTo("fre-test");
        assertThat(result.getInterpreterLanguage())
            .extracting(DynamicList::getValue)
            .extracting(DynamicListItem::getLabel)
            .isEqualTo("Test Dialect");
    }

    @DisplayName("When the appellant wants a language interpreter with a dialect, getAppellantInterpreter returns "
        + "the Hearing Interpreter with IsInterpreterWanted set to Yes and the correct default language reference in a DynamicList")
    @Test
    void testGetAppellantInterpreterDialectInvalidLanguage()  {
        Appeal appeal = Appeal.builder()
            .hearingOptions(HearingOptions.builder()
                .languageInterpreter("Yes")
                .languages("Bad Language")
                .build())
            .build();

        given(verbalLanguages.getVerbalLanguage("Bad Language"))
            .willReturn(null);

        given(referenceData.getVerbalLanguages()).willReturn(verbalLanguages);

        assertThatExceptionOfType(InvalidMappingException.class).isThrownBy(() -> OverridesMapping.getAppellantInterpreter(appeal, referenceData));
    }

    @DisplayName("When the appellant wants a language interpreter, getAppellantInterpreter returns "
        + "the Hearing Interpreter with IsInterpreterWanted set to Yes and the correct default language reference in a DynamicList")
    @Test
    void testGetAppellantInterpreterSignLanguage() throws InvalidMappingException {
        Appeal appeal = Appeal.builder()
            .hearingOptions(HearingOptions.builder()
                .arrangements(List.of("signLanguageInterpreter"))
                .signLanguageType("Makaton")
                .build())
            .build();

        Language language = new Language("sign-mkn", "Makaton", null, null, List.of());
        given(signLanguages.getSignLanguage("Makaton"))
            .willReturn(language);

        given(referenceData.getSignLanguages()).willReturn(signLanguages);

        HearingInterpreter result = OverridesMapping.getAppellantInterpreter(appeal, referenceData);

        assertThat(result).isNotNull();
        assertThat(result.getIsInterpreterWanted()).isEqualTo(YES);
        assertThat(result.getInterpreterLanguage())
            .extracting(DynamicList::getValue)
            .extracting(DynamicListItem::getCode)
            .isEqualTo("sign-mkn");
        assertThat(result.getInterpreterLanguage())
            .extracting(DynamicList::getValue)
            .extracting(DynamicListItem::getLabel)
            .isEqualTo("Makaton");
    }

    @DisplayName("When the appellant wants a language interpreter, getAppellantInterpreter returns "
        + "the Hearing Interpreter with IsInterpreterWanted set to Yes and the correct default language reference in a DynamicList")
    @Test
    void testGetAppellantInterpreterInvalidSignLanguage() {
        Appeal appeal = Appeal.builder()
            .hearingOptions(HearingOptions.builder()
                .arrangements(List.of("signLanguageInterpreter"))
                .signLanguageType("Bad Sign Language")
                .build())
            .build();

        given(signLanguages.getSignLanguage("Bad Sign Language"))
            .willReturn(null);

        given(referenceData.getSignLanguages()).willReturn(signLanguages);

        assertThatExceptionOfType(InvalidMappingException.class).isThrownBy(() -> OverridesMapping.getAppellantInterpreter(appeal, referenceData));
    }

    @DisplayName("When the appellant doesn't want a language interpreter, getAppellantInterpreter returns the Hearing Interpreter with IsInterpreterWanted set to No")
    @Test
    void testGetAppellantInterpreterNotWanted() throws InvalidMappingException {
        Appeal appeal = Appeal.builder()
            .hearingOptions(HearingOptions.builder()
                .languageInterpreter("No")
                .languages("French")
                .build())
            .build();

        HearingInterpreter result = OverridesMapping.getAppellantInterpreter(appeal, referenceData);

        assertThat(result).isNotNull();
        assertThat(result.getIsInterpreterWanted()).isEqualTo(NO);
        assertThat(result.getInterpreterLanguage()).isNull();
    }

    @DisplayName("When language interpreter is Yes and arrangements contains signLanguageInterpreter, getInterpreterWanted returns Yes")
    @Test
    void testGetInterpreter() {
        HearingOptions hearingOptions = HearingOptions.builder()
            .languageInterpreter("Yes")
            .arrangements(List.of("signLanguageInterpreter"))
            .build();

        YesNo result = OverridesMapping.getInterpreterWanted(hearingOptions);

        assertThat(result).isEqualTo(YES);
    }

    @DisplayName("When language interpreter is Yes and arrangements doesn't contain signLanguageInterpreter, getInterpreterWanted returns Yes")
    @Test
    void testGetInterpreterLanguageYes() {
        HearingOptions hearingOptions = HearingOptions.builder()
            .languageInterpreter("Yes")
            .arrangements(List.of())
            .build();

        YesNo result = OverridesMapping.getInterpreterWanted(hearingOptions);

        assertThat(result).isEqualTo(YES);
    }

    @DisplayName("When language interpreter is No and arrangements contains signLanguageInterpreter, getInterpreterWanted returns Yes")
    @Test
    void testGetInterpreterSignLanguage() {
        HearingOptions hearingOptions = HearingOptions.builder()
            .languageInterpreter("No")
            .arrangements(List.of("signLanguageInterpreter"))
            .build();

        YesNo result = OverridesMapping.getInterpreterWanted(hearingOptions);

        assertThat(result).isEqualTo(YES);
    }

    @DisplayName("When language interpreter is null and arrangements doesn't contain signLanguageInterpreter, getInterpreterWanted returns No")
    @Test
    void testGetInterpreterNull() {
        HearingOptions hearingOptions = HearingOptions.builder()
            .languageInterpreter(null)
            .arrangements(null)
            .build();

        YesNo result = OverridesMapping.getInterpreterWanted(hearingOptions);

        assertThat(result).isEqualTo(NO);
    }

    @DisplayName("When the appellant wants a language interpreter, getInterpreterLanguage returns the correct default language")
    @Test
    void testGetInterpreterLanguage() throws InvalidMappingException {
        HearingOptions hearingOptions = HearingOptions.builder()
            .languageInterpreter("Yes")
            .languages("French")
            .build();

        Language language = new Language("fre", "Test", null, null, List.of());
        given(verbalLanguages.getVerbalLanguage("French"))
            .willReturn(language);

        given(referenceData.getVerbalLanguages()).willReturn(verbalLanguages);

        Language result = OverridesMapping.getInterpreterLanguage(hearingOptions, referenceData);

        assertThat(result).isEqualTo(language);
    }

    @DisplayName("When the appellant doesn't want a language interpreter, getInterpreterLanguage returns null")
    @ParameterizedTest
    @ValueSource(strings = {"No"})
    @NullAndEmptySource
    void testGetInterpreterLanguage(String value) throws InvalidMappingException {
        HearingOptions hearingOptions = HearingOptions.builder()
            .languageInterpreter(value)
            .languages("French")
            .build();

        Language result = OverridesMapping.getInterpreterLanguage(hearingOptions, referenceData);

        assertThat(result).isNull();
    }

    @DisplayName("When the appellant doesn't want a language interpreter, getInterpreterLanguage returns null")
    @Test
    void testGetInterpreterLanguageNullHearingOptions() throws InvalidMappingException {
        Language result = OverridesMapping.getInterpreterLanguage(null, referenceData);

        assertThat(result).isNull();
    }

    @DisplayName("When valid case data is given, getHearingDetailsHearingWindow returns the correct default start date and null FirstDateTimeMustBe & DateRangeEnd")
    @Test
    void testGetHearingDetailsHearingWindow() {
        caseData.setDwpResponseDate("2021-12-01");

        HearingWindow result = OverridesMapping.getHearingDetailsHearingWindow(caseData);

        assertThat(result).isNotNull();
        assertThat(result.getFirstDateTimeMustBe()).isNull();
        assertThat(result.getDateRangeStart()).isEqualTo("2021-12-29");
        assertThat(result.getDateRangeEnd()).isNull();
    }

    @DisplayName("When valid case data is given, getHearingDetailsHearingWindow returns the correct default auto list value")
    @ParameterizedTest
    @ValueSource(strings = {"Comment"})
    @NullAndEmptySource
    void testGetHearingDetailsAutoList(String value) {
        caseData.setDwpResponseDate("2021-12-01");
        caseData.getAppeal().getHearingOptions().setOther(value);

        given(sessionCategoryMaps.getSessionCategory(BENEFIT_CODE, ISSUE_CODE,false,false))
            .willReturn(new SessionCategoryMap(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                false, false, SessionCategory.CATEGORY_01, null));

        given(referenceData.getSessionCategoryMaps()).willReturn(sessionCategoryMaps);

        YesNo result = OverridesMapping.getHearingDetailsAutoList(caseData, referenceData);

        YesNo expected = isBlank(value) ? YES : NO;

        assertThat(result)
            .isNotNull()
            .isEqualTo(expected);
    }

    @DisplayName("When valid case data is given, getHearingDetailsHearingWindow returns the default venue epims ids")
    @Test
    void testGetHearingDetailsLocations() {
        given(venueService.getEpimsIdForVenue(caseData.getProcessingVenue())).willReturn("219164");

        given(referenceData.getVenueService()).willReturn(venueService);

        List<CcdValue<CcdValue<String>>> result = OverridesMapping.getHearingDetailsLocations(caseData, referenceData);

        assertThat(result)
            .hasSize(1)
            .extracting(CcdValue::getValue)
            .extracting(CcdValue::getValue)
            .containsExactlyInAnyOrder("219164");
    }

    @DisplayName("When valid case data is given, getHearingDetailsHearingWindow returns the default Po to attend value")
    @ParameterizedTest
    @ValueSource(strings = {"Yes", "No"})
    @NullAndEmptySource
    void testGetPoToAttend(String value) {
        caseData.setDwpIsOfficerAttending(value);

        YesNo result = OverridesMapping.getPoToAttend(caseData);

        YesNo expected = isYes(value) ? YES : NO;

        assertThat(result)
            .isNotNull()
            .isEqualTo(expected);
    }
}
