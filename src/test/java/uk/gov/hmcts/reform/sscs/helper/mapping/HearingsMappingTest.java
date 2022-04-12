package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingRequestPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class HearingsMappingTest extends HearingsMappingBase {

    @DisplayName("When a valid hearing wrapper is given buildHearingPayload returns the correct Hearing Request Payload")
    @Test
    void buildHearingPayload() {
        SscsCaseData caseData = SscsCaseData.builder()
                .ccdCaseId(String.valueOf(CASE_ID))
                .benefitCode(BENEFIT_CODE)
                .issueCode(ISSUE_CODE)
                .caseCreated(CASE_CREATED)
                .workAllocationFields(WorkAllocationFields.builder()
                        .caseNameHmctsInternal(CASE_NAME_INTERNAL)
                        .caseNamePublic(CASE_NAME_PUBLIC)
                        .build())
                .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder().build())
                        .appellant(Appellant.builder()
                                .id("1")
                                .name(Name.builder()
                                        .title("title")
                                        .firstName("first")
                                        .lastName("last")
                                        .build())
                                .relatedParties(new ArrayList<>())
                                .build())
                        .build())
                .caseManagementLocation(CaseManagementLocation.builder()
                        .baseLocation(EPIMS_ID)
                        .region(REGION)
                        .build())
                .build();
        HearingWrapper wrapper = HearingWrapper.builder()
                .caseData(caseData)
                .caseData(caseData)
                .build();
        HearingRequestPayload result = HearingsMapping.buildHearingPayload(wrapper);

        assertThat(result).isNotNull();
        assertThat(result.getRequestDetails()).isNotNull();
        assertThat(result.getHearingDetails()).isNotNull();
        assertThat(result.getCaseDetails()).isNotNull();
        assertThat(result.getRequestDetails()).isNotNull();
    }

    @DisplayName("updateIds Test")
    @Test
    void updateIds() {
        List<CcdValue<OtherParty>> otherParties = new ArrayList<>();
        otherParties.add(new CcdValue<>(OtherParty.builder()
                .id("2")
                .appointee(Appointee.builder().build())
                .rep(Representative.builder().build())
                .build()));
        otherParties.add(new CcdValue<>(OtherParty.builder().build()));
        SscsCaseData caseData = SscsCaseData.builder()
                .appeal(Appeal.builder()
                        .appellant(Appellant.builder().appointee(Appointee.builder().build()).build())
                        .rep(Representative.builder().build())
                        .build())
                .otherParties(otherParties)
                .build();
        HearingWrapper wrapper = HearingWrapper.builder()
                .caseData(caseData)
                .caseData(caseData)
                .build();

        HearingsMapping.updateIds(wrapper);

        assertNotNull(wrapper.getCaseData().getAppeal().getAppellant().getId());
        assertNotNull(wrapper.getCaseData().getAppeal().getAppellant().getAppointee().getId());
        assertNotNull(wrapper.getCaseData().getAppeal().getRep().getId());

        assertNotNull(wrapper.getCaseData().getOtherParties().get(0).getValue().getId());
        assertNotNull(wrapper.getCaseData().getOtherParties().get(0).getValue().getAppointee().getId());
        assertNotNull(wrapper.getCaseData().getOtherParties().get(0).getValue().getRep().getId());

        assertNotNull(wrapper.getCaseData().getOtherParties().get(1).getValue().getId());
    }

    @DisplayName("getMaxId Test")
    @Test
    void getMaxId() {
        Appellant appellant = Appellant.builder().id("1").appointee(Appointee.builder().id("6").build()).build();
        Representative rep = Representative.builder().id("3").build();
        List<CcdValue<OtherParty>> otherParties = new ArrayList<>();
        otherParties.add(new CcdValue<>(OtherParty.builder().id("2").build()));

        int result = HearingsMapping.getMaxId(otherParties, appellant, rep);

        assertEquals(6, result);
    }

    @DisplayName("getAllIds Test")
    @Test
    void getAllIds() {
        Appellant appellant = Appellant.builder().id("1").appointee(Appointee.builder().id("6").build()).build();
        Representative rep = Representative.builder().id("3").build();
        List<CcdValue<OtherParty>> otherParties = new ArrayList<>();
        otherParties.add(new CcdValue<>(OtherParty.builder().id("2").build()));

        List<Integer> result = HearingsMapping.getAllIds(otherParties, appellant, rep);
        List<Integer> expected = List.of(1,2,3,6);

        assertEquals(expected, result.stream().sorted().collect(Collectors.toList()));
    }

    @DisplayName("getAllPartyIds Test")
    @Test
    void getAllPartyIds() {
        Party party = Appellant.builder().id("1").appointee(Appointee.builder().id("2").build()).build();
        Representative rep = Representative.builder().id("3").build();

        List<Integer> result = HearingsMapping.getAllPartyIds(party, rep);
        List<Integer> expected = List.of(1,2,3);

        assertEquals(expected, result.stream().sorted().collect(Collectors.toList()));
    }

    @DisplayName("updateEntityId Parameterised Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "1,1,1,1",
        "2,3,2,3",
        "null,3,4,4",
        "null,16,17,17",
    }, nullValues = {"null"})
    void updateEntityId(String id, int maxId, String expectedId, int expectedMaxId) {
        Entity entity = Appellant.builder().id(id).build();

        int result = HearingsMapping.updateEntityId(entity, maxId);

        assertEquals(expectedId, entity.getId());
        assertEquals(expectedMaxId, result);
    }

    @DisplayName("buildRelatedParties Test")
    @Test
    void buildRelatedParties() {
        // TODO Finish Test when method done
        List<CcdValue<OtherParty>> otherParties = new ArrayList<>();
        otherParties.add(new CcdValue<>(OtherParty.builder().id("2").build()));
        SscsCaseData caseData = SscsCaseData.builder()
                .appeal(Appeal.builder()
                        .appellant(Appellant.builder().id("1").build())
                        .build())
                .otherParties(otherParties)
                .build();
        HearingWrapper wrapper = HearingWrapper.builder()
                .caseData(caseData)
                .caseData(caseData)
                .build();

        HearingsMapping.buildRelatedParties(wrapper);

        assertNotNull(wrapper.getCaseData().getAppeal().getAppellant().getRelatedParties());
        assertFalse(wrapper.getCaseData().getAppeal().getAppellant().getRelatedParties().isEmpty());

        assertNotNull(wrapper.getCaseData().getOtherParties().get(0).getValue().getRelatedParties());
        assertFalse(wrapper.getCaseData().getOtherParties().get(0).getValue().getRelatedParties().isEmpty());
    }

    @DisplayName("given otherParties is not null then getAllPartiesIds will return the correct list of ids")
    @Test
    void getAllPartiesIds() {
        List<CcdValue<OtherParty>> otherParties = new ArrayList<>();
        otherParties.add(CcdValue.<OtherParty>builder().value(OtherParty.builder().id("1").build()).build());
        otherParties.add(CcdValue.<OtherParty>builder().value(OtherParty.builder().id("5").build()).build());
        otherParties.add(CcdValue.<OtherParty>builder().value(OtherParty.builder().id("6").build()).build());
        otherParties.add(CcdValue.<OtherParty>builder().value(OtherParty.builder().id("7").build()).build());

        Appellant appellant = Appellant.builder().id("4").build();
        List<String> result = HearingsMapping.getAllPartiesIds(otherParties, appellant);
        assertThat(result).contains("4", "1", "5", "6", "7");
    }

    @DisplayName("given otherParties is null then getAllPartiesIds will return the correct list of ids")
    @Test
    void getAllPartiesIdsOtherPartiesNull() {
        Appellant appellant = Appellant.builder().id("4").build();
        List<String> result = HearingsMapping.getAllPartiesIds(null, appellant);
        assertThat(result).contains("4");
    }

    @DisplayName("buildRelatedPartiesParty when Appointee is not null Parameterised Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "Yes,true",
        "No,false",
        "null,false",
        ",false",
    }, nullValues = {"null"})
    void buildRelatedPartiesPartyAppointee(String isAppointee, boolean expected) {
        List<String> ids = List.of(new String[]{"1", "2", "3"});
        Appointee appointee = Appointee.builder().id("5").build();
        Party party = Appellant.builder().id("4").isAppointee(isAppointee).appointee(appointee).build();

        HearingsMapping.buildRelatedPartiesParty(party, ids, null);

        assertThat(party.getRelatedParties()).isNotNull().isNotEmpty();
        if (expected) {
            assertThat(appointee.getRelatedParties()).isNotNull().isNotEmpty();
        } else {
            assertThat(appointee.getRelatedParties()).isNull();
        }
    }

    @DisplayName("buildRelatedPartiesParty when representative is not null Parameterised Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "Yes,true",
        "No,false",
        "null,false",
        ",false",
    }, nullValues = {"null"})
    void buildRelatedPartiesPartyRepresentative(String hasRepresentative, boolean expected) {
        List<String> ids = List.of(new String[]{"1", "2", "3"});
        Party party = Appellant.builder().id("4").build();
        Representative rep =  Representative.builder().id("6").hasRepresentative(hasRepresentative).build();
        HearingsMapping.buildRelatedPartiesParty(party, ids, rep);

        assertThat(party.getRelatedParties()).isNotNull().isNotEmpty();
        if (expected) {
            assertThat(rep.getRelatedParties()).isNotNull().isNotEmpty();
        } else {
            assertThat(rep.getRelatedParties()).isNull();
        }
    }

    @DisplayName("buildRelatedPartiesParty when representative and appointee are null Parameterised Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "Yes",
        "No",
    }, nullValues = {"null"})
    void buildRelatedPartiesPartyAppointeeRepNull(String isAppointee) {
        List<String> ids = List.of(new String[]{"1", "2", "3"});
        Party party = Appellant.builder().id("4").isAppointee(isAppointee).build();
        HearingsMapping.buildRelatedPartiesParty(party, ids, null);

        assertThat(party.getRelatedParties()).isNotNull().isNotEmpty();
    }

    @DisplayName("updateEntityRelatedParties Test")
    @Test
    void updateEntityRelatedParties() {
        List<String> ids = List.of(new String[]{"1", "2", "3"});
        Appellant entity = Appellant.builder().build();
        HearingsMapping.updateEntityRelatedParties(entity, ids);
        assertNotNull(entity.getRelatedParties());
        assertFalse(entity.getRelatedParties().isEmpty());
    }

}
