package uk.gov.hmcts.reform.sscs.mappers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.servicehearingvalues.ShvCaseFlags;
import uk.gov.hmcts.reform.sscs.model.servicehearingvalues.ShvHearingWindow;
import uk.gov.hmcts.reform.sscs.model.servicehearingvalues.ShvHearingWindowDateRange;
import uk.gov.hmcts.reform.sscs.model.servicehearingvalues.ShvPartyDetails;
import uk.gov.hmcts.reform.sscs.model.servicehearingvalues.ShvPartyFlags;
import uk.gov.hmcts.reform.sscs.model.servicehearingvalues.UnavailabilityRange;
import uk.gov.hmcts.reform.sscs.model.single.hearing.*;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RelatedParty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServiceHearingValuesMapperTest {

    private static final ServiceHearingValuesMapper mapper = new ServiceHearingValuesMapper();
    private static SscsCaseDetails sscsCaseDetails;

    private static final String NOTE_FROM_OTHER_PARTY = "Yes, this is from other party";
    private static final String NOTE_FROM_OTHER_APPELLANT = "Yes, this is from appellant";
    public static final String FACE_TO_FACE = "faceToFace";

    @BeforeEach
    public void setUp() {
        this.sscsCaseDetails = SscsCaseDetails.builder()
            .data(SscsCaseData.builder()
                      .ccdCaseId("1234")
                      .benefitCode("001")
                      .issueCode("DD")
                      .urgentCase("Yes")
                      .caseManagementLocation(CaseManagementLocation.builder()
                                                  .baseLocation("Location-1")
                                                  .build())
                      .adjournCaseCanCaseBeListedRightAway("Yes")
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
                                                      .languages("Telugu")
                                                      .signLanguageType("Sign language")
                                                      .arrangements(Arrays.asList(
                                                          "signLanguageInterpreter",
                                                          "hearingLoop",
                                                          "disabledAccess"
                                                      ))
                                                      .scheduleHearing("No")
                                                      .excludeDates(getExcludeDates())
                                                      .agreeLessNotice("No")
                                                      .other(NOTE_FROM_OTHER_APPELLANT)
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
                      .build())
            .build();
    }

    @Test
    void shouldMapServiceHearingValuesSuccessfully() {
        // given
        SscsCaseData sscsCaseData = sscsCaseDetails.getData();

        // when
        ServiceHearingValues serviceHearingValues = mapper.mapServiceHearingValues(sscsCaseDetails);

        final ShvHearingWindow expectedHearingWindow = ShvHearingWindow.builder()
            .hearingWindowFirstDate(null)
            .shvHearingWindowDateRange(ShvHearingWindowDateRange.builder()
                                        .hearingWindowStartDateRange("2022-02-26")
                                        .hearingWindowEndDateRange(null)
                                        .build())
            .build();

        //then
        assertEquals(
            serviceHearingValues.getCaseName(),
            sscsCaseData.getAppeal().getAppellant().getName().getFullName()
        );
        assertEquals(serviceHearingValues.isAutoListFlag(), false); // TODO
        assertEquals(serviceHearingValues.getDuration(), 0); // TODO
        assertEquals(serviceHearingValues.getHearingType(), sscsCaseData.getAppeal().getHearingType());
        assertEquals(serviceHearingValues.getCaseType(), sscsCaseData.getBenefitCode());
        assertEquals(String.join("", serviceHearingValues.getCaseSubTypes()), sscsCaseData.getIssueCode());
        assertEquals(serviceHearingValues.getShvHearingWindow(), expectedHearingWindow);

        assertEquals(serviceHearingValues.getHearingPriorityType(), HearingPriorityType.HIGH.getType());
        assertEquals(serviceHearingValues.getNumberOfPhysicalAttendees(), 3);  //TODO
        assertEquals(serviceHearingValues.isHearingInWelshFlag(), false);
        assertEquals(serviceHearingValues.getHearingLocations(), getHearingLocations());
        assertEquals(serviceHearingValues.getCaseAdditionalSecurityFlag(), true);
        assertEquals(serviceHearingValues.getFacilitiesRequired(), Arrays.asList(
            "signLanguageInterpreter",
            "hearingLoop",
            "disabledAccess"
        ));
        assertEquals(
            serviceHearingValues.getListingComments(),
            NOTE_FROM_OTHER_APPELLANT + "\n" + NOTE_FROM_OTHER_PARTY
        );
        assertEquals(serviceHearingValues.getHearingRequester(), null);
        assertEquals(serviceHearingValues.isPrivateHearingRequiredFlag(), false);
        assertEquals(serviceHearingValues.getLeadJudgeContractType(), null); // TODO
        assertEquals(serviceHearingValues.getShvJudiciary(), null); // TODO
        assertEquals(serviceHearingValues.isHearingIsLinkedFlag(), false);
        assertEquals(serviceHearingValues.getShvParties(), getParties()); // TODO
        assertEquals(serviceHearingValues.getShvCaseFlags(), getCaseFlags());
        assertEquals(serviceHearingValues.getShvVocabulary(), null);
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

    private List<HearingLocations> getHearingLocations() {
        return new ArrayList<>() {
            {
                add(HearingLocations.builder()
                        .locationId("Location-1")
                        .locationType(LocationType.COURT.getLocationLabel())
                        .build());
            }
        };
    }


    private List<CcdValue<OtherParty>> getOtherParties() {
        return new ArrayList<CcdValue<OtherParty>>() {
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
                                                       .languages("Telugu")
                                                       .scheduleHearing("No")
                                                       .excludeDates(getExcludeDates())
                                                       .agreeLessNotice("No")
                                                       .other(NOTE_FROM_OTHER_PARTY)
                                                       .build())
                                   .isAppointee("No")
                                   .appointee(Appointee.builder().build())
                                   .rep(Representative.builder().build())
                                   .reasonableAdjustment(ReasonableAdjustmentDetails.builder()
                                                             .reasonableAdjustmentRequirements("Some adjustments...")
                                                             .wantsReasonableAdjustment(YesNo.YES)
                                                             .build())
                                   .role(Role.builder()
                                             .name("party_role")
                                             .description("description")
                                             .build())
                                   .build()));
            }
        };
    }


    private List<ShvPartyDetails> getParties() {
        return new ArrayList<>() {
            {
                add(ShvPartyDetails.builder()
                        .partyID("party_id_1")
                        .partyType(PartyType.IND)
                        .partyName("Mr Barny Boulderstone")
                        .partyChannel(FACE_TO_FACE)
                        .partyRole("party_role")
                        .individualDetails(getIndividualDetails())
                        .organisationDetails(OrganisationDetails.builder().build())
                        .unavailabilityDow(null)
                        .unavailabilityRanges(getUnavailabilityRanges())
                        .build());
            }
        };
    }

    private List<UnavailabilityRange> getUnavailabilityRanges() {
        return new ArrayList<>() {
            {
                add(UnavailabilityRange.builder()
                    .unavailableFromDate("12/01/2022")
                    .unavailableToDate("19/01/2022")
                    .build());
            }
        };
    }

    private IndividualDetails getIndividualDetails() {
        return IndividualDetails.builder()
            .title("Mr")
            .firstName("Barny")
            .lastName("Boulderstone")
            .preferredHearingChannel(FACE_TO_FACE)
            .interpreterLanguage("Telugu")
            .reasonableAdjustments(Arrays.asList("Some adjustments..."))
            .vulnerableFlag(false)
            .vulnerabilityDetails(null)
            .hearingChannelEmail("test2@gmail.com")
            .hearingChannelPhone("0999733735")
            // TODO Field below would be populated when the corresponding method is finished
            //  (SSCS-10321-Create-Hearing-POST-Mapping)
            .relatedParties(getRelatedParties())
            .build();
    }

    private List<RelatedParty> getRelatedParties() {
        return new ArrayList<>() {
            {
                /*add(RelatedParties.builder()
                    .relationshipType("Relative")
                    .relatedPartyID("1")
                    .build());*/
            }
        };
    }

    private List<ExcludeDate> getExcludeDates() {
        return new ArrayList<>() {
            {
                add(ExcludeDate.builder()
                    .value(DateRange.builder()
                               .start("12/01/2022")
                               .end("19/01/2022")
                               .build())
                    .build());
            }
        };
    }

    // TODO it will be populated when the method is provided
    private ShvCaseFlags getCaseFlags() {
        return ShvCaseFlags.builder()
            .shvFlags(getPartyFlags())
            .flagAmendUrl(null)
            .build();
    }

    private List<ShvPartyFlags> getPartyFlags() {
        return new ArrayList<>() {
            {
                add(ShvPartyFlags.builder()
                    .partyName(null)
                    .flagParentId("10")
                    .flagId("44")
                    .flagDescription("Sign Language Interpreter")
                    .flagStatus(null)
                    .build());
                add(ShvPartyFlags.builder()
                    .partyName(null)
                    .flagParentId("6")
                    .flagId("21")
                    .flagDescription("Step free / wheelchair access")
                    .flagStatus(null)
                    .build());
                add(ShvPartyFlags.builder()
                    .partyName(null)
                    .flagParentId("11")
                    .flagId("45")
                    .flagDescription("Hearing loop (hearing enhancement system)")
                    .flagStatus(null)
                    .build());
                add(ShvPartyFlags.builder()
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
