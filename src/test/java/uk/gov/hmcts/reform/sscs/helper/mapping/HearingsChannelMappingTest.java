package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.ccd.domain.AdjournCaseTypeOfHearing;
import uk.gov.hmcts.reform.sscs.ccd.domain.Adjournment;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.CcdValue;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingSubtype;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.OtherParty;
import uk.gov.hmcts.reform.sscs.ccd.domain.OverrideFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.NO;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.YES;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel.FACE_TO_FACE;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel.PAPER;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel.TELEPHONE;


@ExtendWith(MockitoExtension.class)
class HearingsChannelMappingTest {

    @Mock
    private ReferenceDataServiceHolder referenceDataServiceHolder;

    @DisplayName("When a override hearing channel is given getIndividualPreferredHearingChannel returns that hearing channel")
    @ParameterizedTest
    @EnumSource(value = HearingChannel.class)
    void testGetIndividualPreferredHearingChannel(HearingChannel value) throws Exception {
        HearingSubtype hearingSubtype = HearingSubtype.builder().build();
        HearingOptions hearingOptions = HearingOptions.builder().build();
        OverrideFields overrideFields = OverrideFields.builder()
            .appellantHearingChannel(value)
            .build();
        HearingChannel result = HearingsChannelMapping.getIndividualPreferredHearingChannel(hearingSubtype, hearingOptions, overrideFields);

        assertThat(result).isEqualTo(value);
    }

