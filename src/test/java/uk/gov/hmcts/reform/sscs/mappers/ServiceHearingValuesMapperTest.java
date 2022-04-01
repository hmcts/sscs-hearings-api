package uk.gov.hmcts.reform.sscs.mappers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.model.servicehearingvalues.*;
import uk.gov.hmcts.reform.sscs.model.single.hearing.IndividualDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.OrganisationDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PartyType;
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
                      //TODO hearing window
                      //TODO hearing duration
                      .urgentCase("Yes")
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
    void shouldMapServiceHearingValuesSuccessfully(){
        // given
        SscsCaseData sscsCaseData = sscsCaseDetails.getData();

        // when
        ServiceHearingValues serviceHearingValues = mapper.mapServiceHearingValues(sscsCaseDetails);

        System.out.println(serviceHearingValues.getHearingWindow());
        System.out.println(serviceHearingValues.getCaseType());
        System.out.println(serviceHearingValues.getListingComments());
        System.out.println(sscsCaseData);

        HearingWindow expectedHearingWindow = HearingWindow.builder()
            .hearingWindowFirstDate(null)
            .hearingWindowDateRange(HearingWindowDateRange.builder()
                                        .hearingWindowStartDateRange("2022-02-26")
                                        .hearingWindowEndDateRange(null)
                                        .build())
            .build();

        //then
        assertEquals(serviceHearingValues.getCaseName(), sscsCaseData.getAppeal().getAppellant().getName().getFullName());
        //assertEquals(serviceHearingValues.isAutoListFlag(), YesNo.isYes(sscsCaseData.getAutoListFlag()));
        assertEquals(serviceHearingValues.getHearingType(), sscsCaseData.getAppeal().getHearingType());
        assertEquals(serviceHearingValues.getCaseType(), sscsCaseData.getBenefitCode());
        assertEquals(String.join("", serviceHearingValues.getCaseSubTypes()), sscsCaseData.getIssueCode());
        assertEquals(serviceHearingValues.getHearingWindow(), expectedHearingWindow); // TODO

        assertEquals(serviceHearingValues.getHearingPriorityType(), HearingPriorityType.HIGH.getType());
        assertEquals(serviceHearingValues.getNumberOfPhysicalAttendees(), 3);  //TODO
        assertEquals(serviceHearingValues.isHearingInWelshFlag(), false);
        assertEquals(serviceHearingValues.getHearingLocations().size(), 0); // TODO
        assertEquals(serviceHearingValues.getCaseAdditionalSecurityFlag(), true);
        assertEquals(serviceHearingValues.getFacilitiesRequired(), Arrays.asList("signLanguageInterpreter",
                                                                                 "hearingLoop",
                                                                                 "disabledAccess"
        ));
        assertEquals(serviceHearingValues.getListingComments(),  NOTE_FROM_OTHER_APPELLANT + "\n" + NOTE_FROM_OTHER_PARTY);
        assertEquals(serviceHearingValues.getHearingRequester(),  null);
        assertEquals(serviceHearingValues.isPrivateHearingRequiredFlag(),  false);
        assertEquals(serviceHearingValues.getLeadJudgeContractType(),  null); // TODO
        assertEquals(serviceHearingValues.getJudiciary(), null); // TODO
        assertEquals(serviceHearingValues.isHearingIsLinkedFlag(), false);
        assertEquals(serviceHearingValues.getParties().size(), getParties().size()); // TODO
        assertEquals(serviceHearingValues.getParties(), getParties()); // TODO
        assertEquals(serviceHearingValues.getCaseFlags(), getCaseFlags()); // TODO
        assertEquals(serviceHearingValues.getScreenFlow(), null);
        assertEquals(serviceHearingValues.getVocabulary(), null);
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
        }};
    }


    private List<CcdValue<OtherParty>> getOtherParties() {
        CcdValue<OtherParty> ccdValue = new CcdValue<>(OtherParty.builder()
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
                                                           .build());
        return new ArrayList<CcdValue<OtherParty>>(){{
            add(ccdValue);
        }};
    }



    private List<PartyDetails> getParties() {
        return new ArrayList<>() {{
            add(PartyDetails.builder()
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
        }};
    }

    private List<UnavailabilityRange> getUnavailabilityRanges() {
        return new ArrayList<>() {{
            add(UnavailabilityRange.builder()
                    .unavailableFromDate("12/01/2022")
                    .unavailableToDate("19/01/2022")
                    .build());
        }};

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
            .relatedParties(getRelatedParties()) // TODO this field would be populated when the corresponding method is finished
            .build();
    }

    private List<RelatedParty> getRelatedParties() {
        return new ArrayList<>() {{
            /*add(RelatedParties.builder()
                    .relationshipType("Relative")
                    .relatedPartyID("1")
                    .build());*/
        }};
    }


   /* private List<RelatedParty> getRelatedPartyList() {
        return new ArrayList<>() {{
            add(RelatedParty.builder()
                    .relationshipType("Relative")
                    .relatedPartyId("1")
                    .build());
        }};
    }*/


    private List<ExcludeDate> getExcludeDates() {
        return new ArrayList<>() {{
            add(ExcludeDate.builder()
                    .value(DateRange.builder()
                               .start("12/01/2022")
                               .end("19/01/2022")
                               .build())
                    .build());
        }};
    }
    // TODO it will be populated when the method is provided
    private CaseFlags getCaseFlags() {
        return CaseFlags.builder()
                //.flags(getPartyFlags())
                //.flagAmendUrl("")
                .build();
    }

    private List<PartyFlags> getPartyFlags() {
        return new ArrayList<>() {{
            add(PartyFlags.builder()
                    .partyName("Mr Barny Boulderstone")
                    .flagParentId("")
                    .flagId("")
                    .flagDescription("")
                    .flagStatus("")
                    .build());
        }};
    }
}
