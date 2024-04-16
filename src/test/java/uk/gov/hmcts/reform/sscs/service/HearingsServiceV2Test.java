package uk.gov.hmcts.reform.sscs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.sscs.ccd.domain.Adjournment;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.BenefitCode;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseManagementLocation;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.Hearing;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingRoute;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingState;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingSubtype;
import uk.gov.hmcts.reform.sscs.ccd.domain.Issue;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.OverrideFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.Representative;
import uk.gov.hmcts.reform.sscs.ccd.domain.SessionCategory;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;
import uk.gov.hmcts.reform.sscs.ccd.service.UpdateCcdCaseService;
import uk.gov.hmcts.reform.sscs.exception.GetHearingException;
import uk.gov.hmcts.reform.sscs.exception.ListingException;
import uk.gov.hmcts.reform.sscs.exception.UnhandleableHearingStateException;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.model.HearingEvent;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequest;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus;
import uk.gov.hmcts.reform.sscs.model.multi.hearing.CaseHearing;
import uk.gov.hmcts.reform.sscs.model.multi.hearing.HearingsGetResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.CaseDetails;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingCancelRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HmcUpdateResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.RequestDetails;
import uk.gov.hmcts.reform.sscs.reference.data.model.CancellationReason;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel;
import uk.gov.hmcts.reform.sscs.reference.data.model.SessionCategoryMap;
import uk.gov.hmcts.reform.sscs.reference.data.service.HearingDurationsService;
import uk.gov.hmcts.reform.sscs.reference.data.service.SessionCategoryMapService;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.sscs.ccd.domain.AdjournCaseNextHearingDateType.FIRST_AVAILABLE_DATE;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HearingsServiceV2Test {
    private static final long CASE_ID = 1625080769409918L;
    private static final long HEARING_REQUEST_ID = 12345;
    private static final String BENEFIT_CODE = "002";
    private static final String ISSUE_CODE = "DD";
    private static final String PROCESSING_VENUE = "Processing Venue";

    @Mock
    private HmcHearingApiService hmcHearingApiService;
    @Mock
    private HmcHearingsApiService hmcHearingsApiService;
    @Mock
    private ReferenceDataServiceHolder refData;
    @Mock
    public HearingDurationsService hearingDurations;
    @Mock
    public SessionCategoryMapService sessionCategoryMaps;
    @Mock
    private VenueService venueService;
    @Mock
    private IdamService idamService;
    @Mock
    private UpdateCcdCaseService updateCcdCaseService;
    @Mock
    private CcdCaseService ccdCaseService;
    @Captor
    private ArgumentCaptor<Consumer<SscsCaseDetails>> caseDetailsConsumerCaptor;
    @InjectMocks
    private HearingsService hearingsService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(hearingsService, "hearingsCaseUpdateV2Enabled", true);
        given(idamService.getIdamTokens()).willReturn(IdamTokens.builder().build());
        given(sessionCategoryMaps.getSessionCategory(BENEFIT_CODE, ISSUE_CODE, false, false))
            .willReturn(new SessionCategoryMap(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                                               false, false, SessionCategory.CATEGORY_03, null));
        given(refData.getHearingDurations()).willReturn(hearingDurations);
        given(refData.getSessionCategoryMaps()).willReturn(sessionCategoryMaps);
        given(refData.getVenueService()).willReturn(venueService);
        given(refData.isAdjournmentFlagEnabled()).willReturn(true);
        given(venueService.getEpimsIdForVenue(PROCESSING_VENUE)).willReturn("219164");

        given(hmcHearingApiService.sendCreateHearingRequest(any(HearingRequestPayload.class)))
            .willReturn(HmcUpdateResponse.builder().hearingRequestId(123L).versionNumber(1234L).status(HmcStatus.HEARING_REQUESTED).build());

        given(hmcHearingsApiService.getHearingsRequest(anyString(), eq(null)))
            .willReturn(HearingsGetResponse.builder().build());
    }

    @Test
    void processHearingMessageForAdjournAndCreateHearing() throws Exception {

        SscsCaseDetails sscsCaseDetails = createCaseDataForAdjournAndCreate();
        HearingRequest hearingRequest = HearingRequest.internalBuilder()
            .hearingState(HearingState.ADJOURN_CREATE_HEARING)
            .hearingRoute(HearingRoute.LIST_ASSIST)
            .ccdCaseId(String.valueOf(CASE_ID))
            .build();

        hearingsService.processHearingRequest(hearingRequest);

        verifyCaseDataUpdatedWithHearingResponse(sscsCaseDetails, true);
    }

    @Test
    void processHearingMessageForCreateHearing() throws Exception {
        SscsCaseDetails sscsCaseDetails = createCaseDataForCreateHearing();

        HearingRequest hearingRequest = HearingRequest.internalBuilder()
            .hearingState(HearingState.CREATE_HEARING)
            .hearingRoute(HearingRoute.LIST_ASSIST)
            .ccdCaseId(String.valueOf(CASE_ID))
            .build();

        hearingsService.processHearingRequest(hearingRequest);

        verifyCaseDataUpdatedWithHearingResponse(sscsCaseDetails,false);
    }

    @Test
    @DisplayName("When create Hearing is given and there is already a hearing requested/awaiting listing addHearingResponse should run without error")
    void processHearingMessageForCreateHearingWithExistingHearingId() throws Exception {
        var details = uk.gov.hmcts.reform.sscs.model.single.hearing.HearingDetails.builder().build();
        RequestDetails requestDetails = RequestDetails.builder().versionNumber(2L).build();
        HearingGetResponse hearingGetResponse = HearingGetResponse.builder()
            .hearingDetails(details)
            .requestDetails(requestDetails)
            .caseDetails(CaseDetails.builder().build())
            .partyDetails(List.of())
            .hearingResponse(HearingResponse.builder().build())
            .build();
        given(hmcHearingApiService.getHearingRequest(anyString())).willReturn(hearingGetResponse);

        HearingsGetResponse hearingsGetResponse = HearingsGetResponse.builder()
            .caseHearings(List.of(CaseHearing.builder()
                                      .hearingId(HEARING_REQUEST_ID)
                                      .hmcStatus(HmcStatus.HEARING_REQUESTED)
                                      .requestVersion(2L)
                                      .build()))
            .build();

        given(hmcHearingsApiService.getHearingsRequest(anyString(),eq(null)))
            .willReturn(hearingsGetResponse);

        SscsCaseDetails sscsCaseDetails = createCaseDataForUpdateHearing();

        HearingRequest hearingRequest = HearingRequest.internalBuilder()
            .hearingState(HearingState.CREATE_HEARING)
            .hearingRoute(HearingRoute.LIST_ASSIST)
            .ccdCaseId(String.valueOf(CASE_ID))
            .build();

        hearingsService.processHearingRequest(hearingRequest);

        verifyCaseDataUpdatedWithHearingResponse(sscsCaseDetails,false);
    }

    @Test
    @DisplayName("When create Hearing is given and there is already a hearing requested/awaiting listing addHearingResponse should run without error")
    void processHearingMessageForCreateHearingWhenExistingHearingWhenHearingDoesntExists() throws Exception {
        given(hmcHearingApiService.getHearingRequest(anyString())).willThrow(new GetHearingException(""));
        HearingsGetResponse hearingsGetResponse = HearingsGetResponse.builder()
            .caseHearings(List.of(CaseHearing.builder()
                                      .hearingId(HEARING_REQUEST_ID)
                                      .hmcStatus(HmcStatus.HEARING_REQUESTED)
                                      .requestVersion(1L)
                                      .build()))
            .build();

        given(hmcHearingsApiService.getHearingsRequest(anyString(),eq(null)))
            .willReturn(hearingsGetResponse);

        SscsCaseDetails sscsCaseDetails = createCaseDataForUpdateHearing();

        HearingRequest hearingRequest = HearingRequest.internalBuilder()
            .hearingState(HearingState.CREATE_HEARING)
            .hearingRoute(HearingRoute.LIST_ASSIST)
            .ccdCaseId(String.valueOf(CASE_ID))
            .build();

        hearingsService.processHearingRequest(hearingRequest);

        verifyCaseDataUpdatedWithHearingResponse(sscsCaseDetails,false);
    }

    @Test
    void processHearingMessageForUpdateHearing() throws Exception {

        given(hmcHearingApiService.sendUpdateHearingRequest(any(HearingRequestPayload.class), anyString()))
            .willReturn(HmcUpdateResponse.builder().hearingRequestId(HEARING_REQUEST_ID).versionNumber(2L).build());

        SscsCaseDetails sscsCaseDetails = createCaseDataForUpdateHearing();

        HearingRequest hearingRequest = HearingRequest.internalBuilder()
            .hearingState(HearingState.UPDATE_HEARING)
            .hearingRoute(HearingRoute.LIST_ASSIST)
            .ccdCaseId(String.valueOf(CASE_ID))
            .build();

        hearingsService.processHearingRequest(hearingRequest);

        HearingEvent expectedHearingEvent = HearingEvent.UPDATE_HEARING;
        verify(updateCcdCaseService)
            .updateCaseV2(
                eq(CASE_ID),
                eq(expectedHearingEvent.getEventType().getCcdType()),
                eq(expectedHearingEvent.getSummary()),
                eq(expectedHearingEvent.getDescription()),
                any(),
                caseDetailsConsumerCaptor.capture());

        Consumer<SscsCaseDetails> caseDetailsConsumer = caseDetailsConsumerCaptor.getValue();
        caseDetailsConsumer.accept(sscsCaseDetails);

        SscsCaseData updatedCaseData = sscsCaseDetails.getData();
        List<Hearing> hearings = updatedCaseData.getHearings();
        assertEquals(1, hearings.size());
        assertEquals(String.valueOf(HEARING_REQUEST_ID), hearings.get(0).getValue().getHearingId());
        assertEquals(2L, hearings.get(0).getValue().getVersionNumber());

        OverrideFields overrideFields = updatedCaseData.getSchedulingAndListingFields().getOverrideFields();
        assertNotNull(overrideFields);
        assertEquals(HearingChannel.FACE_TO_FACE, overrideFields.getAppellantHearingChannel());
        verifyNoInteractions(ccdCaseService);
    }

    @DisplayName("When wrapper with a valid create Hearing State is given but hearing duration is not multiple of five then send to listing error")
    @ParameterizedTest
    @CsvSource(value = {
        "31",
        "32",
        "33",
        "34",
    })
    void processHearingMessageForUpdateHearingWithListingDurationNotMultipleOfFive(Integer hearingDuration) throws Exception {

        given(hmcHearingApiService.sendUpdateHearingRequest(any(HearingRequestPayload.class), anyString()))
            .willReturn(HmcUpdateResponse.builder().hearingRequestId(HEARING_REQUEST_ID).versionNumber(2L).build());

        SscsCaseDetails sscsCaseDetails = createCaseDataForUpdateHearing();
        sscsCaseDetails.getData().getSchedulingAndListingFields()
            .setOverrideFields(OverrideFields.builder().duration(hearingDuration).build());

        HearingRequest hearingRequest = HearingRequest.internalBuilder()
            .hearingState(HearingState.UPDATE_HEARING)
            .hearingRoute(HearingRoute.LIST_ASSIST)
            .ccdCaseId(String.valueOf(CASE_ID))
            .build();

        hearingsService.processHearingRequest(hearingRequest);

        HearingEvent expectedHearingEvent = HearingEvent.UPDATE_HEARING;
        verify(updateCcdCaseService)
            .updateCaseV2(
                eq(CASE_ID),
                eq(expectedHearingEvent.getEventType().getCcdType()),
                eq(expectedHearingEvent.getSummary()),
                eq(expectedHearingEvent.getDescription()),
                any(),
                caseDetailsConsumerCaptor.capture());

        Consumer<SscsCaseDetails> caseDetailsConsumer = caseDetailsConsumerCaptor.getValue();
        RuntimeException exception = assertThrows(RuntimeException.class, () -> caseDetailsConsumer.accept(sscsCaseDetails));

        assertEquals(ListingException.class, exception.getCause().getClass());
        assertEquals("Listing duration must be multiple of 5.0 minutes", exception.getCause().getMessage());
    }

    @Test
    void processHearingMessageForCancelHearing() throws Exception {

        given(hmcHearingApiService.sendUpdateHearingRequest(any(HearingRequestPayload.class), anyString()))
            .willReturn(HmcUpdateResponse.builder().hearingRequestId(HEARING_REQUEST_ID).versionNumber(2L).build());

        SscsCaseDetails sscsCaseDetails = createCaseDataForUpdateHearing();
        given(ccdCaseService.getStartEventResponse(CASE_ID, EventType.CASE_UPDATED)).willReturn(sscsCaseDetails);

        HearingRequest hearingRequest = HearingRequest.internalBuilder()
            .hearingState(HearingState.CANCEL_HEARING)
            .hearingRoute(HearingRoute.LIST_ASSIST)
            .ccdCaseId(String.valueOf(CASE_ID))
            .cancellationReason(CancellationReason.PARTY_DID_NOT_ATTEND)
            .build();

        hearingsService.processHearingRequest(hearingRequest);

        verify(ccdCaseService).getStartEventResponse(eq(CASE_ID), eq(EventType.CASE_UPDATED));
        verify(hmcHearingApiService).sendCancelHearingRequest(any(HearingCancelRequestPayload.class), eq(String.valueOf(HEARING_REQUEST_ID)));
        verifyNoInteractions(updateCcdCaseService);
    }

    @Test
    void shouldThrowExceptionWhenHearingStateIsInvalid() {
        final HearingRequest hearingRequest = HearingRequest.internalBuilder()
            .hearingRoute(HearingRoute.LIST_ASSIST).ccdCaseId(String.valueOf(CASE_ID)).build();

        assertThatThrownBy(
            () -> hearingsService.processHearingRequest(hearingRequest))
            .isExactlyInstanceOf(UnhandleableHearingStateException.class);

    }

    @ParameterizedTest
    @EnumSource(
        value = HearingState.class,
        names = {"UPDATED_CASE","PARTY_NOTIFIED"})
    void shouldThrowExceptionWhenHearingStateIsNotSupported(HearingState hearingState) {
        final HearingRequest hearingRequest = HearingRequest.internalBuilder()
            .hearingRoute(HearingRoute.LIST_ASSIST)
            .ccdCaseId(String.valueOf(CASE_ID))
            .hearingState(hearingState).build();

        assertThatNoException().isThrownBy(
            () -> hearingsService.processHearingRequest(hearingRequest));
        verifyNoInteractions(ccdCaseService, updateCcdCaseService);
    }

    private void verifyCaseDataUpdatedWithHearingResponse(SscsCaseDetails sscsCaseDetails, boolean isAdjournAndCreate) {
        assertNull(sscsCaseDetails.getData().getSchedulingAndListingFields().getDefaultListingValues());
        if (isAdjournAndCreate) {
            assertEquals(YesNo.YES, sscsCaseDetails.getData().getAdjournment().getAdjournmentInProgress());
        }

        HearingEvent expectedHearingEvent = HearingEvent.CREATE_HEARING;
        verify(updateCcdCaseService)
            .updateCaseV2(
                eq(CASE_ID),
                eq(expectedHearingEvent.getEventType().getCcdType()),
                eq(expectedHearingEvent.getSummary()),
                eq(expectedHearingEvent.getDescription()),
                any(),
                caseDetailsConsumerCaptor.capture());

        Consumer<SscsCaseDetails> caseDetailsConsumer = caseDetailsConsumerCaptor.getValue();
        caseDetailsConsumer.accept(sscsCaseDetails);

        SscsCaseData updatedCaseData = sscsCaseDetails.getData();
        List<Hearing> hearings = updatedCaseData.getHearings();
        assertEquals(1, hearings.size());

        OverrideFields defaultListingValues = updatedCaseData.getSchedulingAndListingFields().getDefaultListingValues();
        assertNotNull(defaultListingValues);
        assertEquals(HearingChannel.FACE_TO_FACE, defaultListingValues.getAppellantHearingChannel());
        verifyNoInteractions(ccdCaseService);

        if (isAdjournAndCreate) {
            assertEquals(YesNo.NO, sscsCaseDetails.getData().getAdjournment().getAdjournmentInProgress());
        } else {
            assertNull(updatedCaseData.getAdjournment().getAdjournmentInProgress());
        }
    }

    private static SscsCaseDetails createCaseDataForAdjournAndCreate() {
        SscsCaseData sscsCaseData = SscsCaseData.builder()
            .ccdCaseId(String.valueOf(CASE_ID))
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .caseManagementLocation(CaseManagementLocation.builder().build())
            .adjournment(Adjournment.builder().nextHearingDateType(FIRST_AVAILABLE_DATE).interpreterRequired(YesNo.YES).adjournmentInProgress(YesNo.YES).build())
            .appeal(Appeal.builder()
                        .rep(Representative.builder().hasRepresentative("No").build())
                        .hearingOptions(HearingOptions.builder().wantsToAttend("yes").build())
                        .hearingType("test")
                        .hearingSubtype(HearingSubtype.builder().hearingVideoEmail("email@email.com").wantsHearingTypeFaceToFace("yes").build())
                        .appellant(Appellant.builder()
                                       .name(Name.builder().firstName("first").lastName("surname").build())
                                       .build())
                        .build())
            .processingVenue(PROCESSING_VENUE)
            .build();

        return SscsCaseDetails.builder().data(sscsCaseData).build();
    }

    private static SscsCaseDetails createCaseDataForCreateHearing() {
        SscsCaseData sscsCaseData = SscsCaseData.builder()
            .ccdCaseId(String.valueOf(CASE_ID))
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .caseManagementLocation(CaseManagementLocation.builder().build())
            .appeal(Appeal.builder()
                        .rep(Representative.builder().hasRepresentative("No").build())
                        .hearingOptions(HearingOptions.builder().wantsToAttend("yes").build())
                        .hearingType("test")
                        .hearingSubtype(HearingSubtype.builder().hearingVideoEmail("email@email.com").wantsHearingTypeFaceToFace("yes").build())
                        .appellant(Appellant.builder()
                                       .name(Name.builder().firstName("first").lastName("surname").build())
                                       .build())
                        .build())
            .processingVenue(PROCESSING_VENUE)
            .build();
        return SscsCaseDetails.builder().data(sscsCaseData).build();
    }

    private static SscsCaseDetails createCaseDataForUpdateHearing() {
        SscsCaseData sscsCaseData = SscsCaseData.builder()
            .ccdCaseId(String.valueOf(CASE_ID))
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .caseManagementLocation(CaseManagementLocation.builder().build())
            .appeal(Appeal.builder()
                        .rep(Representative.builder().hasRepresentative("No").build())
                        .hearingOptions(HearingOptions.builder().wantsToAttend("yes").build())
                        .hearingType("test")
                        .hearingSubtype(HearingSubtype.builder().hearingVideoEmail("email@email.com").wantsHearingTypeFaceToFace("yes").build())
                        .appellant(Appellant.builder()
                                       .name(Name.builder().firstName("first").lastName("surname").build())
                                       .build())
                        .build())
            .hearings(new ArrayList<>(Collections.singletonList(Hearing.builder()
                                                                    .value(HearingDetails.builder()
                                                                               .hearingId(String.valueOf(HEARING_REQUEST_ID))
                                                                               .versionNumber(1L)
                                                                               .build())
                                                                    .build())))
            .processingVenue(PROCESSING_VENUE)
            .build();

        return SscsCaseDetails.builder().data(sscsCaseData).build();
    }

}
