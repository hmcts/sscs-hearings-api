package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.CaseFlags;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.HearingWindow;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.HearingWindowDateRange;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PartyFlags;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.ServiceHearingValues;
import uk.gov.hmcts.reform.sscs.model.single.hearing.*;
import uk.gov.hmcts.reform.sscs.reference.data.model.SessionCategoryMap;
import uk.gov.hmcts.reform.sscs.reference.data.service.HearingDurationsService;
import uk.gov.hmcts.reform.sscs.reference.data.service.SessionCategoryMapService;
import uk.gov.hmcts.reform.sscs.reference.data.service.SignLanguagesService;
import uk.gov.hmcts.reform.sscs.reference.data.service.VerbalLanguagesService;
import uk.gov.hmcts.reform.sscs.service.ReferenceDataServiceHolder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsDetailsMapping.DAYS_TO_ADD_HEARING_WINDOW_TODAY;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingTypeLov.SUBSTANTIVE;

@ExtendWith(MockitoExtension.class)
class ServiceHearingValuesMappingTest extends HearingsMappingBase {


    private static final String NOTE_FROM_OTHER_PARTY = "other party note";
    private static final String NOTE_FROM_APPELLANT = "appellant note";
    public static final String FACE_TO_FACE = "faceToFace";

    private static SscsCaseDetails sscsCaseDetails;

    @Mock
    public HearingDurationsService hearingDurations;

    @Mock
    public VerbalLanguagesService verbalLanguages;

    @Mock
    public SignLanguagesService signLanguages;

    @Mock
    private static ReferenceDataServiceHolder referenceData;

    @Mock
    private static SessionCategoryMapService sessionCategoryMaps;

    @BeforeEach
    public void setUp() {
        sscsCaseDetails = SscsCaseDetails.builder()
            .data(SscsCaseData.builder()
                      .ccdCaseId("1234")
                      .benefitCode(BENEFIT_CODE)
                      .issueCode(ISSUE_CODE)
                      .urgentCase("Yes")
                      .adjournCaseCanCaseBeListedRightAway("Yes")
                      .caseManagementLocation(CaseManagementLocation.builder()
                              .baseLocation("LIVERPOOL SOCIAL SECURITY AND CHILD SUPPORT TRIBUNAL")
                              .region("North West")
                              .build())
                      .appeal(Appeal.builder()
                                  .hearingType("final")
                                  .appellant(Appellant.builder()
                                                 .name(Name.builder()
                                                           .firstName("Fred")
                                                           .lastName("Flintstone")
                                                           .title("Mr")
                                                           .build())
                                                 .build())
                                  .hearingSubtype(HearingSubtype.builder()
                                                      .hearingTelephoneNumber("0999733733")
                                                      .hearingVideoEmail("test@gmail.com")
                                                      .wantsHearingTypeFaceToFace("Yes")
                                                      .wantsHearingTypeTelephone("No")
                                                      .wantsHearingTypeVideo("No")
                                                      .build())
                                  .hearingOptions(HearingOptions.builder()
                                                      .wantsToAttend("Yes")
                                                      .wantsSupport("Yes")
                                                      .languageInterpreter("Yes")
                                                      .languages("Bulgarian")
                                                      .signLanguageType("Makaton")
                                                      .arrangements(Arrays.asList(
                                                          "signLanguageInterpreter",
                                                          "hearingLoop",
                                                          "disabledAccess"
                                                      ))
                                                      .scheduleHearing("No")
                                                      .excludeDates(getExcludeDates())
                                                      .agreeLessNotice("No")
                                                      .other(NOTE_FROM_APPELLANT)
                                                      .build())
                                  .rep(Representative.builder()
                                           .id("12321")
                                           .hasRepresentative("Yes")
                                           .name(Name.builder()
                                                     .title("Mr")
                                                     .firstName("Harry")
                                                     .lastName("Potter")
                                                     .build())
                                           .address(Address.builder()
                                                        .line1("123 Hairy Lane")
                                                        .line2("Off Hairy Park")
                                                        .town("Town")
                                                        .county("County")
                                                        .postcode("CM14 4LQ")
                                                        .build())
                                           .contact(Contact.builder()
                                                        .email("harry.potter@wizards.com")
                                                        .mobile("07411999999")
                                                        .phone(null)
                                                        .build())
                                           .organisation("HP Ltd")
                                           .build())
                                  .build())
                      .events(getEventsOfCaseData())
                      .languagePreferenceWelsh("No")
                      .otherParties(getOtherParties())
                      .linkedCasesBoolean("No")
                      .sscsIndustrialInjuriesData(SscsIndustrialInjuriesData.builder()
                              .panelDoctorSpecialism("cardiologist")
                              .secondPanelDoctorSpecialism("eyeSurgeon")
                              .build())
                      .build())
            .build();

        SessionCategoryMap sessionCategoryMap = new SessionCategoryMap(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                false, false, SessionCategory.CATEGORY_06, null);

        given(sessionCategoryMaps.getSessionCategory(BENEFIT_CODE, ISSUE_CODE,true,false))
                .willReturn(sessionCategoryMap);
        given(sessionCategoryMaps.getCategoryTypeValue(sessionCategoryMap))
                .willReturn("BBA3-002");
        given(sessionCategoryMaps.getCategorySubTypeValue(sessionCategoryMap))
                .willReturn("BBA3-002-DD");

        given(referenceData.getSessionCategoryMaps()).willReturn(sessionCategoryMaps);

        given(hearingDurations.getHearingDuration(BENEFIT_CODE,ISSUE_CODE)).willReturn(null);

        given(referenceData.getHearingDurations()).willReturn(hearingDurations);

        given(verbalLanguages.getVerbalLanguageReference("Bulgarian"))
                .willReturn("bul");

        given(referenceData.getVerbalLanguages()).willReturn(verbalLanguages);

        given(signLanguages.getSignLanguageReference("Makaton"))
                .willReturn("sign-mkn");

        given(referenceData.getSignLanguages()).willReturn(signLanguages);

    }

