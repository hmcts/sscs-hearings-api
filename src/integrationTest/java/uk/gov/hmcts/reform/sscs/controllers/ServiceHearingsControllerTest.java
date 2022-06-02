package uk.gov.hmcts.reform.sscs.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sscs.ResourceLoader;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.BenefitCode;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseAccessManagementFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseLink;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseLinkDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.CcdValue;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingSubtype;
import uk.gov.hmcts.reform.sscs.ccd.domain.Issue;
import uk.gov.hmcts.reform.sscs.ccd.domain.JointParty;
import uk.gov.hmcts.reform.sscs.ccd.domain.OtherParty;
import uk.gov.hmcts.reform.sscs.ccd.domain.Representative;
import uk.gov.hmcts.reform.sscs.ccd.domain.SessionCategory;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsIndustrialInjuriesData;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingsPartiesMapping;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.model.service.ServiceHearingRequest;
import uk.gov.hmcts.reform.sscs.model.service.linkedcases.LinkedCase;
import uk.gov.hmcts.reform.sscs.model.service.linkedcases.ServiceLinkedCases;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingDuration;
import uk.gov.hmcts.reform.sscs.reference.data.model.SessionCategoryMap;
import uk.gov.hmcts.reform.sscs.reference.data.service.HearingDurationsService;
import uk.gov.hmcts.reform.sscs.reference.data.service.SessionCategoryMapService;
import uk.gov.hmcts.reform.sscs.service.ReferenceDataServiceHolder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel.FACE_TO_FACE;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("integration")
class ServiceHearingsControllerTest {

    private static final long CASE_ID = 1625080769409918L;
    private static final long CASE_ID_LINKED = 3456385374124L;
    private static final long MISSING_CASE_ID = 99250807409918L;
    private static final String BAD_CASE_ID = "ABCASDEF";
    private static final long HEARING_ID = 123L;
    private static final String SERVICE_HEARING_VALUES_URL = "/serviceHearingValues";
    private static final String SERVICE_LINKED_CASES_URL = "/serviceLinkedCases";
    private static final String CASE_NAME = "Test Case Name";
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    public static final String PROCESSING_VENUE = "Liverpool";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IdamService idamApiService;


    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private CcdService ccdService;

    @MockBean
    private ReferenceDataServiceHolder referenceDataServiceHolder;

    @Mock
    private SessionCategoryMapService sessionCategoryMaps;

    @Mock
    private HearingDurationsService hearingDurations;

    @Mock
    private VenueService venueService;

    static MockedStatic<HearingsPartiesMapping> hearingsPartiesMapping;

    @BeforeAll
    public static void init() {
        hearingsPartiesMapping = Mockito.mockStatic(HearingsPartiesMapping.class);
    }

    @AfterAll
    public static void close() {
        hearingsPartiesMapping.close();
    }

    @BeforeEach
    void setUp()  {
        List<CaseLink> linkedCases = new ArrayList<>();
        linkedCases.add(CaseLink.builder()
                .value(CaseLinkDetails.builder()
                        .caseReference(String.valueOf(CASE_ID_LINKED))
                        .build())
                .build());

        HearingOptions hearingOptions = HearingOptions.builder().build();
        HearingSubtype hearingSubtype = HearingSubtype.builder()
            .hearingVideoEmail("test2@gmail.com")
            .hearingTelephoneNumber("0999733735")
            .build();

        Representative representative = Representative.builder().id("2").build();
        Appellant appellant = Appellant.builder().id("1").build();

        OtherParty otherParty = OtherParty.builder().id("3")
            .hearingSubtype(hearingSubtype)
            .hearingOptions(hearingOptions)
            .build();

        CcdValue<OtherParty> otherPartyCcdValue = new CcdValue<>(otherParty);
        List<CcdValue<OtherParty>> otherParties = new ArrayList<>();
        otherParties.add(otherPartyCcdValue);

        Appeal appeal = Appeal.builder().rep(representative).hearingSubtype(hearingSubtype).hearingOptions(hearingOptions)
            .appellant(appellant).build();


        hearingsPartiesMapping.when(() -> HearingsPartiesMapping.getIndividualInterpreterLanguage(hearingOptions))
            .thenReturn(Optional.of("Telugu"));
        hearingsPartiesMapping.when(() -> HearingsPartiesMapping.getIndividualFirstName(otherParty)).thenReturn("Barny");
        hearingsPartiesMapping.when(() -> HearingsPartiesMapping.getIndividualLastName(otherParty)).thenReturn("Boulderstone");
        hearingsPartiesMapping.when(() -> HearingsPartiesMapping.getIndividualPreferredHearingChannel(appeal.getHearingType(), hearingSubtype))
            .thenReturn(Optional.ofNullable(FACE_TO_FACE.getHmcReference()));

        SscsCaseData sscsCaseData = SscsCaseData.builder()
            .caseAccessManagementFields(CaseAccessManagementFields.builder()
                .caseNamePublic(CASE_NAME)
                .build())
            .linkedCase(linkedCases)
            .appeal(appeal)
            .ccdCaseId(String.valueOf(CASE_ID))
            .sscsIndustrialInjuriesData(SscsIndustrialInjuriesData.builder()
                .secondPanelDoctorSpecialism("")
                .build())
            .isFqpmRequired(YesNo.NO)
            .jointParty(JointParty.builder()
                .hasJointParty(YesNo.NO)
                .build())
            .processingVenue(PROCESSING_VENUE)
            .build();

        SscsCaseDetails caseDetails = SscsCaseDetails.builder()
                .data(sscsCaseData)
                .build();
        given(ccdService.updateCase(eq(sscsCaseData), eq(CASE_ID), anyString(), anyString(), anyString(), any(IdamTokens.class))).willReturn(caseDetails);
        given(ccdService.getByCaseId(eq(CASE_ID), any(IdamTokens.class))).willReturn(caseDetails);
        given(authTokenGenerator.generate()).willReturn("s2s token");
        given(idamApiService.getIdamTokens()).willReturn(IdamTokens.builder().build());

        SessionCategoryMap sessionCategoryMap = new SessionCategoryMap(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                false, false, SessionCategory.CATEGORY_06, null);

        given(sessionCategoryMaps.getSessionCategory(anyString(), anyString(),anyBoolean(),anyBoolean()))
                .willReturn(sessionCategoryMap);
        given(sessionCategoryMaps.getCategoryTypeValue(sessionCategoryMap))
                .willReturn("BBA3-002");
        given(sessionCategoryMaps.getCategorySubTypeValue(sessionCategoryMap))
                .willReturn("BBA3-002-DD");
        given(hearingDurations.getHearingDuration(anyString(),anyString()))
                .willReturn(new HearingDuration(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                        60,75,30));

        given(venueService.getEpimsIdForVenue(PROCESSING_VENUE))
            .willReturn(Optional.of("LIVERPOOL SOCIAL SECURITY AND CHILD SUPPORT TRIBUNAL"));

        given(referenceDataServiceHolder.getHearingDurations()).willReturn(hearingDurations);
        given(referenceDataServiceHolder.getSessionCategoryMaps()).willReturn(sessionCategoryMaps);
        given(referenceDataServiceHolder.getVenueService()).willReturn(venueService);
    }

