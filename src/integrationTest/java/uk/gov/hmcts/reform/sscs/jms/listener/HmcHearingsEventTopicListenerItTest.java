package uk.gov.hmcts.reform.sscs.jms.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.qpid.jms.message.JmsBytesMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.ccd.service.UpdateCcdCaseService;
import uk.gov.hmcts.reform.sscs.exception.CaseException;
import uk.gov.hmcts.reform.sscs.exception.HmcEventProcessingException;
import uk.gov.hmcts.reform.sscs.exception.MessageProcessingException;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;
import uk.gov.hmcts.reform.sscs.model.hmc.message.HearingUpdate;
import uk.gov.hmcts.reform.sscs.model.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.ListingStatus;
import uk.gov.hmcts.reform.sscs.model.single.hearing.CaseDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.PartyDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RequestDetails;
import uk.gov.hmcts.reform.sscs.service.CcdCaseService;
import uk.gov.hmcts.reform.sscs.service.HmcHearingApi;
import uk.gov.hmcts.reform.sscs.service.VenueService;
import uk.gov.hmcts.reform.sscs.service.hmc.topic.ProcessHmcMessageService;
import uk.gov.hmcts.reform.sscs.service.hmc.topic.ProcessHmcMessageServiceV2;

import java.time.LocalDateTime;
import javax.jms.JMSException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.sscs.ccd.domain.AdjournCaseNextHearingDateType.FIRST_AVAILABLE_DATE;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMappingBase.BENEFIT_CODE;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMappingBase.ISSUE_CODE;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.LISTED;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("integration")
@TestPropertySource(locations = "classpath:config/application_it.properties")
public class HmcHearingsEventTopicListenerItTest {

    private static final String CASE_ID = "1234123412341234";
    private static final String PROCESSING_VENUE_1 = "Cardiff";

    private HmcHearingsEventTopicListener hmcHearingsEventTopicListener;
    @MockBean
    private ObjectMapper mapper;

    @Autowired
    private ProcessHmcMessageService processHmcMessageService;

    @Autowired
    private ProcessHmcMessageServiceV2 processHmcMessageServiceV2;

    @MockBean
    private JmsBytesMessage bytesMessage;

    @MockBean
    private IdamService idamService;
    @MockBean
    private CcdCaseService ccdCaseService;

    @MockBean
    private VenueService venueService;

    @MockBean
    private HmcHearingApi hearingApi;

    @MockBean
    private UpdateCcdCaseService updateCcdCaseService;

    @Test
    public void testHearingsUpdate() throws JMSException, HmcEventProcessingException, JsonProcessingException, CaseException, MessageProcessingException {
        hmcHearingsEventTopicListener = new HmcHearingsEventTopicListener("BBA3", processHmcMessageService, processHmcMessageServiceV2);
        ReflectionTestUtils.setField(hmcHearingsEventTopicListener, "objectMapper", mapper);
        ReflectionTestUtils.setField(hmcHearingsEventTopicListener, "processEventMessageV2Enabled", false);

        HmcMessage hmcMessage = createHmcMessage("BBA3");
        IdamTokens idamTokens = IdamTokens.builder().build();

        SscsCaseDetails sscsCaseDetails = getSscsCaseDetails();
        VenueDetails venueDetails = getVenueDetails();
        HearingGetResponse hearingGetResponse = getHearingGetResponse();

        when(bytesMessage.getStringProperty("hmctsDeploymentId")).thenReturn("test");
        when(bytesMessage.getBodyLength()).thenReturn(20L);

        when(mapper.readValue(any(String.class), eq(HmcMessage.class))).thenReturn(hmcMessage);
        when(idamService.getIdamTokens()).thenReturn(idamTokens);
        when(ccdCaseService.getCaseDetails(anyLong())).thenReturn(sscsCaseDetails);

        when(hearingApi.getHearingRequest(any(), any(), any(), any(), any()))
            .thenReturn(hearingGetResponse);
        when(venueService.getVenueDetailsForActiveVenueByEpimsId(any())).thenReturn(venueDetails);

        hmcHearingsEventTopicListener.onMessage(bytesMessage);

        verify(idamService, times(2)).getIdamTokens();
        verify(ccdCaseService).getCaseDetails(anyLong());

        verify(venueService).getVenueDetailsForActiveVenueByEpimsId(any());

        verify(hearingApi).getHearingRequest(any(), any(), any(), any(), any());

        verify(updateCcdCaseService, never()).updateCaseV2DynamicEvent(anyLong(), any(), anyBoolean(), eq(idamTokens), any());

        verify(ccdCaseService).updateCaseData(
            eq(sscsCaseDetails.getData()),
            any(),
            any(),
            any());

    }

