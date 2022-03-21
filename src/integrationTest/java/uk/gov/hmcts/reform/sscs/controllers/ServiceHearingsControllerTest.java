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
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.WorkAllocationFields;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
import uk.gov.hmcts.reform.sscs.exception.AuthorisationException;
import uk.gov.hmcts.reform.sscs.exception.InvalidHeaderException;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.model.ServiceHearingValues;
import uk.gov.hmcts.reform.sscs.service.AuthorisationService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.sscs.service.AuthorisationService.SERVICE_AUTHORISATION_HEADER;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("integration")
class ServiceHearingsControllerTest {

    private static final long CASE_ID = 1625080769409918L;
    private static final long MISSING_CASE_ID = 99250807409918L;
    private static final String BAD_CASE_ID = "ABCASDEF";
    private static final String AUTHORIZATION = "Bearer eyJhbGciOiJIUzUxMiL7.eyJzdWIiOiJzc2NzIiwiZXhwIjoxNjQ2NDA5MjM5fQ"
            + ".zSEbvMJedOGo16yBOXecLgucWyavnoVu023cterreUF0sxPlmV-Qu8Y7OloJUKrLGlNweUr8mVpYWPzE0iNyYw";
    private static final String BAD_AUTHORIZATION = "eyJhbGciOiJIUzUxMiL7.eyJzdWIiOiJzc2NzIiwiZXhwIjoxNjQ2NDA5MjM5fQ"
            + ".zSEbvMJedOGo16yBOXecLgucWyavnoVu023cterreUF0sxPlmV-Qu8Y7OloJUKrLGlNweUr8mVpYWPzE0iNyYw";
    private static final String NOT_AUTHORIZATION = "Bearer notauthed";
    private static final String CASE_REFERENCE = "caseReference";
    private static final String SERVICE_HEARING_VALUES_URL = "/serviceHearingValues";
    private static final String CASE_NAME = "Test Case Name";

    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IdamService idamApiService;

    @MockBean
    private AuthorisationService authorisationService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private CcdService ccdService;

    @BeforeEach
    public void setUp() throws AuthorisationException, InvalidHeaderException {
        SscsCaseDetails caseDetails =
                SscsCaseDetails.builder().data(SscsCaseData.builder()
                        .workAllocationFields(WorkAllocationFields.builder().caseNamePublic(CASE_NAME).build())
                                .build()).build();
        given(ccdService.getByCaseId(eq(CASE_ID), any(IdamTokens.class))).willReturn(caseDetails);
        given(authTokenGenerator.generate()).willReturn("s2s token");
        given(idamApiService.getIdamTokens()).willReturn(IdamTokens.builder().build());
        willThrow(new InvalidHeaderException(new Exception("Test")))
                .given(authorisationService).authorise(anyString());
        willThrow(new AuthorisationException(new Exception("Test")))
                .given(authorisationService).authorise(matches("Bearer .+"));
        willDoNothing().given(authorisationService).authorise(AUTHORIZATION);
    }

    // These are holder tests that will need to be implemented alongside service hearing controller

    @DisplayName("When Authorization and Case ID valid "
            + "should return the case name with a with 200 response code")
    @Test
    public void testPostRequestServiceHearingValues() throws Exception {
        ServiceHearingValues model = ServiceHearingValues.builder().caseName(CASE_NAME).build();
        String json = asJsonString(model);

        mockMvc.perform(post(SERVICE_HEARING_VALUES_URL)
                    .header(SERVICE_AUTHORISATION_HEADER, AUTHORIZATION)
                    .param(CASE_REFERENCE, String.valueOf(CASE_ID)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(json));
    }

    @DisplayName("When Case Reference is Invalid should return a with 400 response code")
    @Test
    public void testPostRequestServiceHearingValues_badCaseID() throws Exception {

        mockMvc.perform(post(SERVICE_HEARING_VALUES_URL)
                        .header(SERVICE_AUTHORISATION_HEADER, AUTHORIZATION)
                        .param(CASE_REFERENCE,BAD_CASE_ID))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("When Authorization is incorrectly formatted should return a with 400 response code")
    @Test
    public void testPostRequestServiceHearingValues_badAuthHeader() throws Exception {

        mockMvc.perform(post(SERVICE_HEARING_VALUES_URL)
                        .header(SERVICE_AUTHORISATION_HEADER, BAD_AUTHORIZATION)
                        .param(CASE_REFERENCE, String.valueOf(CASE_ID)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("When not Authorized should return a with 403 response code")
    @Test
    public void testPostRequestServiceHearingValues_unauthorised() throws Exception {

        mockMvc.perform(post(SERVICE_HEARING_VALUES_URL)
                        .header(SERVICE_AUTHORISATION_HEADER, NOT_AUTHORIZATION)
                        .param(CASE_REFERENCE, String.valueOf(CASE_ID)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @DisplayName("When Case Not Found should return a with 404 response code")
    @Test
    public void testPostRequestServiceHearingValues_missingCase() throws Exception {

        mockMvc.perform(post(SERVICE_HEARING_VALUES_URL)
                        .header(SERVICE_AUTHORISATION_HEADER, AUTHORIZATION)
                        .param(CASE_REFERENCE, String.valueOf(MISSING_CASE_ID)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }


    public static String asJsonString(final Object obj) throws JsonProcessingException {
        return mapper.writeValueAsString(obj);
    }
}
