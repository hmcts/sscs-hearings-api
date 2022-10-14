package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.ccd.domain.Address;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appointee;
import uk.gov.hmcts.reform.sscs.ccd.domain.BenefitCode;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseManagementLocation;
import uk.gov.hmcts.reform.sscs.ccd.domain.CcdValue;
import uk.gov.hmcts.reform.sscs.ccd.domain.Contact;
import uk.gov.hmcts.reform.sscs.ccd.domain.DateRange;
import uk.gov.hmcts.reform.sscs.ccd.domain.Event;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.ExcludeDate;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingSubtype;
import uk.gov.hmcts.reform.sscs.ccd.domain.Issue;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.OtherParty;
import uk.gov.hmcts.reform.sscs.ccd.domain.ReasonableAdjustmentDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.Representative;
import uk.gov.hmcts.reform.sscs.ccd.domain.Role;
import uk.gov.hmcts.reform.sscs.ccd.domain.SessionCategory;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsIndustrialInjuriesData;
import uk.gov.hmcts.reform.sscs.ccd.domain.Subscription;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.EntityRoleCode;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.CaseFlags;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PartyDetails;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PartyFlags;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.ServiceHearingValues;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingWindow;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingPriority;
import uk.gov.hmcts.reform.sscs.reference.data.model.Language;
import uk.gov.hmcts.reform.sscs.reference.data.model.SessionCategoryMap;
import uk.gov.hmcts.reform.sscs.reference.data.service.HearingDurationsService;
import uk.gov.hmcts.reform.sscs.reference.data.service.SessionCategoryMapService;
import uk.gov.hmcts.reform.sscs.reference.data.service.SignLanguagesService;
import uk.gov.hmcts.reform.sscs.reference.data.service.VerbalLanguagesService;
import uk.gov.hmcts.reform.sscs.service.VenueService;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.NO;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.YES;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsWindowMapping.DAYS_TO_ADD_HEARING_WINDOW_TODAY;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.CaseCategoryType.CASE_SUBTYPE;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.CaseCategoryType.CASE_TYPE;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HearingType.SUBSTANTIVE;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.PartyType.INDIVIDUAL;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel.FACE_TO_FACE;

@ExtendWith(MockitoExtension.class)
class ServiceHearingValuesMappingTest extends HearingsMappingBase {

    public static final String BULGARIAN = "Bulgarian";
    private static final String NOTE_FROM_OTHER_PARTY = "other party note";
    private static final String NOTE_FROM_APPELLANT = "appellant note";

    public static final String BENEFIT = "Benefit";

    public static final String APPELLANT_PARTY_ID = "a2b837d5-ee28-4bc9-a3d8-ce2d2de9fb296292997e-14d4-4814-a163-e64018d2c441";
    public static final String REPRESENTATIVE_PARTY_ID = "a2b837d5-ee28-4bc9-a3d8-ce2d2de9fb29";
    public static final String OTHER_PARTY_ID = "4dd6b6fa-6562-4699-8e8b-6c70cf8a333e";

    @Mock
    public HearingDurationsService hearingDurations;

    @Mock
    public VerbalLanguagesService verbalLanguages;

    @Mock
    public SignLanguagesService signLanguages;

    @Mock
    private ReferenceDataServiceHolder referenceDataServiceHolder;

    @Mock
    private SessionCategoryMapService sessionCategoryMaps;

    @Mock
    private VenueService venueService;
    private SscsCaseData caseData;

