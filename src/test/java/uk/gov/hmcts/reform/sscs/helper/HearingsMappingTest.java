package uk.gov.hmcts.reform.sscs.helper;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


class HearingsMappingTest {

    private static final long HEARING_REQUEST_ID = 12345;
    private static final String HMC_STATUS = "TestStatus";
    private static final long VERSION = 1;
    private static final String CANCELLATION_REASON_CODE = "TestCancelCode";
    private static final long CASE_ID = 1625080769409918L;
    private static final long MISSING_CASE_ID = 99250807409918L;
    private static final String ARRAY_SPLIT_REGEX = "\\s*\\|\\s*";

    @DisplayName("When a valid hearing wrapper is given buildHearingPayload returns the correct Hearing Request Payload")
    @Test
    void buildHearingPayload() {
        // TODO Finish Test when method done
    }

    @DisplayName("updateIds Test")
    @Test
    void updateIds() {
        // TODO Finish Test when method done
        List<CcdValue<OtherParty>> otherParties = new ArrayList<>();
        otherParties.add(new CcdValue<>(OtherParty.builder()
                .id("2")
                .appointee(Appointee.builder().build())
                .rep(Representative.builder().build())
                .build()));
        otherParties.add(new CcdValue<>(OtherParty.builder()
                .appointee(Appointee.builder().build())
                .rep(Representative.builder().build())
                .build()));
        SscsCaseData caseData = SscsCaseData.builder()
                .appeal(Appeal.builder()
                        .appellant(Appellant.builder().appointee(Appointee.builder().build()).build())
                        .rep(Representative.builder().build())
                        .build())
                .otherParties(otherParties)
                .build();
        HearingWrapper wrapper = HearingWrapper.builder()
                .originalCaseData(caseData)
                .updatedCaseData(caseData)
                .build();

        HearingsMapping.updateIds(wrapper);

        assertNotNull(wrapper.getUpdatedCaseData().getAppeal().getAppellant().getId());
        assertNotNull(wrapper.getUpdatedCaseData().getAppeal().getAppellant().getAppointee().getId());
        assertNotNull(wrapper.getUpdatedCaseData().getAppeal().getRep().getId());

        assertNotNull(wrapper.getUpdatedCaseData().getOtherParties().get(0).getValue().getId());
        assertNotNull(wrapper.getUpdatedCaseData().getOtherParties().get(0).getValue().getAppointee().getId());
        assertNotNull(wrapper.getUpdatedCaseData().getOtherParties().get(0).getValue().getRep().getId());

        assertNotNull(wrapper.getUpdatedCaseData().getOtherParties().get(1).getValue().getId());
        assertNotNull(wrapper.getUpdatedCaseData().getOtherParties().get(1).getValue().getAppointee().getId());
        assertNotNull(wrapper.getUpdatedCaseData().getOtherParties().get(1).getValue().getRep().getId());
    }

    @DisplayName("getMaxId Test")
    @Test
    void getMaxId() {
        // TODO Finish Test when method done
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
        // TODO Finish Test when method done
        Appellant appellant = Appellant.builder().id("1").appointee(Appointee.builder().id("6").build()).build();
        Representative rep = Representative.builder().id("3").build();
        List<CcdValue<OtherParty>> otherParties = new ArrayList<>();
        otherParties.add(new CcdValue<>(OtherParty.builder().id("2").build()));

        List<Integer> result = HearingsMapping.getAllIds(otherParties, appellant, rep);
        List<Integer> expected = Stream.of(new Integer[]{1,2,3,6}).sorted().collect(Collectors.toList());

        assertEquals(expected, result.stream().sorted().collect(Collectors.toList()));
    }

