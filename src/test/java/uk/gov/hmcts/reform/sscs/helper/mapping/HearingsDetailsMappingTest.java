package uk.gov.hmcts.reform.sscs.helper.mapping;

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
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.model.HearingLocation;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.HearingType;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDetails;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel;
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

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.YES;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HearingType.SUBSTANTIVE;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.LocationType.COURT;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.NextHearingVenueType.SAME_VENUE;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.NextHearingVenueType.SOMEWHERE_ELSE;

class HearingsDetailsMappingTest extends HearingsMappingBase {

    @Mock
    private HearingDurationsService hearingDurations;

    @Mock
    private SessionCategoryMapService sessionCategoryMaps;

    @Mock
    private ReferenceDataServiceHolder referenceDataServiceHolder;

    @Mock
    private VenueService venueService;

    @Mock
    private VenueDetails venueDetails;

    private static final String PROCESSING_VENUE_1 = "test_place";

    private static final String REGIONAL_PROCESSING_CENTRE = "test_regional_processing_centre";
    private static final String PHONE_NUMBER = "07483871426";

    private static final String VENUE_ID = "12";

    private static final String EPIMS_ID_1 = "112233";
    private static final String EPIMS_ID_2 = "332211";
    private static final String EPIMS_ID_3 = "123123";
    private static final String EPIMS_ID_4 = "321321";
    private static final List<VenueDetails> EPIMS_ID_LIST = Arrays.asList(
        VenueDetails.builder().epimsId(EPIMS_ID_1).build(),
        VenueDetails.builder().epimsId(EPIMS_ID_2).build(),
        VenueDetails.builder().epimsId(EPIMS_ID_3).build(),
        VenueDetails.builder().epimsId(EPIMS_ID_4).build());

