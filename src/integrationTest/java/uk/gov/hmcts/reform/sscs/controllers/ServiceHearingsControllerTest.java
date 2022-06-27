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
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseManagementLocation;
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
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingChannelMapping;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingsPartiesMapping;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.model.service.ServiceHearingRequest;
import uk.gov.hmcts.reform.sscs.model.service.linkedcases.ServiceLinkedCases;
import uk.gov.hmcts.reform.sscs.reference.data.model.EntityRoleCode;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingDuration;
import uk.gov.hmcts.reform.sscs.reference.data.model.SessionCategoryMap;
import uk.gov.hmcts.reform.sscs.reference.data.service.HearingDurationsService;
import uk.gov.hmcts.reform.sscs.reference.data.service.SessionCategoryMapService;
import uk.gov.hmcts.reform.sscs.service.VenueService;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.PartyType.INDIVIDUAL;
import static uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel.FACE_TO_FACE;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("integration")
class ServiceHearingsControllerTest {

    private static final long CASE_ID = 1625080769409918L;
    private static final long CASE_ID_LINKED = 3456385374124L;
    private static final long MISSING_CASE_ID = 99250807409918L;
    private static final long HEARING_ID = 123L;
    private static final String SERVICE_HEARING_VALUES_URL = "/serviceHearingValues";
    private static final String SERVICE_LINKED_CASES_URL = "/serviceLinkedCases";
    private static final String CASE_NAME = "Test Case Name";

    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public static final String PROCESSING_VENUE = "Liverpool";
    public static final String BASE_LOCATION = "12345";

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
    public SessionCategoryMapService sessionCategoryMaps;

    @Mock
    public HearingDurationsService hearingDurations;

    @Mock
    private VenueService venueService;


    static MockedStatic<HearingsPartiesMapping> hearingsPartiesMapping;

    static MockedStatic<HearingChannelMapping> hearingChannelMapping;

