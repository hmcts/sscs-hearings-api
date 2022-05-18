package uk.gov.hmcts.reform.sscs.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.model.service.ServiceHearingRequest;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.ServiceHearingValues;
import uk.gov.hmcts.reform.sscs.model.service.linkedcases.LinkedCase;
import uk.gov.hmcts.reform.sscs.model.service.linkedcases.ServiceLinkedCases;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    private static final String CASE_NAME = "";

    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IdamService idamApiService;


    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private CcdService ccdService;

    @BeforeEach
    void setUp()  {
        List<CaseLink> linkedCases = new ArrayList<>();
        linkedCases.add(CaseLink.builder()
                .value(CaseLinkDetails.builder()
                        .caseReference(String.valueOf(CASE_ID_LINKED))
                        .build())
                .build());
        Appeal appeal = Appeal.builder().build();
        SscsCaseDetails caseDetails = SscsCaseDetails.builder()
                .data(SscsCaseData.builder()
                        .workAllocationFields(WorkAllocationFields.builder()
                                .caseNamePublic(CASE_NAME)
                                .build())
                        .linkedCase(linkedCases)
                        .appeal(appeal)
                        .build())
                .build();
        given(ccdService.getByCaseId(eq(CASE_ID), any(IdamTokens.class))).willReturn(caseDetails);
        given(authTokenGenerator.generate()).willReturn("s2s token");
        given(idamApiService.getIdamTokens()).willReturn(IdamTokens.builder().build());
    }

    // TODO These are holder tests that will need to be implemented alongside service hearing controller

    @DisplayName("When Authorization and Case ID valid "
            + "should return the case name with a with 200 response code")
    @Test
    void testPostRequestServiceHearingValues() throws Exception {
        ServiceHearingValues model = ServiceHearingValues.builder().caseName(CASE_NAME).build();
        String json = asJsonString(model);

        ServiceHearingRequest request = ServiceHearingRequest.builder()
                .caseId(String.valueOf(CASE_ID))
                .build();

        mockMvc.perform(post(SERVICE_HEARING_VALUES_URL)
                        .contentType(APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(json));
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
}