    private SscsCaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder().wantsToAttend("yes").build())
                .hearingSubtype(HearingSubtype.builder()
                    .wantsHearingTypeTelephone("yes")
                    .hearingTelephoneNumber(PHONE_NUMBER).build()).build())
            .processingVenue(PROCESSING_VENUE_1)
            .build();
    }

    @DisplayName("When a valid hearing wrapper is given buildHearingDetails returns the correct Hearing Details")
    @Test
    void buildHearingDetails() throws InvalidMappingException {
        given(hearingDurations.getHearingDuration(BENEFIT_CODE, ISSUE_CODE))
            .willReturn(new HearingDuration(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                                            60, 75, 30));

        given(sessionCategoryMaps.getSessionCategory(BENEFIT_CODE, ISSUE_CODE, false, false))
            .willReturn(new SessionCategoryMap(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                                               false, false, SessionCategory.CATEGORY_03, null));

        given(referenceDataServiceHolder.getHearingDurations()).willReturn(hearingDurations);
        given(referenceDataServiceHolder.getSessionCategoryMaps()).willReturn(sessionCategoryMaps);
        given(referenceDataServiceHolder.getVenueService()).willReturn(venueService);

        // TODO Finish Test when method done
        caseData = SscsCaseData.builder()
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
        caseData = SscsCaseData.builder()
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
        caseData = SscsCaseData.builder()
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
                .build()))
            .build();
        boolean result = HearingsDetailsMapping.isCaseLinked(caseData);

        assertThat(result).isTrue();
    }

    @DisplayName("When case has empty linkedCase isCaseLinked returns true")
    @Test
    void testIsCaseLinkedEmpty() {
        caseData = SscsCaseData.builder()
            .linkedCase(Collections.emptyList())
            .build();
        boolean result = HearingsDetailsMapping.isCaseLinked(caseData);

        assertThat(result).isFalse();
    }

    @DisplayName("When case has null linkedCase isCaseLinked returns true")
    @Test
    void testIsCaseLinkedNull() {
        caseData = SscsCaseData.builder()
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
        caseData = SscsCaseData.builder()
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
        caseData = SscsCaseData.builder()
            .urgentCase(value)
            .build();
        boolean result = HearingsDetailsMapping.isCaseUrgent(caseData);

        assertThat(result).isFalse();
    }


    @Test
    void getHearingLocations_shouldReturnCorrespondingEpimsIdForVenue() throws InvalidMappingException {
        caseData = SscsCaseData.builder()
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

        given(venueService.getEpimsIdForVenue(caseData.getProcessingVenue())).willReturn(EPIMS_ID_1);
        given(referenceDataServiceHolder.getVenueService()).willReturn(venueService);

        checkHearingLocationResults(HearingsLocationMapping.getHearingLocations(caseData, referenceDataServiceHolder),
                                    EPIMS_ID_1);
    }

    @DisplayName("Multiple hearing location Test")
    @Test
    void getMultipleHearingLocations_shouldReturnCorrespondingMultipleEpimsIdForVenue() throws InvalidMappingException {
        caseData = SscsCaseData.builder()
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
        given(venueService.getEpimsIdForVenue(caseData.getProcessingVenue())).willReturn("443014");
        given(referenceDataServiceHolder.getVenueService()).willReturn(venueService);
        given(referenceDataServiceHolder.getMultipleHearingLocations()).willReturn(multipleHearingLocations);
        List<HearingLocation> result = HearingsLocationMapping.getHearingLocations(
            caseData, referenceDataServiceHolder);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLocationId()).isEqualTo("226511");
        assertThat(result.get(1).getLocationId()).isEqualTo("443014");

    }

    @DisplayName("When hearing is paper case, return list of regional hearing locations based on RPC name")
    @Test
    void getRegionalHearingLocations_shouldReturnCorrespondingEpimsIdsForVenuesWithSameRpc()
        throws InvalidMappingException {
        caseData = SscsCaseData.builder()
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
            .willReturn(EPIMS_ID_LIST);
        given(referenceDataServiceHolder.getVenueService()).willReturn(venueService);

        List<HearingLocation> result = HearingsLocationMapping.getHearingLocations(
            caseData,
            referenceDataServiceHolder);

        checkHearingLocationResults(result, EPIMS_ID_1, EPIMS_ID_2, EPIMS_ID_3, EPIMS_ID_4);
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
        caseData.setUrgentCase(isUrgentCase);
        caseData.setAdjournCasePanelMembersExcluded(isAdjournCase);
        String result = HearingsDetailsMapping.getHearingPriority(caseData);

        assertEquals(expected, result);
    }

    @DisplayName("When case data with a valid processing venue is given, getHearingLocations returns the correct venues")
    @ParameterizedTest
    @CsvSource(value = {"219164,court"}, nullValues = {"null"})
    void getHearingLocations() throws InvalidMappingException {
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

        given(venueService.getEpimsIdForVenue(caseData.getProcessingVenue())).willReturn(EPIMS_ID_1);
        given(referenceDataServiceHolder.getVenueService()).willReturn(venueService);

        checkHearingLocationResults(HearingsLocationMapping.getHearingLocations(caseData, referenceDataServiceHolder),
                                    EPIMS_ID_1);
    }

    @DisplayName("When override Hearing Venue Epims Ids is not empty getHearingLocations returns the override values")
    @Test
    void getHearingLocationsOverride() throws InvalidMappingException {
        buildOverrideHearingLocations();

        checkHearingLocationResults(HearingsLocationMapping.getHearingLocations(caseData, referenceDataServiceHolder),
                                    EPIMS_ID_1, EPIMS_ID_2);
    }

    @DisplayName("When a case has been adjourned and a different venue has been selected, return the new venue")
    @Test
    void getHearingLocationsAdjournmentNewVenue() throws InvalidMappingException {
        enableAdjournmentFlag();

        given(referenceDataServiceHolder.getVenueService()).willReturn(venueService);
        given(venueService.getVenueDetailsForActiveVenueByEpimsId(EPIMS_ID_1)).willReturn(venueDetails);
        given(venueService.getEpimsIdForVenueId(VENUE_ID)).willReturn(EPIMS_ID_1);
        given(venueDetails.getRegionalProcessingCentre()).willReturn(REGIONAL_PROCESSING_CENTRE);

        setupAdjournedHearingVenue(SOMEWHERE_ELSE.getValue(), VENUE_ID);

        List<HearingLocation> results = HearingsLocationMapping.getHearingLocations(
            caseData, referenceDataServiceHolder);

        checkHearingLocationResults(results, EPIMS_ID_1);

        assertThat(results.get(0).getRegion()).isEqualTo(REGIONAL_PROCESSING_CENTRE);
    }

    @DisplayName("When a case has been adjourned and the same venue has been selected, return the same venue")
    @Test
    void getHearingLocationsAdjournmentSameVenue() throws InvalidMappingException {
        enableAdjournmentFlag();

        given(referenceDataServiceHolder.getVenueService()).willReturn(venueService);
        given(venueService.getVenueDetailsForActiveVenueByEpimsId(EPIMS_ID_2)).willReturn(venueDetails);

        setupAdjournedHearingVenue(SAME_VENUE.getValue(), EPIMS_ID_1);

        caseData.setHearings(Collections.singletonList(Hearing.builder()
                    .value(uk.gov.hmcts.reform.sscs.ccd.domain.HearingDetails.builder()
                    .epimsId(EPIMS_ID_2).build())
                .build()));

        checkHearingLocationResults(
            HearingsLocationMapping.getHearingLocations(caseData, referenceDataServiceHolder),
            EPIMS_ID_2);
    }

    @DisplayName("When a case has been adjourned but the next hearing is paper, return the override hearing locations")
    @Test
    void getHearingLocationsAdjournmentNewVenuePaperCase() throws InvalidMappingException {
        buildOverrideHearingLocations();

        setupAdjournedHearingVenue(SOMEWHERE_ELSE.getValue(), EPIMS_ID_1);

        caseData.getSchedulingAndListingFields().getOverrideFields().setAppellantHearingChannel(HearingChannel.PAPER);

        checkHearingLocationResults(HearingsLocationMapping.getHearingLocations(caseData, referenceDataServiceHolder),
                                    EPIMS_ID_1, EPIMS_ID_2);
    }

    @DisplayName("Checks both the errors we can throw when trying to obtain the venue ID when getting the locations")
    @Test
    void getHearingLocationsFailOnGettingVenueId() {
        enableAdjournmentFlag();

        caseData.setAdjournCaseNextHearingVenue(SAME_VENUE.getValue());

        assertThatThrownBy(() -> HearingsLocationMapping.getHearingLocations(caseData, referenceDataServiceHolder))
            .isInstanceOf(InvalidMappingException.class)
            .hasMessageContaining("Failed to determine next hearing location due to no latest hearing on case "
                                      + caseData.getCcdCaseId());
    }

    @DisplayName("Checks that the flag will make sure the code isn't run and returns the override values")
    @Test
    void getHearingLocationsCheckFlag() throws InvalidMappingException {
        given(venueService.getEpimsIdForVenue(caseData.getProcessingVenue())).willReturn(EPIMS_ID_1);
        given(referenceDataServiceHolder.getVenueService()).willReturn(venueService);

        given(referenceDataServiceHolder.isAdjournmentFlagEnabled()).willReturn(false); //TODO: remove flag

        checkHearingLocationResults(HearingsLocationMapping.getHearingLocations(caseData, referenceDataServiceHolder),
                                    EPIMS_ID_1);
    }

    void setupAdjournedHearingVenue(String nextHearingVenue, String adjournedId) {
        DynamicListItem item = new DynamicListItem(adjournedId, "");
        DynamicList list = new DynamicList(item, Collections.emptyList());

        caseData.setAdjournCaseNextHearingVenue(nextHearingVenue);
        caseData.setAdjournCaseNextHearingVenueSelected(list);
    }

    void checkHearingLocationResults(List<HearingLocation> hearingLocations, String... expectedResults) {
        assertThat(hearingLocations)
            .hasSize(expectedResults.length)
            .allSatisfy(hearingLocation ->
                assertThat(hearingLocation.getLocationType()).isEqualTo(COURT))
            .extracting(HearingLocation::getLocationId)
            .containsExactlyInAnyOrder(expectedResults);
    }

    void buildOverrideHearingLocations() {
        caseData.getSchedulingAndListingFields().setOverrideFields(OverrideFields.builder()
            .hearingVenueEpimsIds(List.of(
                CcdValue.<CcdValue<String>>builder()
                    .value(CcdValue.<String>builder()
                            .value(EPIMS_ID_1)
                            .build())
                    .build(),
                CcdValue.<CcdValue<String>>builder()
                    .value(CcdValue.<String>builder()
                            .value(EPIMS_ID_2)
                            .build())
                    .build()))
                .build());
    }

    void enableAdjournmentFlag() {
        given(referenceDataServiceHolder.isAdjournmentFlagEnabled()).willReturn(true); //TODO: remove flag
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
        "AppellantComments,'OpComments1',Appellant - Mx Test Appellant:\\nAppellantComments\\n\\n"
            + "Other Party - Mx Test OtherParty:\\nOpComments1",
        "AppellantComments,'',Appellant - Mx Test Appellant:\\nAppellantComments",
        "AppellantComments,null,Appellant - Mx Test Appellant:\\nAppellantComments",
    }, nullValues = {"null"})
    void getListingComments(String appellant, String otherPartiesComments, String expected) {
        List<CcdValue<OtherParty>> otherParties = new ArrayList<>();

        if (nonNull(otherPartiesComments)) {
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

        caseData = SscsCaseData.builder()
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

        caseData = SscsCaseData.builder()
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
        caseData = SscsCaseData.builder()
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
        caseData = SscsCaseData.builder()
            .dwpIsOfficerAttending(value)
            .build();

        boolean result = HearingsDetailsMapping.isPoOfficerAttending(caseData);

        assertThat(result).isFalse();
    }

    @DisplayName("When override poToAttend is Yes, isPoOfficerAttending returns true")
    @Test
    void testIsPoOfficerAttendingOverride() {
        caseData = SscsCaseData.builder()
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
        caseData = SscsCaseData.builder()
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
        caseData = SscsCaseData.builder()
            .dwpIsOfficerAttending("No")
            .build();

        boolean result = HearingsDetailsMapping.isPoOfficerAttending(caseData);

        assertThat(result).isFalse();
    }
}