    @Test
    public void testHearingsUpdateV2() throws JMSException, HmcEventProcessingException, JsonProcessingException, CaseException, MessageProcessingException {
        hmcHearingsEventTopicListener = new HmcHearingsEventTopicListener("BBA3", processHmcMessageService, processHmcMessageServiceV2);
        ReflectionTestUtils.setField(hmcHearingsEventTopicListener, "objectMapper", mapper);
        ReflectionTestUtils.setField(hmcHearingsEventTopicListener, "processEventMessageV2Enabled", true);

        HmcMessage hmcMessage = createHmcMessage("BBA3");
        IdamTokens idamTokens = IdamTokens.builder().build();

        HearingGetResponse hearingGetResponse = getHearingGetResponse();

        when(bytesMessage.getStringProperty("hmctsDeploymentId")).thenReturn("test");
        when(bytesMessage.getBodyLength()).thenReturn(20L);

        when(mapper.readValue(any(String.class), eq(HmcMessage.class))).thenReturn(hmcMessage);
        when(idamService.getIdamTokens()).thenReturn(idamTokens);
        when(ccdCaseService.getCaseDetails(anyLong())).thenReturn(SscsCaseDetails.builder().data(SscsCaseData.builder().build()).build());
        when(venueService.getVenueDetailsForActiveVenueByEpimsId(any())).thenReturn(VenueDetails.builder().build());

        when(hearingApi.getHearingRequest(any(), any(), any(), any(), any()))
            .thenReturn(hearingGetResponse);

        hmcHearingsEventTopicListener.onMessage(bytesMessage);

        verify(idamService, times(3)).getIdamTokens();

        verify(hearingApi).getHearingRequest(any(), any(), any(), any(), any());

        verify(ccdCaseService, never()).updateCaseData(any(), any(), any());
        verify(updateCcdCaseService).updateCaseV2DynamicEvent(eq(Long.parseLong(CASE_ID)), any(), anyBoolean(), eq(idamTokens), any());

    }

    private static SscsCaseDetails getSscsCaseDetails() {
        return SscsCaseDetails.builder().data(
            SscsCaseData.builder()
                .ccdCaseId(CASE_ID)
                .adjournment(Adjournment.builder().nextHearingDateType(FIRST_AVAILABLE_DATE).build())
                .processingVenue(PROCESSING_VENUE_1)
                .benefitCode(BENEFIT_CODE)
                .issueCode(ISSUE_CODE)
                .appeal(
                    Appeal.builder().appellant(
                            Appellant.builder()
                                .id("11")
                                .name(Name.builder().firstName("first").lastName("last").build())
                                .build())
                        .hearingOptions(HearingOptions.builder().languageInterpreter("Yes").build())
                        .build())
                .regionalProcessingCenter(RegionalProcessingCenter.builder().name("Bristol Magistrates").postcode(
                    "BA1 1AA").build())
                .build()).build();
    }

    private HmcMessage createHmcMessage(String messageServiceCode) {
        return HmcMessage.builder()
            .hmctsServiceCode(messageServiceCode)
            .caseId(Long.parseLong(CASE_ID))
            .hearingId("1")
            .hearingUpdate(HearingUpdate.builder()
                               .hmcStatus(LISTED)
                               .build())
            .build();
    }

    private HearingGetResponse getHearingGetResponse() {
        return HearingGetResponse.builder()
            .hearingResponse(HearingResponse.builder()
                                 .hearingSessions(Lists.newArrayList(HearingDaySchedule.builder()
                                                                         .hearingVenueEpimsId("8001")
                                                                         .hearingStartDateTime(LocalDateTime.now())
                                                                         .hearingEndDateTime(LocalDateTime.now().plusDays(2))
                                                                         .build()))
                                 .listingStatus(ListingStatus.FIXED).build())
            .hearingDetails(HearingDetails.builder().build())
            .caseDetails(CaseDetails.builder().build())
            .partyDetails(Lists.newArrayList(PartyDetails.builder().build()))
            .requestDetails(RequestDetails.builder().hearingRequestId("12").build())
            .build();
    }

    private VenueDetails getVenueDetails() {
        return VenueDetails.builder()
            .venueId("8001")
            .venName("venueName")
            .url("http://test.com")
            .venAddressLine1("adrLine1")
            .venAddressLine2("adrLine2")
            .venAddressTown("adrTown")
            .venAddressCounty("adrCounty")
            .venAddressPostcode("adrPostcode")
            .regionalProcessingCentre("regionalProcessingCentre")
            .build();
    }
}