    @DisplayName("When a null appellant Hearing Channel is given getIndividualPreferredHearingChannel returns the valid hearing channel")
    @Test
    void testGetIndividualPreferredHearingChannel() {
        HearingSubtype hearingSubtype = HearingSubtype.builder()
            .wantsHearingTypeFaceToFace(YES.getValue())
            .build();
        HearingOptions hearingOptions = HearingOptions.builder()
            .wantsToAttend(YES.getValue())
            .build();
        OverrideFields overrideFields = OverrideFields.builder()
            .appellantHearingChannel(null)
            .build();
        HearingChannel result = HearingsChannelMapping.getIndividualPreferredHearingChannel(hearingSubtype, hearingOptions, overrideFields);

        assertThat(result).isEqualTo(FACE_TO_FACE);
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
        List<HearingChannel> result = HearingsChannelMapping.getHearingChannels(caseData);

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
                    .name(Name.builder()
                        .title("title")
                        .firstName("first")
                        .lastName("last")
                        .build())
                    .build())
                .build())
            .dwpIsOfficerAttending(NO.getValue())
            .build();

        List<HearingChannel> hearingChannels = HearingsChannelMapping.getHearingChannels(caseData);

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
                    .name(Name.builder()
                        .title("title")
                        .firstName("first")
                        .lastName("last")
                        .build())
                    .build())
                .build())
            .dwpIsOfficerAttending(NO.getValue())
            .build();

        List<HearingChannel> result = HearingsChannelMapping.getHearingChannels(caseData);

        assertThat(result)
            .hasSize(1)
            .containsOnly(PAPER);
    }

    @DisplayName("should return hmc reference")
    @Test
    void  getHearingChannels_hearingOptionsNotAttending() {

        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend(NO.getValue()).build())
                .appellant(Appellant.builder()
                    .name(Name.builder()
                        .title("title")
                        .firstName("first")
                        .lastName("last")
                        .build())
                    .build())
                .build())
            .dwpIsOfficerAttending(NO.getValue())
            .build();

        List<HearingChannel> result = HearingsChannelMapping.getHearingChannels(caseData);

        assertThat(result)
            .hasSize(1)
            .containsOnly(PAPER);
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

        boolean result = HearingsChannelMapping.isPaperCase(caseData);

        assertThat(result).isTrue();
    }

    @DisplayName("When someone wants to attend, isPaperCase returns False")
    @Test
    void testIsPaperCaseAttending() {
        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingSubtype(HearingSubtype.builder()
                    .wantsHearingTypeFaceToFace("Yes")
                    .build())
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend("Yes")
                    .build())
                .build())
            .build();

        boolean result = HearingsChannelMapping.isPaperCase(caseData);

        assertThat(result).isFalse();
    }

    @DisplayName("When adjournment flag is enabled and adjournment is in progress returns next hearing channel")
    @Test
    void getHearingChannels_ifAdjournmentEnabledInProgress_getNextHearing() {
        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend(NO.getValue()).build())
                    .appellant(Appellant.builder()
                        .name(Name.builder()
                             .title("title")
                             .firstName("first")
                             .lastName("last")
                        .build())
                    .build())
                .build())
            .dwpIsOfficerAttending(NO.getValue())
            .adjournment(Adjournment.builder().typeOfNextHearing(AdjournCaseTypeOfHearing.TELEPHONE)
                 .adjournmentInProgress(YES).build())
            .build();

        given(referenceDataServiceHolder.isAdjournmentFlagEnabled()).willReturn(true);

        List<HearingChannel> result = HearingsChannelMapping.getHearingChannels(caseData, referenceDataServiceHolder);
        assertThat(result)
            .hasSize(1)
            .containsOnly(TELEPHONE);
    }

    @DisplayName("When adjournment flag is enabled and adjournment is not in progress returns next hearing channel")
    @Test
    void getHearingChannels_ifAdjournmentEnabledAndNotProgress_getNextHearing() {
        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend(NO.getValue()).build())
                .appellant(Appellant.builder()
                    .name(Name.builder()
                    .title("title")
                    .firstName("first")
                    .lastName("last")
                    .build())
                .build())
            .build())
            .dwpIsOfficerAttending(NO.getValue())
            .adjournment(Adjournment.builder().typeOfNextHearing(AdjournCaseTypeOfHearing.TELEPHONE)
                .adjournmentInProgress(NO).build())
            .build();

        given(referenceDataServiceHolder.isAdjournmentFlagEnabled()).willReturn(true);

        List<HearingChannel> result = HearingsChannelMapping.getHearingChannels(caseData, referenceDataServiceHolder);
        assertThat(result)
            .hasSize(1)
            .containsOnly(PAPER);
    }

    @DisplayName("When adjournment flag is false, returns hearing chanel from the case")
    @Test
    void getHearingChannels_ifAdjournmentDisabled_returnDefaultHearingChannel() {
        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend(NO.getValue()).build())
                    .appellant(Appellant.builder()
                        .name(Name.builder()
                            .title("title")
                            .firstName("first")
                            .lastName("last").build())
                   .build())
                .build())
            .dwpIsOfficerAttending(NO.getValue())
            .adjournment(Adjournment.builder().typeOfNextHearing(AdjournCaseTypeOfHearing.TELEPHONE).build())
            .build();

        given(referenceDataServiceHolder.isAdjournmentFlagEnabled()).willReturn(false);

        List<HearingChannel> result = HearingsChannelMapping.getHearingChannels(caseData, referenceDataServiceHolder);
        assertThat(result)
            .hasSize(1)
            .containsOnly(PAPER);
    }

    @DisplayName("When adjournment flag is true and adjournment next hearing is null, returns hearing channel from the case")
    @Test
    void getHearingChannels_ifAdjournmentDisabledAndNextHearingIsNull_returnDefaultHearingChannel() {
        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend(NO.getValue()).build())
                .appellant(Appellant.builder()
                    .name(Name.builder()
                        .title("title")
                        .firstName("first")
                        .lastName("last")
                        .build())
                   .build())
                .build())
            .dwpIsOfficerAttending(NO.getValue())
            .adjournment(Adjournment.builder().typeOfNextHearing(null).build())
            .build();

        given(referenceDataServiceHolder.isAdjournmentFlagEnabled()).willReturn(false);

        List<HearingChannel> result = HearingsChannelMapping.getHearingChannels(caseData, referenceDataServiceHolder);
        assertThat(result)
            .hasSize(1)
            .containsOnly(PAPER);
    }

}
