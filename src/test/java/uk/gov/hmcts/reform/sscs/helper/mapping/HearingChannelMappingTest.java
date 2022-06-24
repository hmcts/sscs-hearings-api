package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.CcdValue;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingSubtype;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.OtherParty;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;
import uk.gov.hmcts.reform.sscs.exception.HearingChannelNotFoundException;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingChannelMapping.getHearingChannels;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingChannelMapping.getHearingChannelsHmcReference;

class HearingChannelMappingTest {

    @DisplayName("When DWP is attending then return Face to Face as preferred hearing type")
    @Test
    void getHearingChannels_whenDwpIsAttending_thenReturnFaceToFace_asPreferredHearingType() throws Exception {
        SscsCaseData caseData = SscsCaseData.builder()
            .dwpIsOfficerAttending(YesNo.YES.getValue())
            .appeal(Appeal.builder()
                .hearingType(HearingChannel.FACE_TO_FACE.getHmcReference())
                .build())
            .build();
        List<HearingChannel> result = getHearingChannels(caseData);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(HearingChannel.FACE_TO_FACE);
    }

    @DisplayName("The resolved hearing channel should follow the hierarchy face to face > video > telephone")
    @ParameterizedTest
    @MethodSource("hearingChannelArguments")
    void getHearingChannels_whenOneOfTheParties_containsFaceToFace_selectFaceToFace_asPreferredValue(String faceToFace,
                                                                                  String video, String telephone,
                                                                                  HearingChannel resolvedHearingChannel)
        throws Exception {

        List<CcdValue<OtherParty>> otherParties = new ArrayList<>();
        otherParties.add(new CcdValue<>(OtherParty.builder()
            .hearingOptions(
                HearingOptions.builder().wantsToAttend(YesNo.YES.getValue()).build())
            .hearingSubtype(HearingSubtype.builder()
                .wantsHearingTypeFaceToFace(faceToFace)
                .wantsHearingTypeVideo(video)
                .wantsHearingTypeTelephone(telephone)
                .hearingTelephoneNumber("1234")
                .hearingVideoEmail("email")
                .build())
            .id("2")
            .name(Name.builder()
                .title("title")
                .firstName("first")
                .lastName("last")
                .build())
            .build()));

        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder().wantsToAttend(YesNo.YES.getValue()).build())
                .hearingSubtype(HearingSubtype.builder()
                    .wantsHearingTypeFaceToFace(faceToFace)
                    .wantsHearingTypeVideo(video)
                    .wantsHearingTypeTelephone(telephone)
                    .hearingTelephoneNumber("1234")
                    .hearingVideoEmail("email")
                    .build())
                .appellant(Appellant.builder()
                    .id("1")
                    .name(Name.builder()
                        .title("title")
                        .firstName("first")
                        .lastName("last")
                        .build())
                    .build())
                .build())
            .dwpIsOfficerAttending(YesNo.NO.getValue())
            .otherParties(otherParties)
            .build();
        List<HearingChannel> result = getHearingChannels(caseData);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(resolvedHearingChannel);
    }

    private static Stream<Arguments> hearingChannelArguments() {
        return Stream.of(
            Arguments.of(YesNo.YES.getValue(), YesNo.NO.getValue(), YesNo.NO.getValue(), HearingChannel.FACE_TO_FACE),
            Arguments.of(YesNo.YES.getValue(), YesNo.YES.getValue(), YesNo.YES.getValue(), HearingChannel.FACE_TO_FACE),
            Arguments.of(YesNo.NO.getValue(), YesNo.YES.getValue(), YesNo.NO.getValue(), HearingChannel.VIDEO),
            Arguments.of(YesNo.NO.getValue(), YesNo.YES.getValue(), YesNo.YES.getValue(), HearingChannel.VIDEO),
            Arguments.of(YesNo.NO.getValue(), YesNo.NO.getValue(), YesNo.YES.getValue(), HearingChannel.TELEPHONE));
    }

    @DisplayName("should throw HearingChannelNotFoundException if no party has a preference selected but want to attend.")
    @Test
    void getHearingChannels_ifNoPartiesHaveAPreferenceSelected_throwException() {

        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder().wantsToAttend(YesNo.YES.getValue()).build())
                .appellant(Appellant.builder()
                    .id("1")
                    .name(Name.builder()
                        .title("title")
                        .firstName("first")
                        .lastName("last")
                        .build())
                    .build())
                .build())
            .dwpIsOfficerAttending(YesNo.NO.getValue())
            .build();

        assertThatExceptionOfType(HearingChannelNotFoundException.class).isThrownBy(() -> getHearingChannels(caseData))
            .withMessageContaining("Hearing Channel Not Found Exception");
    }

    @DisplayName("should return not attending if selected on the appeal")
    @Test
    void getHearingChannels_hearingOptionsNotAttending() throws HearingChannelNotFoundException {

        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder().wantsToAttend(YesNo.NO.getValue()).build())
                .appellant(Appellant.builder()
                    .id("1")
                    .name(Name.builder()
                        .title("title")
                        .firstName("first")
                        .lastName("last")
                        .build())
                    .build())
                .build())
            .dwpIsOfficerAttending(YesNo.NO.getValue())
            .build();

        List<HearingChannel> result = getHearingChannels(caseData);
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(HearingChannel.NOT_ATTENDING);

    }

    @DisplayName("should return hmc reference")
    @Test
    void  getHearingChannelsHmcReference_hearingOptionsNotAttending() throws HearingChannelNotFoundException {

        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder().wantsToAttend(YesNo.NO.getValue()).build())
                .appellant(Appellant.builder()
                    .id("1")
                    .name(Name.builder()
                        .title("title")
                        .firstName("first")
                        .lastName("last")
                        .build())
                    .build())
                .build())
            .dwpIsOfficerAttending(YesNo.NO.getValue())
            .build();

        List<String> result = getHearingChannelsHmcReference(caseData);
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(HearingChannel.NOT_ATTENDING.getHmcReference());
    }


}
