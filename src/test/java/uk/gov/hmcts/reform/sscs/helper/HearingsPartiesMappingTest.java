package uk.gov.hmcts.reform.sscs.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingSubtype;
import uk.gov.hmcts.reform.sscs.reference.data.mappings.HearingChannel;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HearingsPartiesMappingTest {

    private static final long HEARING_REQUEST_ID = 12345;
    private static final String HMC_STATUS = "TestStatus";
    private static final long VERSION = 1;
    private static final String CANCELLATION_REASON_CODE = "TestCancelCode";
    private static final long CASE_ID = 1625080769409918L;
    private static final long MISSING_CASE_ID = 99250807409918L;

    @DisplayName("When a valid hearing wrapper is given buildHearingPartiesDetails returns the correct Hearing Parties Details")
    @Test
    void buildHearingPartiesDetails() {
        // TODO Finish Test when method done
    }

    @DisplayName("When language passed in should return correct LOV format")
    @ParameterizedTest
    @CsvSource({"Acholi,ach-ach", "Afrikaans,afr-afr", "Akan,aka-aka", "Albanian,alb-alb", "Zaza,zza-zza", "Zulu,zul-zul"})
    void getIndividualInterpreterLanguageTest(String lang, String expected) {
        HearingOptions hearingOptions = HearingOptions.builder()
            .languageInterpreter("Yes")
            .languages(lang)
            .build();
        String result = HearingsPartiesMapping.getIndividualInterpreterLanguage(hearingOptions);
        assertThat(result).isEqualTo(expected);
    }

    @DisplayName("When sign language passed in should return correct LOV format")
    @ParameterizedTest
    @CsvSource({"American Sign Language (ASL),americanSignLanguage", "Hands on signing,handsOnSigning", "Deaf Relay,deafRelay", "Palantypist / Speech to text,palantypist"})
    void getIndividualInterpreterSignLanguageTest(String signLang, String expected) {
        List<String> arrangements = Collections.singletonList("signLanguageInterpreter");
        HearingOptions hearingOptions = HearingOptions.builder()
            .arrangements(arrangements)
            .signLanguageType(signLang)
            .build();
        hearingOptions.wantsSignLanguageInterpreter();
        String result = HearingsPartiesMapping.getIndividualInterpreterLanguage(hearingOptions);
        assertThat(result).isEqualTo(expected);
    }

    @DisplayName("When hearing type paper then return LOV not attending")
    @Test
    void getIndividualPreferredHearingChannelPaperTest() {
        HearingSubtype hearingSubtype = HearingSubtype.builder().build();
        String result = HearingsPartiesMapping.getIndividualPreferredHearingChannel("paper", hearingSubtype);
        assertThat(result).isEqualTo(HearingChannel.NOT_ATTENDING.getKey());
    }

    @DisplayName("When hearing type oral and video then return LOV not attending")
    @Test
    void getIndividualPreferredHearingChannelOralVideoTest() {
        HearingSubtype hearingSubtype = HearingSubtype.builder().wantsHearingTypeVideo("Yes").build();
        String result = HearingsPartiesMapping.getIndividualPreferredHearingChannel("oral", hearingSubtype);
        assertThat(result).isEqualTo(HearingChannel.VIDEO.getKey());
    }

    @DisplayName("When hearing type oral and telephone then return LOV not attending")
    @Test
    void getIndividualPreferredHearingChannelOralTelephoneTest() {
        HearingSubtype hearingSubtype = HearingSubtype.builder().wantsHearingTypeTelephone("Yes").build();
        String result = HearingsPartiesMapping.getIndividualPreferredHearingChannel("oral", hearingSubtype);
        assertThat(result).isEqualTo(HearingChannel.TELEPHONE.getKey());
    }

    @DisplayName("When hearing type oral and face to face then return LOV not attending")
    @Test
    void getIndividualPreferredHearingChannelOralFaceToFaceTest() {
        HearingSubtype hearingSubtype = HearingSubtype.builder().wantsHearingTypeFaceToFace("Yes").build();
        String result = HearingsPartiesMapping.getIndividualPreferredHearingChannel("oral", hearingSubtype);
        assertThat(result).isEqualTo(HearingChannel.FACE_TO_FACE.getKey());
    }

    @DisplayName("When hearing type is blank and face to face then return LOV not attending")
    @Test
    void getIndividualPreferredHearingChannelBlankFaceToFaceTest() {
        HearingSubtype hearingSubtype = HearingSubtype.builder().wantsHearingTypeFaceToFace("Yes").build();
        String result = HearingsPartiesMapping.getIndividualPreferredHearingChannel("", hearingSubtype);
        assertThat(result).isEqualTo(HearingChannel.FACE_TO_FACE.getKey());
    }
}