    @Test
    void shouldMapServiceHearingValuesSuccessfully() throws InvalidMappingException {
        // given
        SscsCaseData sscsCaseData = sscsCaseDetails.getData();
        // when
        final ServiceHearingValues serviceHearingValues = ServiceHearingValuesMapping.mapServiceHearingValues(sscsCaseDetails, referenceData);
        final HearingWindow expectedHearingWindow = HearingWindow.builder()
                .hearingWindowDateRange(HearingWindowDateRange.builder()
                .hearingWindowStartDateRange(LocalDate.now().plusDays(DAYS_TO_ADD_HEARING_WINDOW_TODAY).toString()).build()).build();
        //then
        assertEquals(sscsCaseData.getCaseAccessManagementFields().getCaseNameHmctsInternal(), serviceHearingValues.getCaseName());
        assertEquals(sscsCaseData.getCaseAccessManagementFields().getCaseNamePublic(), serviceHearingValues.getCaseNamePublic());
        assertFalse(serviceHearingValues.isAutoListFlag());
        assertEquals(30, serviceHearingValues.getDuration());
        assertEquals(SUBSTANTIVE.getHmcReference(), serviceHearingValues.getHearingType());
        assertEquals(sscsCaseData.getBenefitCode(), serviceHearingValues.getCaseType());
        List<String> categoryValueList = serviceHearingValues.getCaseCategories().stream().filter(c -> "caseType".equals(c.getCategoryType())).map(CaseCategory::getCategoryValue).collect(Collectors.toList());
        assertEquals("BBA3-002", categoryValueList.stream().findFirst().orElse(""));
        categoryValueList = serviceHearingValues.getCaseCategories().stream().filter(c -> "caseSubType".equals(c.getCategoryType())).map(CaseCategory::getCategoryValue).collect(Collectors.toList());
        assertEquals("BBA3-002-DD", categoryValueList.stream().findFirst().orElse(""));
        assertEquals(expectedHearingWindow, serviceHearingValues.getHearingWindow());
        assertEquals("high", serviceHearingValues.getHearingPriorityType());
        assertEquals(3, serviceHearingValues.getNumberOfPhysicalAttendees());
        assertFalse(serviceHearingValues.isHearingInWelshFlag());
        assertEquals(1, serviceHearingValues.getHearingLocations().size());
        assertTrue(serviceHearingValues.getCaseAdditionalSecurityFlag());
        assertEquals(Arrays.asList("signLanguageInterpreter",
            "hearingLoop",
            "disabledAccess"
        ), serviceHearingValues.getFacilitiesRequired());
        assertThat(serviceHearingValues.getListingComments())
            .isEqualToIgnoringWhitespace(NOTE_FROM_OTHER_APPELLANT + NOTE_FROM_OTHER_APPELLANT + "\n" + "\n" + NOTE_FROM_OTHER_PARTY + NOTE_FROM_OTHER_PARTY);
        assertNull(serviceHearingValues.getHearingRequester());
        assertFalse(serviceHearingValues.isPrivateHearingRequiredFlag());
        assertNull(serviceHearingValues.getLeadJudgeContractType());
        assertThat(serviceHearingValues.getJudiciary()).isNotNull();
        assertThat(serviceHearingValues.getJudiciary().getJudiciarySpecialisms())
                .hasSize(3)
                .contains("BBA3-MQPM1-001","BBA3-MQPM2-003","BBA3-?");
        assertFalse(serviceHearingValues.isHearingIsLinkedFlag());
        assertEquals(getCaseFlags(), serviceHearingValues.getCaseFlags());
        assertNull(serviceHearingValues.getVocabulary());
    }

