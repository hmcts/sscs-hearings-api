package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.CcdValue;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingSubtype;
import uk.gov.hmcts.reform.sscs.ccd.domain.JointParty;
import uk.gov.hmcts.reform.sscs.ccd.domain.OtherParty;
import uk.gov.hmcts.reform.sscs.ccd.domain.Representative;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.NO;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.YES;

class HearingsNumberAttendeesMappingTest {


    @DisplayName("When hearing not face to face, getNumberOfPhysicalAttendees returns zero")
    @Test
    void shouldGetNumberOfPhysicalAttendeesNotFaceToFace() {
        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend(NO)
                    .build())
                .build())
            .dwpIsOfficerAttending(NO)
            .build();

        int result = HearingsNumberAttendeesMapping.getNumberOfPhysicalAttendees(caseData);

        assertThat(result).isZero();
    }

    @DisplayName("When the appellant, an other party, and PO wants to Attend and a interpreter is required, getNumberOfPhysicalAttendees returns four")
    @Test
    void shouldGetNumberOfPhysicalAttendees() {
        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingSubtype(HearingSubtype.builder()
                    .wantsHearingTypeFaceToFace(YES)
                    .build())
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend(YES)
                    .languageInterpreter(YES)
                    .build())
                .build())
            .otherParties(List.of(
                CcdValue.<OtherParty>builder()
                    .value(OtherParty.builder()
                        .hearingOptions(HearingOptions.builder()
                            .wantsToAttend(YES)
                            .build())
                        .build())
                    .build()))
            .dwpIsOfficerAttending(YES)
            .build();

        int result = HearingsNumberAttendeesMapping.getNumberOfPhysicalAttendees(caseData);

        assertThat(result).isEqualTo(4);
    }

    @DisplayName("When the appellant wants to Attend, but no interpreter is needed and PO doesnt want to attend, getNumberOfPhysicalAttendees returns one")
    @Test
    void shouldGetNumberOfPhysicalAttendeesOnlyAppellant() {
        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingSubtype(HearingSubtype.builder()
                    .wantsHearingTypeFaceToFace(YES)
                    .build())
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend(YES)
                    .build())
                .build())
            .build();

        int result = HearingsNumberAttendeesMapping.getNumberOfPhysicalAttendees(caseData);

        assertThat(result).isEqualTo(1);
    }

    @DisplayName("When the PO wants to Attend a face to face hearing, getNumberOfPhysicalAttendees should include the PO")
    @Test
    void shouldGetNumberOfPhysicalAttendeesOnlPO() {
        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingSubtype(HearingSubtype.builder()
                    .wantsHearingTypeFaceToFace(YES)
                    .build())
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend(YES)
                    .build())
                .build())
            .dwpIsOfficerAttending(YES)
            .build();

        int result = HearingsNumberAttendeesMapping.getNumberOfPhysicalAttendees(caseData);

        assertThat(result).isEqualTo(2);
    }

    @DisplayName("When wantsToAttend is not Yes, getNumberOfAppellantAttendees returns zero")
    @ParameterizedTest
    @EnumSource(value = YesNo.class, names = {"NO"})
    @NullSource
    void testGetNumberOfAppellantAttendeesNoAttend(YesNo value) {
        Appeal appeal = Appeal.builder()
            .hearingOptions(HearingOptions.builder()
                .wantsToAttend(value)
                .build())
            .build();

        int result = HearingsNumberAttendeesMapping.getNumberOfAppellantAttendees(appeal, null);

        assertThat(result).isZero();
    }

    @DisplayName("When wantsToAttend, hasRepresentative and jointParty is Yes, getNumberOfAppellantAttendees returns three")
    @Test
    void testGetNumberOfAppellantAttendees() {

        Appeal appeal = Appeal.builder()
            .hearingOptions(HearingOptions.builder()
                .wantsToAttend(YES)
                .build())
            .rep(Representative.builder()
                .hasRepresentative(YES)
                .build())
            .build();

        JointParty jointParty = JointParty.builder()
            .hasJointParty(YES)
            .build();
        int result = HearingsNumberAttendeesMapping.getNumberOfAppellantAttendees(appeal, jointParty);

        assertThat(result).isEqualTo(3);
    }

    @DisplayName("When wantsToAttend is yes but hasRepresentative and jointParty is not Yes, getNumberOfAppellantAttendees returns one")
    @Test
    void testGetNumberOfAppellantAttendeesAppellantOnly() {

        Appeal appeal = Appeal.builder()
            .hearingOptions(HearingOptions.builder()
                .wantsToAttend(YES)
                .build())
            .rep(Representative.builder()
                .build())
            .build();

        JointParty jointParty = JointParty.builder()
            .hasJointParty(NO)
            .build();

        int result = HearingsNumberAttendeesMapping.getNumberOfAppellantAttendees(appeal, jointParty);

        assertThat(result).isEqualTo(1);
    }

    @DisplayName("When wantsToAttend and hasRepresentative is yes but and jointParty is not Yes, getNumberOfAppellantAttendees returns two")
    @Test
    void testGetNumberOfAppellantAttendeesAppellantRepOnly() {
        Appeal appeal = Appeal.builder()
            .hearingOptions(HearingOptions.builder()
                .wantsToAttend(YES)
                .build())
            .rep(Representative.builder()
                .hasRepresentative(YES)
                .build())
            .build();

        JointParty jointParty = JointParty.builder().build();

        int result = HearingsNumberAttendeesMapping.getNumberOfAppellantAttendees(appeal, jointParty);

        assertThat(result).isEqualTo(2);
    }

    @DisplayName("When wantsToAttend and jointParty is yes but and hasRepresentative is null, getNumberOfAppellantAttendees returns two")
    @Test
    void testGetNumberOfAppellantAttendeesAppellantJointOnly() {
        Appeal appeal = Appeal.builder()
            .hearingOptions(HearingOptions.builder()
                .wantsToAttend(YES)
                .build())
            .build();

        JointParty jointParty = JointParty.builder()
            .hasJointParty(YES)
            .build();

        int result = HearingsNumberAttendeesMapping.getNumberOfAppellantAttendees(appeal, jointParty);

        assertThat(result).isEqualTo(2);
    }

    @DisplayName("When otherParties are given, getNumberOfAppellantAttendees returns the count of those that want to attend")
    @Test
    void testGetNumberOfOtherPartyAttendees() {
        List<CcdValue<OtherParty>> otherParties = List.of(
            CcdValue.<OtherParty>builder()
                .value(OtherParty.builder()
                    .hearingOptions(HearingOptions.builder().wantsToAttend(YES).build())
                    .build())
                .build(),
            CcdValue.<OtherParty>builder()
                .value(OtherParty.builder()
                    .hearingOptions(HearingOptions.builder().wantsToAttend(YES).build())
                    .build())
                .build(),
            CcdValue.<OtherParty>builder()
                .value(OtherParty.builder()
                    .hearingOptions(HearingOptions.builder().wantsToAttend(NO).build())
                    .build())
                .build(),
            CcdValue.<OtherParty>builder()
                .value(OtherParty.builder()
                    .hearingOptions(HearingOptions.builder().wantsToAttend(null).build())
                    .build())
                .build());

        long result = HearingsNumberAttendeesMapping.getNumberOfOtherPartyAttendees(otherParties);

        assertThat(result).isEqualTo(2);
    }
}