    @DisplayName("getAllPartyIds Test")
    @Test
    void getAllPartyIds() {
        // TODO Finish Test when method done
        Party party = Appellant.builder().id("1").appointee(Appointee.builder().id("2").build()).build();
        Representative rep = Representative.builder().id("3").build();

        List<Integer> result = HearingsMapping.getAllPartyIds(party, rep);
        List<Integer> expected = Stream.of(new Integer[]{1,2,3}).sorted().collect(Collectors.toList());

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
        // TODO Finish Test when method done
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
                .originalCaseData(caseData)
                .updatedCaseData(caseData)
                .build();

        HearingsMapping.buildRelatedParties(wrapper);

        assertNotNull(wrapper.getUpdatedCaseData().getAppeal().getAppellant().getRelatedParties());
        assertFalse(wrapper.getUpdatedCaseData().getAppeal().getAppellant().getRelatedParties().isEmpty());

        assertNotNull(wrapper.getUpdatedCaseData().getOtherParties().get(0).getValue().getRelatedParties());
        assertFalse(wrapper.getUpdatedCaseData().getOtherParties().get(0).getValue().getRelatedParties().isEmpty());
    }

    @DisplayName("getAllPartiesIds Parameterised Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "4,1,4|1",
        "3,5|4,3|5|4",
        "6,8|9|10,6|8|9|10",
        "2,null,2",
        "7,,7",
    }, nullValues = {"null"})
    void getAllPartiesIds(String appellantId, String otherPartiesIds, String expected) {
        // TODO Finish Test when method done
        List<CcdValue<OtherParty>> otherParties = null;
        if (nonNull(otherPartiesIds)) {
            otherParties = new ArrayList<>();
            for (String id : splitCsvParamArray(otherPartiesIds)) {
                otherParties.add(CcdValue.<OtherParty>builder().value(OtherParty.builder().id(id).build()).build());
            }
        }
        Appellant appellant = Appellant.builder().id(appellantId).build();
        List<String> result = HearingsMapping.getAllPartiesIds(otherParties, appellant);
        assertEquals(splitCsvParamArray(expected).stream().sorted().collect(Collectors.toList()),
                result.stream().sorted().collect(Collectors.toList()));
    }



    @DisplayName("buildRelatedPartiesParty Parameterised Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "true,true,true,true",
        "true,true,true,false",
        "true,true,false,true",
        "true,true,false,false",
        "true,false,true,true",
        "true,false,true,false",
        "true,false,false,true",
        "true,false,false,false",
        "false,false,true,true",
        "false,false,true,false",
        "false,false,false,true",
        "false,false,false,false",
    }, nullValues = {"null"})
    void buildRelatedPartiesParty(boolean hasAppointee, boolean hasRepresentative, boolean appointeeNull, boolean representativeNull) {
        // TODO Finish Test when method done
        List<String> ids = List.of(new String[]{"1", "2", "3"});
        Appointee appointee = appointeeNull ? null : Appointee.builder().id("5").build();
        Party party = Appellant.builder().appointee(appointee).id("4").build();
        Representative rep =  representativeNull ? null : Representative.builder().id("6").build();
        HearingsMapping.buildRelatedPartiesParty(party, ids, hasAppointee, hasRepresentative, rep);

        assertNotNull(party.getRelatedParties());
        assertFalse(party.getRelatedParties().isEmpty());

        if (!appointeeNull) {
            if (hasAppointee) {
                assertNotNull(appointee.getRelatedParties());
                assertFalse(appointee.getRelatedParties().isEmpty());
            } else {
                assertNull(appointee.getRelatedParties());
            }
        }

        if (!representativeNull) {
            if (hasRepresentative) {
                assertNotNull(rep.getRelatedParties());
                assertFalse(rep.getRelatedParties().isEmpty());
            } else {
                assertNull(rep.getRelatedParties());
            }
        }
    }

    @DisplayName("updateEntityRelatedParties Test")
    @Test
    void updateEntityRelatedParties() {
        // TODO Finish Test when method done
        List<String> ids = List.of(new String[]{"1", "2", "3"});
        Appellant entity = Appellant.builder().build();
        HearingsMapping.updateEntityRelatedParties(entity, ids);
        assertNotNull(entity.getRelatedParties());
        assertFalse(entity.getRelatedParties().isEmpty());
    }

    @NotNull
    List<String> splitCsvParamArray(String expected) {
        return List.of(expected.split(ARRAY_SPLIT_REGEX));
    }

}
