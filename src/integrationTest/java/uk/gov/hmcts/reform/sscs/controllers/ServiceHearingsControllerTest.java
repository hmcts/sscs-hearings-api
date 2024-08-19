package uk.gov.hmcts.reform.sscs.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.sscs.ccd.client.CcdClient;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseLink;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingSubtype;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.model.service.ServiceHearingRequest;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("integration")
class ServiceHearingsControllerTest {

    private static final long CASE_ID = 1625080769409918L;

    private static final long MISSING_CASE_ID = 99250807409918L;

    private static final long HEARING_ID = 123L;

    private static final String SERVICE_HEARING_VALUES_URL = "/serviceHearingValues";

    private static final String SERVICE_LINKED_CASES_URL = "/serviceLinkedCases";

    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public static final String PROCESSING_VENUE = "Liverpool";
    public static final String BENEFIT_CODE = "1";
    public static final String ISSUE_CODE = "UM";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CcdClient ccdClient;
    @MockBean
    private IdamService idamService;
    @MockBean
    private CoreCaseDataApi coreCaseDataApi;



    @DisplayName("When Authorization and Case ID valid "
            + "should return the case name with a with 200 response code")
    @Test
    void testPostRequestServiceHearingValues() throws Exception {
        ServiceHearingRequest request = ServiceHearingRequest.builder()
                .caseId(String.valueOf(CASE_ID))
                .build();

        given(ccdClient.readForCaseworker(any(), eq(CASE_ID)))
            .willReturn(CaseDetails.builder().data(
                Map.of("appeal",
                       Appeal.builder()
                           .appellant(Appellant.builder()
                                          .name(Name.builder().firstName("John").lastName("Smith").build())
                                          .build())
                           .hearingSubtype(HearingSubtype.builder().wantsHearingTypeFaceToFace("yes").build())
                           .hearingOptions(HearingOptions.builder().wantsToAttend("yes").build()).build(),
                       "benefitCode", BENEFIT_CODE,
                       "issueCode", ISSUE_CODE,
                       "processingVenue", PROCESSING_VENUE
                )
            ).build());

        mockMvc.perform(post(SERVICE_HEARING_VALUES_URL)
                        .contentType(APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("When Case Not Found should return a with 404 response code")
    @Test
    void testPostRequestServiceHearingValues_missingCase() throws Exception {
        ServiceHearingRequest request = ServiceHearingRequest.builder()
                .caseId(String.valueOf(MISSING_CASE_ID))
                .build();

        given(ccdClient.readForCaseworker(any(), eq(MISSING_CASE_ID)))
            .willReturn(null);

        mockMvc.perform(post(SERVICE_HEARING_VALUES_URL)
                        .contentType(APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @DisplayName("When Request and Authorisation are valid should return all mapped hearing service values")
    @Test
    void shouldReturnAllMappedServiceHearingValues() throws Exception {
        ServiceHearingRequest request = ServiceHearingRequest.builder()
                .caseId(String.valueOf(CASE_ID))
                .build();

        given(ccdClient.readForCaseworker(any(), eq(CASE_ID)))
            .willReturn(CaseDetails.builder().data(
                Map.of("appeal",
                       Appeal.builder()
                           .appellant(Appellant.builder()
                                          .name(Name.builder().firstName("John").lastName("Smith").build())
                                          .build())
                           .hearingSubtype(HearingSubtype.builder().wantsHearingTypeFaceToFace("yes").build())
                           .hearingOptions(HearingOptions.builder().wantsToAttend("yes").build()).build(),
                       "benefitCode", BENEFIT_CODE,
                       "issueCode", ISSUE_CODE,
                       "processingVenue", PROCESSING_VENUE
                )
            ).build());

        mockMvc.perform(post(SERVICE_HEARING_VALUES_URL)
                        .contentType(APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("When Authorization and Case ID valid should return the case name with a with 200 response code")
    @Test
    void testPostRequestServiceLinkedCases() throws Exception {
        ServiceHearingRequest request = ServiceHearingRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .hearingId(String.valueOf(HEARING_ID))
            .build();

        given(idamService.getIdamTokens()).willReturn(IdamTokens.builder().build());
        given(coreCaseDataApi.searchCases(
            any(), any(), any(), contains(String.valueOf(CASE_ID))))
            .willReturn(SearchResult.builder().cases(
                List.of(CaseDetails.builder()
                            .data(Map.of("linkedCase", List.of(CaseLink.builder().build()))).build()
                )).build());

        mockMvc.perform(post(SERVICE_LINKED_CASES_URL)
                        .contentType(APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testPostRequestServiceLinkedCases_missingCase() throws Exception {
        ServiceHearingRequest request = ServiceHearingRequest.builder()
                .caseId(String.valueOf(MISSING_CASE_ID))
                .hearingId(String.valueOf(HEARING_ID))
                .build();

        given(idamService.getIdamTokens()).willReturn(IdamTokens.builder().build());
        given(coreCaseDataApi.searchCases(
            any(), any(), any(), contains(String.valueOf(MISSING_CASE_ID))))
            .willReturn(SearchResult.builder().build());

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
