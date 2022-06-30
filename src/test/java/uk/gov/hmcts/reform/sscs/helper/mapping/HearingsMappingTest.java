package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.Mock;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appointee;
import uk.gov.hmcts.reform.sscs.ccd.domain.BenefitCode;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseAccessManagementFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseManagementLocation;
import uk.gov.hmcts.reform.sscs.ccd.domain.CcdValue;
import uk.gov.hmcts.reform.sscs.ccd.domain.Entity;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingSubtype;
import uk.gov.hmcts.reform.sscs.ccd.domain.Issue;
import uk.gov.hmcts.reform.sscs.ccd.domain.JointParty;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.OtherParty;
import uk.gov.hmcts.reform.sscs.ccd.domain.Party;
import uk.gov.hmcts.reform.sscs.ccd.domain.Representative;
import uk.gov.hmcts.reform.sscs.ccd.domain.SessionCategory;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingRequestPayload;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingDuration;
import uk.gov.hmcts.reform.sscs.reference.data.model.SessionCategoryMap;
import uk.gov.hmcts.reform.sscs.reference.data.service.HearingDurationsService;
import uk.gov.hmcts.reform.sscs.reference.data.service.SessionCategoryMapService;
import uk.gov.hmcts.reform.sscs.service.VenueService;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.YES;

class HearingsMappingTest extends HearingsMappingBase {

    @Mock
    private HearingDurationsService hearingDurations;

    @Mock
    private SessionCategoryMapService sessionCategoryMaps;

    @Mock
    private ReferenceDataServiceHolder referenceDataServiceHolder;

    @Mock
    private VenueService venueService;

    @DisplayName("When a valid hearing wrapper is given buildHearingPayload returns the correct Hearing Request Payload")
    @Test
    void buildHearingPayload() throws InvalidMappingException, Exception {
        given(hearingDurations.getHearingDuration(BENEFIT_CODE,ISSUE_CODE))
                .willReturn(new HearingDuration(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                                                60,75,30));
        given(sessionCategoryMaps.getSessionCategory(BENEFIT_CODE,ISSUE_CODE,false,false))
                .willReturn(new SessionCategoryMap(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                                                   false, false, SessionCategory.CATEGORY_03, null));

        given(referenceDataServiceHolder.getHearingDurations()).willReturn(hearingDurations);
        given(referenceDataServiceHolder.getSessionCategoryMaps()).willReturn(sessionCategoryMaps);
        given(referenceDataServiceHolder.getVenueService()).willReturn(venueService);

        SscsCaseData caseData = SscsCaseData.builder()
                .ccdCaseId(String.valueOf(CASE_ID))
                .benefitCode(BENEFIT_CODE)
                .issueCode(ISSUE_CODE)
                .caseCreated(CASE_CREATED)
                .caseAccessManagementFields(CaseAccessManagementFields.builder()
                        .caseNameHmctsInternal(CASE_NAME_INTERNAL)
                        .caseNamePublic(CASE_NAME_PUBLIC)
                        .build())
                .appeal(Appeal.builder()
                        .rep(Representative.builder().hasRepresentative("no").build())
                        .hearingOptions(HearingOptions.builder().wantsToAttend("yes").build())
                        .hearingType("test")
                        .hearingSubtype(HearingSubtype.builder().wantsHearingTypeFaceToFace("yes").build())
                        .appellant(Appellant.builder()
                                .id("1")
                                .name(Name.builder()
                                        .title("title")
                                        .firstName("first")
                                        .lastName("last")
                                        .build())
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
        HearingRequestPayload result = HearingsMapping.buildHearingPayload(wrapper, referenceDataServiceHolder);

        assertThat(result).isNotNull();
        assertThat(result.getRequestDetails()).isNotNull();
        assertThat(result.getHearingDetails()).isNotNull();
        assertThat(result.getCaseDetails()).isNotNull();
        assertThat(result.getRequestDetails()).isNotNull();
    }

    @DisplayName("When entities are missing ids, update Ids adds missing ids")
    @Test
    void testUpdateIds() {
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
            .build();

        HearingsMapping.updateIds(wrapper);

        assertNotNull(wrapper.getCaseData().getAppeal().getAppellant().getId());
        assertNotNull(wrapper.getCaseData().getAppeal().getAppellant().getAppointee().getId());
        assertNotNull(wrapper.getCaseData().getAppeal().getRep().getId());

        assertNotNull(wrapper.getCaseData().getOtherParties().get(0).getValue().getId());
        assertNotNull(wrapper.getCaseData().getOtherParties().get(0).getValue().getAppointee().getId());
        assertNotNull(wrapper.getCaseData().getOtherParties().get(0).getValue().getRep().getId());

        assertNotNull(wrapper.getCaseData().getOtherParties().get(1).getValue().getId());

        assertThat(wrapper.getCaseData().getJointParty().getId()).isNull();
    }

    @DisplayName("When hasJointParty is No, blank or null, the joint party Id is not updated")
    @ParameterizedTest
    @EnumSource(value = YesNo.class, names = "NO")
    @NullSource
    void testUpdateIdsNoJointParty(YesNo value) {
        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                    .appellant(Appellant.builder().build())
                    .build())
            .jointParty(JointParty.builder()
                .hasJointParty(value)
                .build())
            .build();

        HearingWrapper wrapper = HearingWrapper.builder()
                .caseData(caseData)
                .build();

        HearingsMapping.updateIds(wrapper);

        assertThat(wrapper.getCaseData().getJointParty().getId()).isNull();
    }

    @DisplayName("When hasJointParty is No, blank or null, the joint party Id is not updated")
    @Test
    void testUpdateIdsHasJointParty() {
        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .appellant(Appellant.builder().build())
                .build())
            .jointParty(JointParty.builder()
                .hasJointParty(YES)
                .build())
            .build();

        HearingWrapper wrapper = HearingWrapper.builder()
            .caseData(caseData)
            .caseData(caseData)
            .build();

        HearingsMapping.updateIds(wrapper);

        assertThat(wrapper.getCaseData().getJointParty().getId()).isNotNull();
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
}