    @BeforeEach
    public void setUp() {
        caseData = SscsCaseData.builder()
            .ccdCaseId("1234")
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .urgentCase("Yes")
            .adjournCaseCanCaseBeListedRightAway("Yes")
            .adjournCaseInterpreterRequired("Yes")
            .adjournCaseInterpreterLanguage(BULGARIAN)
            .caseManagementLocation(CaseManagementLocation.builder()
                .baseLocation("LIVERPOOL SOCIAL SECURITY AND CHILD SUPPORT TRIBUNAL")
                .region("North West")
                .build())
            .appeal(Appeal.builder()
                .hearingType("final")
                .appellant(Appellant.builder()
                    .id(APPELLANT_PARTY_ID)
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
                    .languages(BULGARIAN)
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
                    .id(REPRESENTATIVE_PARTY_ID)
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
            .build();

        SessionCategoryMap sessionCategoryMap = new SessionCategoryMap(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                false, false, SessionCategory.CATEGORY_06, null);

        given(sessionCategoryMaps.getSessionCategory(BENEFIT_CODE, ISSUE_CODE,true,false))
            .willReturn(sessionCategoryMap);
        given(sessionCategoryMaps.getCategoryTypeValue(sessionCategoryMap))
            .willReturn("BBA3-002");
        given(sessionCategoryMaps.getCategorySubTypeValue(sessionCategoryMap))
            .willReturn("BBA3-002-DD");

        given(referenceDataServiceHolder.getSessionCategoryMaps()).willReturn(sessionCategoryMaps);

        given(referenceDataServiceHolder.getVerbalLanguages()).willReturn(verbalLanguages);

        given(referenceDataServiceHolder.getSignLanguages()).willReturn(signLanguages);

        given(hearingDurations.getHearingDuration(BENEFIT_CODE,ISSUE_CODE)).willReturn(null);

        given(referenceDataServiceHolder.getHearingDurations()).willReturn(hearingDurations);

        given(referenceDataServiceHolder.getVerbalLanguages().getVerbalLanguage(BULGARIAN))
            .willReturn(new Language("bul","Test",null,null,List.of(BULGARIAN)));

        given(referenceDataServiceHolder.getSignLanguages().getSignLanguage("Makaton"))
            .willReturn(new Language("sign-mkn","Test",null,null,List.of("Makaton")));
    }

    @Test
    void shouldMapServiceHearingValuesSuccessfully() throws InvalidMappingException {
        // given
        given(referenceDataServiceHolder.getVenueService()).willReturn(venueService);
        // when
        final ServiceHearingValues serviceHearingValues = ServiceHearingValuesMapping.mapServiceHearingValues(caseData, referenceDataServiceHolder);
        final HearingWindow expectedHearingWindow = HearingWindow.builder()
            .dateRangeStart(LocalDate.now().plusDays(DAYS_TO_ADD_HEARING_WINDOW_TODAY))
            .build();
        //then
        assertThat(serviceHearingValues.isAutoListFlag()).isFalse();
        assertThat(serviceHearingValues.getDuration()).isEqualTo(30);
        assertThat(serviceHearingValues.getHearingType()).isEqualTo(SUBSTANTIVE);
        assertThat(serviceHearingValues.getCaseType()).isEqualTo(BENEFIT);
        assertThat(serviceHearingValues.getCaseCategories())
            .extracting("categoryType","categoryValue")
            .containsExactlyInAnyOrder(
                tuple(CASE_TYPE,"BBA3-002"),
                tuple(CASE_SUBTYPE,"BBA3-002-DD"));
        assertThat(serviceHearingValues.getHearingWindow()).isEqualTo(expectedHearingWindow);
        assertThat(serviceHearingValues.getHearingPriorityType()).isEqualTo(HearingPriority.URGENT.getHmcReference());
        assertThat(serviceHearingValues.getNumberOfPhysicalAttendees()).isEqualTo(4);
        assertThat(serviceHearingValues.isHearingInWelshFlag()).isFalse();
        assertThat(serviceHearingValues.getHearingLocations()).hasSize(1);
        assertThat(serviceHearingValues.getCaseAdditionalSecurityFlag()).isTrue();
        assertThat(serviceHearingValues.getFacilitiesRequired()).isEmpty();
        assertThat(serviceHearingValues.getListingComments())
            .isEqualToNormalizingNewlines("Appellant - Mr Fred Flintstone:\n" + NOTE_FROM_APPELLANT
                + "\n\n" + "party_role - Mr Barny Boulderstone:\n" + NOTE_FROM_OTHER_PARTY);
        assertThat(serviceHearingValues.getHearingRequester()).isNull();
        assertThat(serviceHearingValues.isPrivateHearingRequiredFlag()).isFalse();
        assertThat(serviceHearingValues.getLeadJudgeContractType()).isNull();
        assertThat(serviceHearingValues.getJudiciary()).isNotNull();
        assertThat(serviceHearingValues.isHearingIsLinkedFlag()).isFalse();
        assertThat(serviceHearingValues.getCaseFlags()).isEqualTo(getCaseFlags());
        assertThat(serviceHearingValues.getVocabulary()).isNull();
        assertThat(serviceHearingValues.getHearingChannels()).isEqualTo(List.of(FACE_TO_FACE));
        assertThat(serviceHearingValues.isCaseInterpreterRequiredFlag()).isTrue();
        assertThat(serviceHearingValues.getAdjournCaseInterpreterLanguage()).isEqualTo(BULGARIAN);
    }

    @Test
    void shouldMapPartiesInServiceHearingValues() throws InvalidMappingException {
        // given
        given(referenceDataServiceHolder.getVenueService()).willReturn(venueService);
        // when
        final ServiceHearingValues serviceHearingValues = ServiceHearingValuesMapping.mapServiceHearingValues(caseData, referenceDataServiceHolder);
        //then
        assertThat(serviceHearingValues.getParties())
            .hasSize(3)
            .anySatisfy(partyDetails -> {
                assertThat(partyDetails.getPartyID()).isEqualTo(APPELLANT_PARTY_ID.substring(0,15));
                assertThat(partyDetails.getPartyRole()).isEqualTo(EntityRoleCode.APPELLANT.getHmcReference());
            })
            .anySatisfy(partyDetails -> {
                assertThat(partyDetails.getPartyID()).isEqualTo(REPRESENTATIVE_PARTY_ID.substring(0,15));
                assertThat(partyDetails.getPartyRole()).isEqualTo(EntityRoleCode.REPRESENTATIVE.getHmcReference());
            })
            .anySatisfy(partyDetails -> {
                assertThat(partyDetails.getPartyID()).isEqualTo(OTHER_PARTY_ID.substring(0,15));
                assertThat(partyDetails.getPartyRole()).isEqualTo(EntityRoleCode.OTHER_PARTY.getHmcReference());
            });
    }

    @Test
    void shouldRepresentativeNotHaveOrganisation() throws InvalidMappingException {
        // given
        given(referenceDataServiceHolder.getVenueService()).willReturn(venueService);
        // when
        final ServiceHearingValues serviceHearingValues = ServiceHearingValuesMapping.mapServiceHearingValues(caseData, referenceDataServiceHolder);
        //then
        assertThat(serviceHearingValues.getParties())
            .filteredOn(partyDetails -> EntityRoleCode.REPRESENTATIVE.getHmcReference().equals(partyDetails.getPartyRole()))
            .extracting(PartyDetails::getPartyType)
            .containsOnly(INDIVIDUAL);
    }

    private List<Event> getEventsOfCaseData() {
        return new ArrayList<>() {
            {
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
                    .id(OTHER_PARTY_ID)
                    .name(Name.builder()
                         .firstName("Barny")
                         .lastName("Boulderstone")
                         .title("Mr")
                         .build())
                    .address(Address.builder().build())
                    .confidentialityRequired(NO)
                    .unacceptableCustomerBehaviour(YES)
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
                        .languages(BULGARIAN)
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
                    .sendNewOtherPartyNotification(NO)
                    .reasonableAdjustment(ReasonableAdjustmentDetails.builder()
                        .reasonableAdjustmentRequirements("Some adjustments...")
                        .wantsReasonableAdjustment(YES)
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
        return new ArrayList<>() {
            {
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
                add(PartyFlags.builder()
                    .partyName(null)
                    .flagParentId("2")
                    .flagId("70")
                    .flagDescription("Language Interpreter")
                    .flagStatus(null)
                    .build());
            }
        };
    }
}
