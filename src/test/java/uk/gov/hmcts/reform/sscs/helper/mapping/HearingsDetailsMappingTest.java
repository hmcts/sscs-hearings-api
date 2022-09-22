package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appointee;
import uk.gov.hmcts.reform.sscs.ccd.domain.BenefitCode;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseLink;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseLinkDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseManagementLocation;
import uk.gov.hmcts.reform.sscs.ccd.domain.CcdValue;
import uk.gov.hmcts.reform.sscs.ccd.domain.DynamicList;
import uk.gov.hmcts.reform.sscs.ccd.domain.DynamicListItem;
import uk.gov.hmcts.reform.sscs.ccd.domain.Hearing;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingSubtype;
import uk.gov.hmcts.reform.sscs.ccd.domain.Issue;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.OtherParty;
import uk.gov.hmcts.reform.sscs.ccd.domain.OverrideFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.Party;
import uk.gov.hmcts.reform.sscs.ccd.domain.RegionalProcessingCenter;
import uk.gov.hmcts.reform.sscs.ccd.domain.Role;
import uk.gov.hmcts.reform.sscs.ccd.domain.SchedulingAndListingFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.SessionCategory;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;
import uk.gov.hmcts.reform.sscs.model.HearingLocation;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.HearingType;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDetails;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingDuration;
import uk.gov.hmcts.reform.sscs.reference.data.model.SessionCategoryMap;
import uk.gov.hmcts.reform.sscs.reference.data.service.HearingDurationsService;
import uk.gov.hmcts.reform.sscs.reference.data.service.SessionCategoryMapService;
import uk.gov.hmcts.reform.sscs.service.VenueService;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.YES;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HearingType.SUBSTANTIVE;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.LocationType.COURT;

class HearingsDetailsMappingTest extends HearingsMappingBase {

    @Mock
    private HearingDurationsService hearingDurations;

    @Mock
    private SessionCategoryMapService sessionCategoryMaps;

    @Mock
    private ReferenceDataServiceHolder referenceDataServiceHolder;

    @Mock
    private VenueService venueService;

    public static final String PROCESSING_VENUE_1 = "test_place";
    private static final String PHONE_NUMBER = "07483871426";
    private static final String HEARING_VENUE_ID_1 = "219164";
    private static final String HEARING_VENUE_ID_2 = "436578";

    private String epimsId1;
    private String epimsId2;
    private String epimsId3;
    private String epimsId4;
    private List<VenueDetails> epimsIdsList;

    @BeforeEach
    void setUp() {
        epimsId1 = "112233";
        epimsId2 = "332211";
        epimsId3 = "123123";
        epimsId4 = "321321";

        epimsIdsList = Arrays.asList(
            VenueDetails.builder().epimsId(epimsId1).build(),
            VenueDetails.builder().epimsId(epimsId2).build(),
            VenueDetails.builder().epimsId(epimsId3).build(),
            VenueDetails.builder().epimsId(epimsId4).build());
    }

    @DisplayName("When a valid hearing wrapper is given buildHearingDetails returns the correct Hearing Details")
    @Test
    void buildHearingDetails() {
        given(hearingDurations.getHearingDuration(BENEFIT_CODE, ISSUE_CODE))
            .willReturn(new HearingDuration(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                                            60, 75, 30
            ));
        given(sessionCategoryMaps.getSessionCategory(BENEFIT_CODE, ISSUE_CODE, false, false))
            .willReturn(new SessionCategoryMap(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                                               false, false, SessionCategory.CATEGORY_03, null
            ));

        given(referenceDataServiceHolder.getHearingDurations()).willReturn(hearingDurations);
        given(referenceDataServiceHolder.getSessionCategoryMaps()).willReturn(sessionCategoryMaps);
        given(referenceDataServiceHolder.getVenueService()).willReturn(venueService);

        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder()
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .appeal(Appeal.builder()
                .hearingSubtype(HearingSubtype.builder()
                    .wantsHearingTypeFaceToFace("Yes")
                    .build())
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend("Yes")
                    .build())
                .build())
            .caseManagementLocation(CaseManagementLocation.builder()
                                        .baseLocation(EPIMS_ID)
                                        .region(REGION)
                                        .build())
            .dwpIsOfficerAttending("Yes")
            .build();

