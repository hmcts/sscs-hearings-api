package uk.gov.hmcts.reform.sscs;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.sscs.ccd.domain.Address;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.BenefitType;
import uk.gov.hmcts.reform.sscs.ccd.domain.Contact;
import uk.gov.hmcts.reform.sscs.ccd.domain.DynamicList;
import uk.gov.hmcts.reform.sscs.ccd.domain.DynamicListItem;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.Identity;
import uk.gov.hmcts.reform.sscs.ccd.domain.MrnDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.SchedulingAndListingFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.Subscription;
import uk.gov.hmcts.reform.sscs.ccd.domain.Subscriptions;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.idam.IdamService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

import static uk.gov.hmcts.reform.sscs.ccd.domain.HearingRoute.LIST_ASSIST;
import static uk.gov.hmcts.reform.sscs.ccd.domain.State.READY_TO_LIST;
import static uk.gov.hmcts.reform.sscs.ccd.util.CaseDataUtils.YES;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("functional")
@Slf4j
public abstract class FunctionalTestBase {

    private static final String CREATED_BY_FUNCTIONAL_TEST = "created by functional test";

    @Autowired
    protected CcdService ccdService;
    @Autowired
    protected RestTemplate restTemplate;
    @Autowired
    protected IdamService idamService;

    @Value("http://localhost:${server.port}")
    protected String baseUrl;

    @BeforeEach
    void setUp() {
        log.info("Test running against base url: {}", baseUrl);
    }


    public SscsCaseDetails createCase() {
        String firstname = String.format("Func-%s",RandomStringUtils.random(5, true, false));
        String surname = String.format("Func-%s",RandomStringUtils.random(5, true, false));
        final SscsCaseData sscsCaseData = buildSscsCaseDataForTesting(firstname, surname, getRandomNino());
        sscsCaseData.getAppeal().getMrnDetails().setMrnDate(getRandomMrnDate().toString());
        return ccdService.createCase(sscsCaseData, EventType.CREATE_WITH_DWP_TEST_CASE.getCcdType(),
                                     CREATED_BY_FUNCTIONAL_TEST, CREATED_BY_FUNCTIONAL_TEST,
                                     idamService.getIdamTokens());
    }

    public LocalDate getRandomMrnDate() {
        long minDay = LocalDate.now().minusDays(1).toEpochDay();
        long maxDay = LocalDate.now().minusDays(28).toEpochDay();
        @SuppressWarnings("squid:S2245")
        long randomDay = ThreadLocalRandom.current().nextLong(maxDay, minDay);
        return LocalDate.ofEpochDay(randomDay);
    }

    public String getRandomNino() {
        return RandomStringUtils.random(9, true, true).toUpperCase();
    }

    public static void addFurtherEvidenceActionData(SscsCaseData testCaseData) {
        testCaseData.setInterlocReviewState(null);
        DynamicListItem value = new DynamicListItem("informationReceivedForInterlocJudge", "any");
        DynamicList furtherEvidenceActionList = new DynamicList(value, Collections.singletonList(value));
        testCaseData.setFurtherEvidenceAction(furtherEvidenceActionList);
    }

    public static SscsCaseData buildSscsCaseDataForTesting(final String firstname, final String surname, final String nino) {
        SscsCaseData testCaseData = buildCaseData(firstname, surname, nino);

        addFurtherEvidenceActionData(testCaseData);

        Subscriptions subscriptions = Subscriptions.builder()
            .appellantSubscription(Subscription.builder()
                .tya("app-appeal-number")
                .email("sscstest+notify@greencroftconsulting.com")
                .mobile("07398785050")
                .subscribeEmail(YES)
                .subscribeSms(YES)
                .reason("")
                .build())
            .representativeSubscription(Subscription.builder()
                .tya("rep-appeal-number")
                .email("sscstest+notify@greencroftconsulting.com")
                .mobile("07398785050")
                .subscribeEmail(YES)
                .subscribeSms(YES)
                .build())
            .appointeeSubscription(Subscription.builder()
                .tya("appointee-appeal-number")
                .email("sscstest+notify@greencroftconsulting.com")
                .mobile("07398785050")
                .subscribeEmail(YES)
                .subscribeSms(YES)
                .reason("")
                .build())
            .build();

        testCaseData.setSubscriptions(subscriptions);
        return testCaseData;
    }

    public static SscsCaseData buildCaseData(final String firstname, final String surname, final String nino) {
        return SscsCaseData.builder()
            .caseReference("SC068/17/00013")
            .caseCreated(LocalDate.now().toString())
            .appeal(Appeal.builder()
                .appellant(Appellant.builder()
                    .name(Name.builder()
                        .title("Mr")
                        .firstName(firstname)
                        .lastName(surname)
                        .build())
                    .address(Address.builder()
                        .line1("123 Hairy Lane")
                        .line2("Off Hairy Park")
                        .town("Hairyfield")
                        .county("Kent")
                        .postcode("CF24 3GP")
                        .build())
                    .contact(Contact.builder()
                        .email("mail@email.com")
                        .phone("01234567890")
                        .mobile("01234567890")
                        .build())
                    .identity(Identity.builder()
                        .dob("1904-03-10")
                        .nino(nino)
                        .build())
                    .build())
                .benefitType(BenefitType.builder()
                    .code("PIP")
                    .build())
                .hearingOptions(HearingOptions.builder()
                    .wantsToAttend(YES)
                    .languageInterpreter(YES)
                    .languages("Spanish")
                    .signLanguageType("A sign language")
                    .arrangements(Arrays.asList("hearingLoop", "signLanguageInterpreter"))
                    .other("Yes, this...")
                    .build())
                .mrnDetails(MrnDetails.builder()
                    .mrnDate("2018-06-29")
                    .dwpIssuingOffice("1")
                    .mrnLateReason("Lost my paperwork")
                    .build())
                .signer("Signer")
                .hearingType("oral")
                .receivedVia("Online")
                .build())
            .subscriptions(Subscriptions.builder()
                .appellantSubscription(Subscription.builder()
                    .tya("app-appeal-number")
                    .email("appellant@email.com")
                    .mobile("")
                    .subscribeEmail(YES)
                    .subscribeSms(YES)
                    .reason("")
                    .build())
                .build())
            .region("CARDIFF")
            .createdInGapsFrom(READY_TO_LIST.getId())
            .schedulingAndListingFields(SchedulingAndListingFields.builder()
                .hearingRoute(LIST_ASSIST)
                .build())
            .build();
    }
}