    // TODO These are holder tests that will need to be implemented alongside service hearing controller

    @DisplayName("When Authorization and Case ID valid "
            + "should return the case name with a with 200 response code")
    @Test
    void testPostRequestServiceHearingValues() throws Exception {
        ServiceHearingRequest request = ServiceHearingRequest.builder()
                .caseId(String.valueOf(CASE_ID))
                .build();

        String actualJson = ResourceLoader.loadJson("serviceHearingValuesForControllerTest.json");
        actualJson = replaceMockValues(actualJson);

        mockMvc.perform(post(SERVICE_HEARING_VALUES_URL)
                        .contentType(APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(actualJson));
    }

    @DisplayName("When Case Reference is Invalid should return a with 400 response code")
    @Test
    void testPostRequestServiceHearingValues_badCaseID() throws Exception {
        ServiceHearingRequest request = ServiceHearingRequest.builder()
                .caseId(BAD_CASE_ID)
                .build();

        mockMvc.perform(post(SERVICE_HEARING_VALUES_URL)
                        .contentType(APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("When Case Not Found should return a with 404 response code")
    @Test
    void testPostRequestServiceHearingValues_missingCase() throws Exception {
        ServiceHearingRequest request = ServiceHearingRequest.builder()
                .caseId(String.valueOf(MISSING_CASE_ID))
                .build();

        mockMvc.perform(post(SERVICE_HEARING_VALUES_URL)
                        .contentType(APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @DisplayName("When Authorization and Case ID valid should return the case name with a with 200 response code")
    @Test
    void testPostRequestServiceLinkedCases() throws Exception {
        List<LinkedCase> linkedCases = new ArrayList<>();
        linkedCases.add(LinkedCase.builder().ccdCaseId(String.valueOf(CASE_ID_LINKED)).build());
        ServiceLinkedCases model = ServiceLinkedCases.builder().linkedCases(linkedCases).build();
        String json = asJsonString(model);

        ServiceHearingRequest request = ServiceHearingRequest.builder()
                .caseId(String.valueOf(CASE_ID))
                .hearingId(String.valueOf(HEARING_ID))
                .build();

        mockMvc.perform(post(SERVICE_LINKED_CASES_URL)
                        .contentType(APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(json));
    }

    @DisplayName("When Case Reference is Invalid should return a with 400 response code")
    @Test
    void testPostRequestServiceLinkedCases_badCaseID() throws Exception {
        ServiceHearingRequest request = ServiceHearingRequest.builder()
                .caseId(BAD_CASE_ID)
                .hearingId(String.valueOf(HEARING_ID))
                .build();

        mockMvc.perform(post(SERVICE_LINKED_CASES_URL)
                        .contentType(APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testPostRequestServiceLinkedCases_missingCase() throws Exception {
        ServiceHearingRequest request = ServiceHearingRequest.builder()
                .caseId(String.valueOf(MISSING_CASE_ID))
                .hearingId(String.valueOf(HEARING_ID))
                .build();

        mockMvc.perform(post(SERVICE_LINKED_CASES_URL)
                        .contentType(APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    public static String asJsonString(final Object obj) throws JsonProcessingException {
        return mapper.writeValueAsString(obj);
    }

    @NotNull
    private static String replaceMockValues(String actualJson) {
        String dateTomorrow = LocalDate.now().plusDays(1).toString();
        return actualJson.replace("MOCK_DATE_TOMORROW", dateTomorrow);
    }
}