    @Test
    void shouldMapPartiesInServiceHearingValues() throws InvalidMappingException {
        // given
        SscsCaseData sscsCaseData = sscsCaseDetails.getData();
        // when
        final ServiceHearingValues serviceHearingValues = ServiceHearingValuesMapping.mapServiceHearingValues(sscsCaseDetails, referenceData);
        //then
        assertEquals(3, serviceHearingValues.getParties().size());
        assertEquals("BBA3-a", serviceHearingValues.getParties().stream().findFirst().orElseThrow().getPartyRole());
        assertEquals("BBA3-j", serviceHearingValues.getParties().stream().filter(partyDetails -> PartyType.ORG == partyDetails.getPartyType()).findFirst().orElseThrow().getPartyRole());
        assertEquals("BBA3-d", serviceHearingValues.getParties().stream().filter(partyDetails -> "party_id_1".equals(partyDetails.getPartyID())).findFirst().orElseThrow().getPartyRole());
    }

    private List<Event> getEventsOfCaseData() {
        return new ArrayList<>() {{
                add(Event.builder()
                        .value(EventDetails.builder()
                                   .date("2022-02-12T20:30:00")
                                   .type("responseReceived")
                                   .description("Dwp respond")
                                   .build())
                        .build());
            }
        };
    }


    private List<CcdValue<OtherParty>> getOtherParties() {
        return new ArrayList<>() {
            {
                add(new CcdValue<>(OtherParty.builder()
                                   .id("party_id_1")
                                   .name(Name.builder()
                                             .firstName("Barny")
                                             .lastName("Boulderstone")
                                             .title("Mr")
                                             .build())
                                   .address(Address.builder().build())
                                   .confidentialityRequired(YesNo.NO)
                                   .unacceptableCustomerBehaviour(YesNo.YES)
                                   .hearingSubtype(HearingSubtype.builder()
                                                       .hearingTelephoneNumber("0999733735")
                                                       .hearingVideoEmail("test2@gmail.com")
                                                       .wantsHearingTypeFaceToFace("Yes")
                                                       .wantsHearingTypeTelephone("No")
                                                       .wantsHearingTypeVideo("No")
                                                       .build())
                                   .hearingOptions(HearingOptions.builder()
                                                       .wantsToAttend("Yes")
                                                       .wantsSupport("Yes")
                                                       .languageInterpreter("Yes")
                                                       .languages("Bulgarian")
                                                       .scheduleHearing("No")
                                                       .excludeDates(getExcludeDates())
                                                       .agreeLessNotice("No")
                                                       .other(NOTE_FROM_OTHER_PARTY)
                                                       .build())
                                   .isAppointee("No")
                                   .appointee(Appointee.builder().build())
                                   .rep(Representative.builder().build())
                                   .otherPartySubscription(Subscription.builder().build())
                                   .otherPartyAppointeeSubscription(Subscription.builder().build())
                                   .otherPartyRepresentativeSubscription(Subscription.builder().build())
                                   .sendNewOtherPartyNotification(YesNo.NO)
                                   .reasonableAdjustment(ReasonableAdjustmentDetails.builder()
                                                             .reasonableAdjustmentRequirements("Some adjustments...")
                                                             .wantsReasonableAdjustment(YesNo.YES)
                                                             .build())
                                   .appointeeReasonableAdjustment(ReasonableAdjustmentDetails.builder().build())
                                   .repReasonableAdjustment(ReasonableAdjustmentDetails.builder().build())
                                   .role(Role.builder()
                                             .name("party_role")
                                             .description("description")
                                             .build())
                                   .build()));
            }
        };
    }