    @BeforeAll
    public static void init() {
        hearingsPartiesMapping = Mockito.mockStatic(HearingsPartiesMapping.class);
        hearingChannelMapping = Mockito.mockStatic(HearingChannelMapping.class);
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

        Appeal appeal = mock(Appeal.class);
        Appellant appellant = mock(Appellant.class);
        when(appeal.getAppellant()).thenReturn(appellant);
        Representative representative = mock(Representative.class);
        when(appeal.getRep()).thenReturn(representative);
        OtherParty otherParty = mock(OtherParty.class);
        CcdValue<OtherParty> otherPartyCcdValue = new CcdValue<>(otherParty);
        List<CcdValue<OtherParty>> otherParties = new ArrayList<>();
        otherParties.add(otherPartyCcdValue);
        HearingSubtype hearingSubtype = Mockito.mock(HearingSubtype.class);
        when(hearingSubtype.getHearingVideoEmail()).thenReturn("test2@gmail.com");
        when(hearingSubtype.getHearingTelephoneNumber()).thenReturn("0999733735");
        when(otherParty.getHearingSubtype()).thenReturn(hearingSubtype);
        when(appeal.getHearingSubtype()).thenReturn(hearingSubtype);
        HearingOptions hearingOptions = Mockito.mock(HearingOptions.class);

        hearingsPartiesMapping.when(() -> HearingsPartiesMapping.getPartyId(appellant)).thenReturn("1");
        hearingsPartiesMapping.when(() -> HearingsPartiesMapping.getPartyId(representative)).thenReturn("1");
        hearingsPartiesMapping.when(() -> HearingsPartiesMapping.getPartyId(otherParty)).thenReturn("1");
        hearingsPartiesMapping.when(() -> HearingsPartiesMapping.getPartyType(appellant)).thenReturn(INDIVIDUAL);
        hearingsPartiesMapping.when(() -> HearingsPartiesMapping.getPartyRole(any(Appellant.class))).thenReturn(
            EntityRoleCode.APPELLANT.getHmcReference());
        hearingsPartiesMapping.when(() -> HearingsPartiesMapping.getPartyRole(any(Representative.class)))
            .thenReturn(EntityRoleCode.REPRESENTATIVE.getHmcReference());
        hearingsPartiesMapping.when(() -> HearingsPartiesMapping.getPartyRole(any(OtherParty.class)))
            .thenReturn(EntityRoleCode.OTHER_PARTY.getHmcReference());
        hearingsPartiesMapping.when(() -> HearingsPartiesMapping.getIndividualInterpreterLanguage(hearingOptions,
            referenceDataServiceHolder)).thenReturn("bul");
        hearingsPartiesMapping.when(() -> HearingsPartiesMapping.getIndividualFirstName(otherParty)).thenReturn("Barny");
        hearingsPartiesMapping.when(() -> HearingsPartiesMapping.getIndividualLastName(otherParty)).thenReturn("Boulderstone");
        hearingChannelMapping.when(() -> HearingChannelMapping.getIndividualPreferredHearingChannel(
            hearingSubtype, hearingOptions)).thenReturn(FACE_TO_FACE);
        when(otherParty.getHearingOptions()).thenReturn(hearingOptions);
        when(appeal.getHearingOptions()).thenReturn(hearingOptions);
        SscsCaseData sscsCaseData = Mockito.mock(SscsCaseData.class);
        when(sscsCaseData.getCaseAccessManagementFields()).thenReturn(CaseAccessManagementFields.builder()
                .caseNamePublic(CASE_NAME)
                .build());
        when(sscsCaseData.getProcessingVenue()).thenReturn(PROCESSING_VENUE);
        when(sscsCaseData.getLinkedCase()).thenReturn(linkedCases);
        when(sscsCaseData.getAppeal()).thenReturn(appeal);
        when(sscsCaseData.getCcdCaseId()).thenReturn(String.valueOf(CASE_ID));
        SscsIndustrialInjuriesData sscsIndustrialInjuriesData = mock(SscsIndustrialInjuriesData.class);
        when(sscsIndustrialInjuriesData.getSecondPanelDoctorSpecialism()).thenReturn("");
        when(sscsCaseData.getSscsIndustrialInjuriesData()).thenReturn(sscsIndustrialInjuriesData);
        when(sscsCaseData.getIsFqpmRequired()).thenReturn(YesNo.NO);
        CaseManagementLocation caseManagementLocation = CaseManagementLocation.builder().baseLocation(BASE_LOCATION).build();
        JointParty jointParty = mock(JointParty.class);
        when(jointParty.getHasJointParty()).thenReturn(YesNo.NO);
        when(sscsCaseData.getJointParty()).thenReturn(jointParty);
        when(sscsCaseData.getCaseManagementLocation()).thenReturn(caseManagementLocation);
        SscsCaseDetails caseDetails = SscsCaseDetails.builder()
                .data(sscsCaseData)
                .build();
        given(ccdService.updateCase(eq(sscsCaseData), eq(CASE_ID), anyString(), anyString(), anyString(), any(IdamTokens.class)))
            .willReturn(caseDetails);
        given(ccdService.getByCaseId(eq(CASE_ID), any(IdamTokens.class))).willReturn(caseDetails);
        given(authTokenGenerator.generate()).willReturn("s2s token");
        given(idamApiService.getIdamTokens()).willReturn(IdamTokens.builder().build());
        hearingChannelMapping.when(() -> HearingChannelMapping.getHearingChannelsHmcReference(sscsCaseData))
            .thenReturn(List.of(FACE_TO_FACE.getHmcReference()));

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
        List<String> reasonsforLink = Collections.emptyList();
        List<ServiceLinkedCases> serviceLinkedCases = new ArrayList<>();

        serviceLinkedCases.add(ServiceLinkedCases.builder()
                .caseReference(String.valueOf(CASE_ID_LINKED))
                .caseName(CASE_NAME)
                .reasonsForLink(reasonsforLink)
                .build());

        String json = asJsonString(serviceLinkedCases);

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