        HearingWrapper wrapper = HearingWrapper.builder()
            .caseData(caseData)
            .build();

        HearingDetails hearingDetails = HearingsDetailsMapping.buildHearingDetails(wrapper, referenceDataServiceHolder);

        assertNotNull(hearingDetails.getHearingType());
        assertNotNull(hearingDetails.getHearingWindow());
        assertNotNull(hearingDetails.getDuration());
        assertNotNull(hearingDetails.getHearingPriorityType());
        assertEquals(2, hearingDetails.getNumberOfPhysicalAttendees());
        assertNotNull(hearingDetails.getHearingLocations());
        assertNull(hearingDetails.getListingComments());
        assertNull(hearingDetails.getHearingRequester());
        assertNull(hearingDetails.getLeadJudgeContractType());
        assertNotNull(hearingDetails.getPanelRequirements());
    }

    @DisplayName("shouldBeHearingsInWelshFlag Test")
    @Test
    void shouldBeHearingsInWelshFlag() {
        boolean result = HearingsDetailsMapping.shouldBeHearingsInWelshFlag();

        assertFalse(result);
    }

    @DisplayName("When case has a linked case isCaseLinked returns true")
    @Test
    void testIsCaseLinked() {
        SscsCaseData caseData = SscsCaseData.builder()
            .linkedCase(List.of(CaseLink.builder()
                .value(CaseLinkDetails.builder()
                    .caseReference("123456")
                    .build())
                .build()))
            .build();
        boolean result = HearingsDetailsMapping.isCaseLinked(caseData);

        assertThat(result).isTrue();
    }

    @DisplayName("When case has multiple linked cases isCaseLinked returns true")
    @Test
    void testIsCaseLinkedMultiple() {
        SscsCaseData caseData = SscsCaseData.builder()
            .linkedCase(List.of(
                CaseLink.builder()
                .value(CaseLinkDetails.builder()
                .caseReference("123456")
                .build())
                .build(),
                CaseLink.builder()
                .value(CaseLinkDetails.builder()
                .caseReference("654321")
                .build())
                .build()
            ))
            .build();
        boolean result = HearingsDetailsMapping.isCaseLinked(caseData);

        assertThat(result).isTrue();
    }

    @DisplayName("When case has empty linkedCase isCaseLinked returns true")
    @Test
    void testIsCaseLinkedEmpty() {
        SscsCaseData caseData = SscsCaseData.builder()
            .linkedCase(Collections.emptyList())
            .build();
        boolean result = HearingsDetailsMapping.isCaseLinked(caseData);

        assertThat(result).isFalse();
    }

    @DisplayName("When case has null linkedCase isCaseLinked returns true")
    @Test
    void testIsCaseLinkedNull() {
        SscsCaseData caseData = SscsCaseData.builder()
            .build();
        boolean result = HearingsDetailsMapping.isCaseLinked(caseData);

        assertThat(result).isFalse();
    }

    @DisplayName("Hearing type should be substantive.")
    @Test
    void getHearingType() {
        HearingType result = HearingsDetailsMapping.getHearingType();

        assertThat(result).isEqualTo(SUBSTANTIVE);
    }

    @DisplayName("When urgentCase is yes, isCaseUrgent return True")
    @Test
    void testIsCaseUrgent() {
        SscsCaseData caseData = SscsCaseData.builder()
            .urgentCase("Yes")
            .build();
        boolean result = HearingsDetailsMapping.isCaseUrgent(caseData);

        assertThat(result).isTrue();
    }

    @DisplayName("When urgentCase is No or blank, isCaseUrgent return False")
    @ParameterizedTest
    @ValueSource(strings = {"No"})
    @NullAndEmptySource
    void testIsCaseUrgent(String value) {
        SscsCaseData caseData = SscsCaseData.builder()
            .urgentCase(value)
            .build();
        boolean result = HearingsDetailsMapping.isCaseUrgent(caseData);

        assertThat(result).isFalse();
    }


    @Test
    void getHearingLocations_shouldReturnCorrespondingEpimsIdForVenue() {
        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingSubtype(HearingSubtype.builder()
                    .wantsHearingTypeFaceToFace("Yes")
                    .build())
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend("Yes")
                    .build())
                .build())
            .processingVenue(PROCESSING_VENUE_1)
            .dwpIsOfficerAttending("Yes")
            .build();

        given(venueService.getEpimsIdForVenue(caseData.getProcessingVenue())).willReturn(Optional.of("9876"));
        given(referenceDataServiceHolder.getVenueService()).willReturn(venueService);

        List<HearingLocation> result = HearingsDetailsMapping.getHearingLocations(
            caseData,
            referenceDataServiceHolder
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLocationId()).isEqualTo("9876");
        assertThat(result.get(0).getLocationType()).isEqualTo(COURT);
    }

    @DisplayName("Multiple hearing location Test")
    @Test
    void getMultipleHearingLocations_shouldReturnCorrespondingMultipleEpimsIdForVenue() {
        final SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingSubtype(HearingSubtype.builder()
                    .wantsHearingTypeFaceToFace("Yes")
                    .build())
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend("Yes")
                    .build())
                .build())
            .processingVenue(PROCESSING_VENUE_1)
            .dwpIsOfficerAttending("Yes")
            .build();
        Map<String, List<String>> multipleHearingLocations = new HashMap<>();
        multipleHearingLocations.put("Chester",new ArrayList<>(Arrays.asList("226511", "443014")));
        multipleHearingLocations.put("Manchester",new ArrayList<>(Arrays.asList("512401","701411")));
        multipleHearingLocations.put("Plymouth",new ArrayList<>(Arrays.asList("764728","235590")));
        given(venueService.getEpimsIdForVenue(caseData.getProcessingVenue())).willReturn(Optional.of("443014"));
        given(referenceDataServiceHolder.getVenueService()).willReturn(venueService);
        given(referenceDataServiceHolder.getMultipleHearingLocations()).willReturn(multipleHearingLocations);
        List<HearingLocation> result = HearingsDetailsMapping.getHearingLocations(
            caseData,
            referenceDataServiceHolder
        );

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLocationId()).isEqualTo("226511");
        assertThat(result.get(1).getLocationId()).isEqualTo("443014");

    }

    @DisplayName("When hearing is paper case, return list of regional hearing locations based on RPC name")
    @Test
    void getRegionalHearingLocations_shouldReturnCorrespondingEpimsIdsForVenuesWithSameRpc() {
        final SscsCaseData caseData = SscsCaseData.builder()
            .dwpIsOfficerAttending("No")
            .regionalProcessingCenter(RegionalProcessingCenter.builder()
                                          .name("SSCS Leeds")
                                          .build())
            .appeal(Appeal.builder()
                        .hearingOptions(HearingOptions.builder()
                                            .wantsToAttend("N")
                                            .build())
                        .build())
            .processingVenue(PROCESSING_VENUE_1)
            .build();
        given(venueService.getActiveRegionalEpimsIdsForRpc(caseData.getRegionalProcessingCenter().getEpimsId()))
            .willReturn(epimsIdsList);
        given(referenceDataServiceHolder.getVenueService()).willReturn(venueService);

        List<HearingLocation> result = HearingsDetailsMapping.getHearingLocations(
            caseData,
            referenceDataServiceHolder);

        checkHearingLocationResults(result,
            new Tuple[] {tuple(epimsId1, COURT), tuple(epimsId2, COURT),
                tuple(epimsId3, COURT), tuple(epimsId4, COURT)});
    }

    @DisplayName("getHearingPriority Parameterized Tests")
    @ParameterizedTest
    @CsvSource(value = {
        "Yes,Yes,Urgent", "Yes,No,Urgent", "No,Yes,Urgent",
        "No,No,Standard",
        "Yes,null,Urgent", "No,null,Standard",
        "null,Yes,Urgent", "null,No,Standard",
        "null,null,Standard",
        "Yes,,Urgent", "No,,Standard",
        ",Yes,Urgent", ",No,Standard",
        ",,Standard"
    }, nullValues = {"null"})
    void getHearingPriority(String isAdjournCase, String isUrgentCase, String expected) {
        // TODO Finish Test when method done
        SscsCaseData caseData = SscsCaseData.builder()
            .urgentCase(isUrgentCase)
            .adjournCasePanelMembersExcluded(isAdjournCase)
            .build();
        String result = HearingsDetailsMapping.getHearingPriority(caseData);

        assertEquals(expected, result);
    }

    @DisplayName("When case data with a valid processing venue is given,"
        + "getHearingLocations returns the correct venues")
    @ParameterizedTest
    @CsvSource(value = {HEARING_VENUE_ID_1 + ",court"}, nullValues = {"null"})
    void getHearingLocations() {
        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingSubtype(HearingSubtype.builder()
                    .wantsHearingTypeFaceToFace("Yes")
                    .build())
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend("Yes")
                    .build())
                .build())
            .processingVenue(PROCESSING_VENUE_1)
            .dwpIsOfficerAttending("Yes")
            .build();

        given(venueService.getEpimsIdForVenue(caseData.getProcessingVenue()))
            .willReturn(Optional.of(HEARING_VENUE_ID_1));
        given(referenceDataServiceHolder.getVenueService()).willReturn(venueService);

        List<HearingLocation> result = HearingsDetailsMapping.getHearingLocations(caseData, referenceDataServiceHolder);

        checkHearingLocationResults(result,
            new Tuple[] {tuple(HEARING_VENUE_ID_1, COURT)});
    }

    @DisplayName("When override Hearing Venue Epims Ids is not empty getHearingLocations returns the override values")
    @Test
    void getHearingLocationsOverride() {
        SscsCaseData caseData = buildOverrideHearingLocations();

        List<HearingLocation> result = HearingsDetailsMapping.getHearingLocations(caseData, referenceDataServiceHolder);

        checkHearingLocationResults(result,
            new Tuple[] {tuple(HEARING_VENUE_ID_1, COURT), tuple(HEARING_VENUE_ID_2, COURT)});
    }

    @DisplayName("When a case has been adjourned and a different venue has been selected, return the new venue")
    @Test
    void getHearingLocationsAdjournmentNewVenue() {
        SscsCaseData caseData = buildOverrideHearingLocations();

        setupAppeal(caseData);
        setupAdjournedHearingVenue(caseData, "somewhereElse");

        checkHearingLocationResults(HearingsDetailsMapping.getHearingLocations(caseData, referenceDataServiceHolder),
            new Tuple[] {tuple(HEARING_VENUE_ID_1, COURT)});
    }

    @DisplayName("When a case has been adjourned and the same venue has been selected, return the same venue")
    @Test
    void getHearingLocationsAdjournmentSameVenue() {
        SscsCaseData caseData = buildOverrideHearingLocations();

        setupAppeal(caseData);
        setupAdjournedHearingVenue(caseData, "sameVenue");

        caseData.setHearings(Collections.singletonList(Hearing.builder()
                    .value(uk.gov.hmcts.reform.sscs.ccd.domain.HearingDetails.builder()
                    .venueId(HEARING_VENUE_ID_2).build())
                .build()));

        checkHearingLocationResults(HearingsDetailsMapping.getHearingLocations(caseData, referenceDataServiceHolder),
            new Tuple[] {tuple(HEARING_VENUE_ID_2, COURT)});
    }

    @DisplayName("When a case has been adjourned but the next hearing is paper, return the override hearing locations")
    @Test
    void getHearingLocationsAdjournmentNewVenuePaperCase() {
        SscsCaseData caseData = buildOverrideHearingLocations();

        setupAdjournedHearingVenue(caseData, "somewhereElse");

        checkHearingLocationResults(HearingsDetailsMapping.getHearingLocations(caseData, referenceDataServiceHolder),
            new Tuple[] {tuple(HEARING_VENUE_ID_1, COURT), tuple(HEARING_VENUE_ID_2, COURT)});
    }

    @DisplayName("Checks both the errors we can throw when trying to obtain the venue ID when getting the locations")
    @Test
    void getHearingLocationsFailOnGettingVenueId() {
        SscsCaseData caseData = buildOverrideHearingLocations();

        setupAppeal(caseData);

        String unknownValue = "UnknownValue";
        setupAdjournedHearingVenue(caseData, "UnknownValue");

        assertThatThrownBy(() -> HearingsDetailsMapping.getHearingLocations(caseData, referenceDataServiceHolder))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Failed to determine next hearing venue: " + unknownValue);

        caseData.setAdjournCaseNextHearingVenue("sameVenue");

        assertThatThrownBy(() -> HearingsDetailsMapping.getHearingLocations(caseData, referenceDataServiceHolder))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Failed to determine next hearing venue due to no latest hearing");
    }

    void setupAdjournedHearingVenue(SscsCaseData caseData, String nextHearingVenue) {
        DynamicListItem item = new DynamicListItem(HEARING_VENUE_ID_1, "");
        DynamicList list = new DynamicList(item, Collections.emptyList());

        caseData.setAdjournCaseNextHearingVenue(nextHearingVenue);
        caseData.setAdjournCaseNextHearingVenueSelected(list);
    }

    void checkHearingLocationResults(List<HearingLocation> hearingLocations, Tuple[] expectedResult) {
        assertThat(hearingLocations)
            .hasSize(expectedResult.length)
            .extracting("locationId","locationType")
            .containsExactlyInAnyOrder(expectedResult);
    }

    void setupAppeal(SscsCaseData caseData) {
        caseData.getAppeal().setHearingSubtype(HearingSubtype.builder()
                                                   .wantsHearingTypeTelephone("yes")
                                                   .hearingTelephoneNumber(PHONE_NUMBER).build());
        caseData.getAppeal().setHearingOptions(HearingOptions.builder().wantsToAttend("yes").build());
    }

    SscsCaseData buildOverrideHearingLocations() {
        return SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder().build())
                .build())
            .schedulingAndListingFields(SchedulingAndListingFields.builder()
                .overrideFields(OverrideFields.builder()
                    .hearingVenueEpimsIds(List.of(
                        CcdValue.<CcdValue<String>>builder()
                            .value(CcdValue.<String>builder()
                                .value(HEARING_VENUE_ID_1)
                                .build())
                            .build(),
                        CcdValue.<CcdValue<String>>builder()
                            .value(CcdValue.<String>builder()
                                .value(HEARING_VENUE_ID_2)
                                .build())
                            .build()))
                    .build())
                .build())
            .processingVenue(PROCESSING_VENUE_1)
            .build();
    }

    @DisplayName("getFacilitiesRequired returns an empty list")
    @Test
    void testGetFacilitiesRequired() {
        List<String> individualReasonableAdjustments = HearingsDetailsMapping.getFacilitiesRequired();
        assertThat(individualReasonableAdjustments).isEmpty();
    }

    @DisplayName("When appellant and other parties Hearing Options other comments are given "
        + "getListingComments returns all the comments separated by newlines")
    @ParameterizedTest
    @CsvSource(value = {
        "AppellantComments,'OpComments1',Appellant - Mx Test Appellant:\\nAppellantComments\\n\\nOther Party - "
            + "Mx Test OtherParty:\\nOpComments1",
        "AppellantComments,'',Appellant - Mx Test Appellant:\\nAppellantComments",
        "AppellantComments,null,Appellant - Mx Test Appellant:\\nAppellantComments",
    }, nullValues = {"null"})
    void getListingComments(String appellant, String otherPartiesComments, String expected) {
        List<CcdValue<OtherParty>> otherParties = null;

        if (nonNull(otherPartiesComments)) {
            otherParties = new ArrayList<>();
            otherParties.add(new CcdValue<>(OtherParty.builder()
                .name(Name.builder()
                    .title("Mx")
                    .firstName("Test")
                    .lastName("OtherParty")
                    .build())
                .hearingOptions(HearingOptions.builder()
                    .other(otherPartiesComments)
                    .build())
                .build()));
        }

        Appeal appeal = Appeal.builder()
            .appellant(Appellant.builder()
                .name(Name.builder()
                    .title("Mx")
                    .firstName("Test")
                    .lastName("Appellant")
                    .build())
                .build())
            .build();
        if (nonNull(appellant)) {
            appeal.setHearingOptions(HearingOptions.builder().other(appellant).build());
        }

        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(appeal)
            .otherParties(otherParties)
            .build();

        String result = HearingsDetailsMapping.getListingComments(caseData);

        assertThat(result).isEqualToNormalizingNewlines(expected.replace("\\n", String.format("%n")));
    }

    @DisplayName("When all null or empty comments are given getListingComments returns null")
    @ParameterizedTest
    @CsvSource(value = {
        "'',''",
        "null,''",
        "null,null",
        "'','||'",
    }, nullValues = {"null"})
    void getListingCommentsReturnsNull(String appellant, String otherPartiesComments) {
        List<CcdValue<OtherParty>> otherParties = new ArrayList<>();
        if (nonNull(otherPartiesComments)) {
            for (String otherPartyComment : splitCsvParamArray(otherPartiesComments)) {
                otherParties.add(new CcdValue<>(OtherParty.builder()
                    .hearingOptions(HearingOptions.builder().other(otherPartyComment).build())
                    .build()));
            }
        } else {
            otherParties = null;
        }
        Appeal appeal = Appeal.builder().build();
        if (nonNull(appellant)) {
            appeal.setHearingOptions(HearingOptions.builder().other(appellant).build());
        }

        SscsCaseData caseData = SscsCaseData.builder()
            .appeal(appeal)
            .otherParties(otherParties)
            .build();

        String result = HearingsDetailsMapping.getListingComments(caseData);

        assertNull(result);
    }


    @DisplayName("getPartyRole with Role Test")
    @Test
    void getPartyRole() {
        Party party = Appellant.builder()
            .role(Role.builder()
                .name("Test Role")
                .build())
            .build();
        String result = HearingsDetailsMapping.getPartyRole(party);

        assertThat(result).isEqualTo("Test Role");
    }

    @DisplayName("getPartyRole without Role Test")
    @Test
    void getPartyRoleNoRole() {
        Party party = Appellant.builder().build();
        String result = HearingsDetailsMapping.getPartyRole(party);

        assertThat(result).isEqualTo("Appellant");
    }

    @DisplayName("getPartyRole with empty/null Role Name Test")
    @ParameterizedTest
    @NullAndEmptySource
    void getPartyRoleNoRole(String role) {
        Party party = Appellant.builder()
            .role(Role.builder()
                .name(role)
                .build())
            .build();
        String result = HearingsDetailsMapping.getPartyRole(party);

        assertThat(result).isEqualTo("Appellant");
    }

    @DisplayName("getPartyName where Appointee is null Test")
    @ParameterizedTest
    @ValueSource(strings = {"Yes", "No"})
    @NullAndEmptySource
    void getPartyName(String isAppointee) {
        Party party = Appellant.builder()
            .isAppointee(isAppointee)
            .name(Name.builder()
                .title("Mx")
                .firstName("Test")
                .lastName("Appellant")
                .build())
            .build();
        String result = HearingsDetailsMapping.getEntityName(party);

        assertThat(result).isEqualTo("Mx Test Appellant");
    }

    @DisplayName("getPartyName No Appointee Test")
    @ParameterizedTest
    @ValueSource(strings = {"No"})
    @NullAndEmptySource
    void getPartyNameAppellant(String isAppointee) {
        Party party = Appellant.builder()
            .isAppointee(isAppointee)
            .appointee(Appointee.builder()
                .name(Name.builder()
                    .title("Mx")
                    .firstName("Test")
                    .lastName("Appointee")
                    .build())
                .build())
            .name(Name.builder()
                .title("Mx")
                .firstName("Test")
                .lastName("Appellant")
                .build())
            .build();
        String result = HearingsDetailsMapping.getEntityName(party);

        assertThat(result).isEqualTo("Mx Test Appellant");
    }

    @DisplayName("getHearingRequester Test")
    @Test
    void getHearingRequester() {
        String result = HearingsDetailsMapping.getHearingRequester();

        assertNull(result);
    }

    @DisplayName("When .. is given getLeadJudgeContractType returns the correct LeadJudgeContractType")
    @Test
    void getLeadJudgeContractType() {
        // TODO Finish Test when method done
        String result = HearingsDetailsMapping.getLeadJudgeContractType();

        assertNull(result);
    }

    @DisplayName("When dwpIsOfficerAttending is yes, isPoOfficerAttending return True")
    @Test
    void testIsPoOfficerAttending() {
        SscsCaseData caseData = SscsCaseData.builder()
            .dwpIsOfficerAttending("Yes")
            .build();

        boolean result = HearingsDetailsMapping.isPoOfficerAttending(caseData);

        assertThat(result).isTrue();
    }

    @DisplayName("When dwpIsOfficerAttending is No or blank, isPoOfficerAttending return False")
    @ParameterizedTest
    @ValueSource(strings = {"No"})
    @NullAndEmptySource
    void testIsPoOfficerAttending(String value) {
        SscsCaseData caseData = SscsCaseData.builder()
            .dwpIsOfficerAttending(value)
            .build();

        boolean result = HearingsDetailsMapping.isPoOfficerAttending(caseData);

        assertThat(result).isFalse();
    }

    @DisplayName("When override poToAttend is Yes, isPoOfficerAttending returns true")
    @Test
    void testIsPoOfficerAttendingOverride() {
        SscsCaseData caseData = SscsCaseData.builder()
            .schedulingAndListingFields(SchedulingAndListingFields.builder()
                .overrideFields(OverrideFields.builder()
                    .poToAttend(YES)
                    .build())
                .build())
            .build();

        boolean result = HearingsDetailsMapping.isPoOfficerAttending(caseData);

        assertThat(result).isTrue();
    }

    @DisplayName("When override poToAttend is not Yes, isPoOfficerAttending returns the default value")
    @ParameterizedTest
    @ValueSource(strings = {"NO"})
    @NullSource
    void testIsPoOfficerAttendingOverride(YesNo value) {
        SscsCaseData caseData = SscsCaseData.builder()
            .schedulingAndListingFields(SchedulingAndListingFields.builder()
                .overrideFields(OverrideFields.builder()
                    .poToAttend(value)
                    .build())
                .build())
            .dwpIsOfficerAttending("No")
            .build();

        boolean result = HearingsDetailsMapping.isPoOfficerAttending(caseData);

        assertThat(result).isFalse();
    }

    @DisplayName("When override fields is null, isPoOfficerAttending returns the default value")
    @Test
    void testIsPoOfficerAttendingOverrideNull() {
        SscsCaseData caseData = SscsCaseData.builder()
            .dwpIsOfficerAttending("No")
            .build();

        boolean result = HearingsDetailsMapping.isPoOfficerAttending(caseData);

        assertThat(result).isFalse();
    }
}
