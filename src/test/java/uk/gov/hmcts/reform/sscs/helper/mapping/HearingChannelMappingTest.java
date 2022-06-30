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
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.NO;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.YES;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingChannelMapping.getHearingChannels;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingChannelMapping.getHearingChannelsHmcReference;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel.FACE_TO_FACE;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel.PAPER;

class HearingChannelMappingTest {

    @DisplayName("When DWP is attending then return Face to Face as preferred hearing type")
    @Test
    void getHearingChannels_whenDwpIsAttending_thenReturnFaceToFace_asPreferredHearingType() throws Exception {
        SscsCaseData caseData = SscsCaseData.builder()
            .dwpIsOfficerAttending(YES.getValue())
            .appeal(Appeal.builder().build())
            .build();
        List<HearingChannel> result = getHearingChannels(caseData);

        assertThat(result)
            .hasSize(1)
            .containsOnly(FACE_TO_FACE);
    }

    @DisplayName("The resolved hearing channel should follow the hierarchy face to face > video > telephone")
    @ParameterizedTest
    @MethodSource("hearingChannelArguments")
    void getHearingChannels_whenOneOfTheParties_containsFaceToFace_selectFaceToFace_asPreferredValue(String faceToFace,
                                                                                  String video, String telephone,
                                                                                  HearingChannel resolvedHearingChannel) {

        List<CcdValue<OtherParty>> otherParties = new ArrayList<>();
        otherParties.add(new CcdValue<>(OtherParty.builder()
            .hearingOptions(
                HearingOptions.builder().wantsToAttend(YES.getValue()).build())
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
                .hearingOptions(HearingOptions.builder().wantsToAttend(YES.getValue()).build())
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
            .dwpIsOfficerAttending(NO.getValue())
            .otherParties(otherParties)
            .build();
        List<HearingChannel> result = getHearingChannels(caseData);

        assertThat(result)
            .hasSize(1)
            .containsOnly(resolvedHearingChannel);
    }

    private static Stream<Arguments> hearingChannelArguments() {
        return Stream.of(
            Arguments.of(YES.getValue(), NO.getValue(), NO.getValue(), FACE_TO_FACE),
            Arguments.of(YES.getValue(), YES.getValue(), YES.getValue(), FACE_TO_FACE),
            Arguments.of(NO.getValue(), YES.getValue(), NO.getValue(), HearingChannel.VIDEO),
            Arguments.of(NO.getValue(), YES.getValue(), YES.getValue(), HearingChannel.VIDEO),
            Arguments.of(NO.getValue(), NO.getValue(), YES.getValue(), HearingChannel.TELEPHONE));
    }

    @DisplayName("should throw HearingChannelNotFoundException if no party has a preference selected but want to attend.")
    @Test
    void getHearingChannels_ifNoPartiesHaveAPreferenceSelected_throwException() {

        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend(YES.getValue()).build())
                .appellant(Appellant.builder()
                    .id("1")
                    .name(Name.builder()
                        .title("title")
                        .firstName("first")
                        .lastName("last")
                        .build())
                    .build())
                .build())
            .dwpIsOfficerAttending(NO.getValue())
            .build();

        List<HearingChannel> hearingChannels = getHearingChannels(caseData);

        assertThat(hearingChannels)
            .hasSize(1)
            .containsOnly(PAPER);
    }

    @DisplayName("should return not attending if selected on the appeal")
    @Test
    void getHearingChannels_hearingOptionsPaper() {

        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend(NO.getValue()).build())
                .appellant(Appellant.builder()
                    .id("1")
                    .name(Name.builder()
                        .title("title")
                        .firstName("first")
                        .lastName("last")
                        .build())
                    .build())
                .build())
            .dwpIsOfficerAttending(NO.getValue())
            .build();

        List<HearingChannel> result = getHearingChannels(caseData);

        assertThat(result)
            .hasSize(1)
            .containsOnly(PAPER);
    }

    @DisplayName("should return hmc reference")
    @Test
    void  getHearingChannelsHmcReference_hearingOptionsNotAttending() {

        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend(NO.getValue()).build())
                .appellant(Appellant.builder()
                    .id("1")
                    .name(Name.builder()
                        .title("title")
                        .firstName("first")
                        .lastName("last")
                        .build())
                    .build())
                .build())
            .dwpIsOfficerAttending(NO.getValue())
            .build();

        List<String> result = getHearingChannelsHmcReference(caseData);

        assertThat(result)
            .hasSize(1)
            .containsOnly(PAPER.getHmcReference());
    }

    @DisplayName("When no one wants to attend, isPaperCase returns True")
    @Test
    void testIsPaperCaseNoOneAttend() {
        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend("No")
                    .build())
                .build())
            .build();

        boolean result = HearingChannelMapping.isPaperCase(caseData);

        assertThat(result).isTrue();
    }

    @DisplayName("When someone wants to attend, isPaperCase returns False")
    @Test
    void testIsPaperCaseAttending() {
        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend("No")
                    .build())
                .build())
            .dwpIsOfficerAttending("Yes")
            .build();

        boolean result = HearingChannelMapping.isPaperCase(caseData);

        assertThat(result).isFalse();
    }
}