    private List<PartyDetails> getParties() {
        return new ArrayList<>() {{
                add(PartyDetails.builder()
                        .partyID(null)
                        .partyType(PartyType.IND.getPartyLabel())
                        .partyChannelSubType(FACE_TO_FACE)
                        .partyRole("BBA3-appellant")
                        .individualDetails(getIndividualDetails())
                        .organisationDetails(OrganisationDetails.builder().build())
                        .unavailabilityDayOfWeek(null)
                        .unavailabilityRanges(getUnavailabilityRanges())
                        .build());
                add(PartyDetails.builder()
                    .partyID("party_id_1")
                    .partyType(PartyType.IND.getPartyLabel())
                    .partyChannelSubType(FACE_TO_FACE)
                    .partyRole("party_role")
                    .individualDetails(getIndividualDetails())
                    .organisationDetails(OrganisationDetails.builder().build())
                    .unavailabilityDayOfWeek(null)
                    .unavailabilityRanges(getUnavailabilityRanges())
                    .build());
            }
        };
    }

    private List<UnavailabilityRange> getUnavailabilityRanges() {
        return new ArrayList<>() {
            {
                add(UnavailabilityRange.builder()
                    .unavailableFromDate(LocalDate.of(2022, 1,12))
                    .unavailableToDate(LocalDate.of(2022,1,19))
                    .build());
            }};
    }

    private IndividualDetails getIndividualDetails() {
        return IndividualDetails.builder()
            .firstName("Barny")
            .lastName("Boulderstone")
            .preferredHearingChannel(FACE_TO_FACE)
            .interpreterLanguage("tel")
            .reasonableAdjustments(new ArrayList<>())
            .vulnerableFlag(false)
            .vulnerabilityDetails(null)
            .hearingChannelEmail(Collections.singletonList("test2@gmail.com"))
            .hearingChannelPhone(Collections.singletonList("0999733735"))
            .relatedParties(getRelatedParties()) // TODO this field would be populated when the corresponding method is finished
            .build();
    }

    private List<RelatedParty> getRelatedParties() {
        return new ArrayList<>();
    }


    private List<ExcludeDate> getExcludeDates() {
        return new ArrayList<>() {
            {
                add(ExcludeDate.builder()
                    .value(DateRange.builder()
                               .start("2022-01-12")
                               .end("2022-01-19")
                               .build())
                    .build());
            }
        };
    }

    // TODO it will be populated when the method is provided
    private CaseFlags getCaseFlags() {
        return CaseFlags.builder()
            .flags(getPartyFlags())
            .flagAmendUrl("")
            .build();
    }

    private List<PartyFlags> getPartyFlags() {
        return new ArrayList<>() {{
                add(PartyFlags.builder()
                    .partyName(null)
                    .flagParentId("10")
                    .flagId("44")
                    .flagDescription("Sign Language Interpreter")
                    .flagStatus(null)
                    .build());
                add(PartyFlags.builder()
                    .partyName(null)
                    .flagParentId("6")
                    .flagId("21")
                    .flagDescription("Step free / wheelchair access")
                    .flagStatus(null)
                    .build());
                add(PartyFlags.builder()
                    .partyName(null)
                    .flagParentId("11")
                    .flagId("45")
                    .flagDescription("Hearing loop (hearing enhancement system)")
                    .flagStatus(null)
                    .build());
                add(PartyFlags.builder()
                    .partyName(null)
                    .flagParentId("1")
                    .flagId("67")
                    .flagDescription("Urgent flag")
                    .flagStatus(null)
                    .build());
            }
        };
    }
}
