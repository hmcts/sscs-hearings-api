package uk.gov.hmcts.reform.sscs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.ccd.domain.Adjournment;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.BenefitCode;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseManagementLocation;
import uk.gov.hmcts.reform.sscs.ccd.domain.Hearing;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingState;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingSubtype;
import uk.gov.hmcts.reform.sscs.ccd.domain.Issue;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.Representative;
import uk.gov.hmcts.reform.sscs.ccd.domain.SessionCategory;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.State;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.UnhandleableHearingStateException;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.hearings.HearingRequest;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus;
import uk.gov.hmcts.reform.sscs.model.multi.hearing.CaseHearing;
import uk.gov.hmcts.reform.sscs.model.multi.hearing.HearingsGetResponse;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingCancelRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingRequestPayload;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HmcUpdateResponse;
import uk.gov.hmcts.reform.sscs.reference.data.model.SessionCategoryMap;
import uk.gov.hmcts.reform.sscs.reference.data.service.HearingDurationsService;
import uk.gov.hmcts.reform.sscs.reference.data.service.SessionCategoryMapService;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.sscs.ccd.domain.HearingRoute.LIST_ASSIST;
import static uk.gov.hmcts.reform.sscs.ccd.domain.HearingState.ADJOURN_CREATE_HEARING;
import static uk.gov.hmcts.reform.sscs.ccd.domain.HearingState.CANCEL_HEARING;
import static uk.gov.hmcts.reform.sscs.ccd.domain.HearingState.CREATE_HEARING;
import static uk.gov.hmcts.reform.sscs.ccd.domain.HearingState.UPDATED_CASE;
import static uk.gov.hmcts.reform.sscs.ccd.domain.HearingState.UPDATE_HEARING;
import static uk.gov.hmcts.reform.sscs.reference.data.model.CancellationReason.OTHER;

@ExtendWith(MockitoExtension.class)
class HearingsServiceTest {
    private static final long HEARING_REQUEST_ID = 12345;
    private static final long CASE_ID = 1625080769409918L;
    private static final String BENEFIT_CODE = "002";
    private static final String ISSUE_CODE = "DD";
    public static final String PROCESSING_VENUE = "Processing Venue";


    private HearingWrapper wrapper;
    private HearingRequest request;
    private SscsCaseDetails expectedCaseDetails;

    @Mock
    private HmcHearingApiService hmcHearingApiService;

    @Mock
    private HmcHearingsApiService hmcHearingsApiService;

    @Mock
    private CcdCaseService ccdCaseService;

    @Mock
    private ReferenceDataServiceHolder refData;

    @Mock
    public HearingDurationsService hearingDurations;

    @Mock
    public SessionCategoryMapService sessionCategoryMaps;

    @Mock
    private VenueService venueService;

    @InjectMocks
    private HearingsService hearingsService;

    @BeforeEach
    void setup() {
        SscsCaseData caseData = SscsCaseData.builder()
            .ccdCaseId(String.valueOf(CASE_ID))
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .caseManagementLocation(CaseManagementLocation.builder().build())
            .adjournment(Adjournment.builder().adjournmentInProgress(YesNo.NO).build())
            .appeal(Appeal.builder()
                .rep(Representative.builder().hasRepresentative("No").build())
                .hearingOptions(HearingOptions.builder().wantsToAttend("yes").build())
                .hearingType("test")
                .hearingSubtype(HearingSubtype.builder().wantsHearingTypeFaceToFace("yes").build())
                .appellant(Appellant.builder()
                    .name(Name.builder().build())
                    .build())
                .build())
            .processingVenue(PROCESSING_VENUE)
            .build();

        wrapper = HearingWrapper.builder()
            .hearingState(CREATE_HEARING)
            .caseData(caseData)
            .caseState(State.READY_TO_LIST)
            .build();

        request = HearingRequest
                .builder(String.valueOf(CASE_ID))
                .hearingState(CREATE_HEARING)
                .hearingRoute(LIST_ASSIST)
                .build();

        expectedCaseDetails = SscsCaseDetails.builder()
            .data(SscsCaseData.builder()
                .ccdCaseId(String.valueOf(CASE_ID))
                .build())
            .build();
    }

    @DisplayName("When wrapper with a valid Hearing State is given addHearingResponse should run without error")
    @ParameterizedTest
    @EnumSource(
        value = HearingState.class,
        names = {"UPDATED_CASE","PARTY_NOTIFIED"})
    void processHearingRequest(HearingState state) throws GetCaseException {
        given(ccdCaseService.getStartEventResponse(eq(CASE_ID), any())).willReturn(expectedCaseDetails);

        request.setHearingState(state);
        assertThatNoException()
                .isThrownBy(() -> hearingsService.processHearingRequest(request));
    }

    @DisplayName("When wrapper with a valid Hearing State and Cancellation reason is given addHearingResponse should run without error")
    @Test
    void processHearingRequest() throws GetCaseException {
        given(ccdCaseService.getStartEventResponse(eq(CASE_ID), any())).willReturn(expectedCaseDetails);

        request.setHearingState(UPDATED_CASE);
        request.setCancellationReason(OTHER);
        assertThatNoException()
            .isThrownBy(() -> hearingsService.processHearingRequest(request));
    }

    @DisplayName("When wrapper with a invalid Hearing State is given addHearingResponse should throw an UnhandleableHearingState error")
    @ParameterizedTest
    @NullSource
    void processHearingRequestInvalidState(HearingState state) {
        request.setHearingState(state);

        UnhandleableHearingStateException thrown = assertThrows(UnhandleableHearingStateException.class, () -> {
            hearingsService.processHearingRequest(request);
        });

        assertThat(thrown.getMessage()).isNotEmpty();
    }

    @DisplayName("When wrapper with a case in an invalid case state is given should run without error")
    @Test
    void processHearingWrapperInvalidState() {
        SscsCaseData caseData = SscsCaseData.builder()
            .ccdCaseId(String.valueOf(CASE_ID))
            .build();
        wrapper.setHearingState(CREATE_HEARING);
        wrapper.setCaseData(caseData);
        for (State invalidState : HearingsService.INVALID_CASE_STATES) {
            wrapper.setCaseState(invalidState);
            assertThatNoException()
                .isThrownBy(() -> hearingsService.processHearingWrapper(wrapper));
        }
    }

    @DisplayName("When wrapper with a valid adjourn create Hearing State is given addHearingResponse should run without error")
    @Test
    void processHearingWrapperAdjournmentCreate() {
        given(sessionCategoryMaps.getSessionCategory(BENEFIT_CODE,ISSUE_CODE,false,false))
            .willReturn(new SessionCategoryMap(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                                               false,false,SessionCategory.CATEGORY_03,null));

        given(refData.getHearingDurations()).willReturn(hearingDurations);
        given(refData.getSessionCategoryMaps()).willReturn(sessionCategoryMaps);
        given(refData.getVenueService()).willReturn(venueService);

        given(venueService.getEpimsIdForVenue(PROCESSING_VENUE)).willReturn("219164");

        given(hmcHearingApiService.sendCreateHearingRequest(any(HearingRequestPayload.class)))
            .willReturn(HmcUpdateResponse.builder().build());

        given(hmcHearingsApiService.getHearingsRequest(anyString(),eq(null)))
            .willReturn(HearingsGetResponse.builder().build());

        wrapper.setHearingState(ADJOURN_CREATE_HEARING);

        assertThatNoException()
            .isThrownBy(() -> hearingsService.processHearingWrapper(wrapper));
    }

    @DisplayName("When wrapper with a valid create Hearing State is given addHearingResponse should run without error")
    @Test
    void processHearingWrapperCreate() {
        given(sessionCategoryMaps.getSessionCategory(BENEFIT_CODE,ISSUE_CODE,false,false))
            .willReturn(new SessionCategoryMap(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                false,false,SessionCategory.CATEGORY_03,null));

        given(refData.getHearingDurations()).willReturn(hearingDurations);
        given(refData.getSessionCategoryMaps()).willReturn(sessionCategoryMaps);
        given(refData.getVenueService()).willReturn(venueService);

        given(venueService.getEpimsIdForVenue(PROCESSING_VENUE)).willReturn("219164");

        given(hmcHearingApiService.sendCreateHearingRequest(any(HearingRequestPayload.class)))
                .willReturn(HmcUpdateResponse.builder().build());

        given(hmcHearingsApiService.getHearingsRequest(anyString(),eq(null)))
            .willReturn(HearingsGetResponse.builder().build());

        wrapper.setHearingState(CREATE_HEARING);

        assertThatNoException()
            .isThrownBy(() -> hearingsService.processHearingWrapper(wrapper));
    }

    @DisplayName("When create Hearing is given and there is already a hearing requested/awaiting listing addHearingResponse should run without error")
    @Test
    void processHearingWrapperCreateExistingHearing() {
        given(sessionCategoryMaps.getSessionCategory(BENEFIT_CODE,ISSUE_CODE,false,false))
            .willReturn(new SessionCategoryMap(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                                               false,false,SessionCategory.CATEGORY_03,null));
        given(refData.getVenueService()).willReturn(venueService);
        given(refData.getHearingDurations()).willReturn(hearingDurations);
        given(refData.getSessionCategoryMaps()).willReturn(sessionCategoryMaps);
        HearingsGetResponse hearingsGetResponse = HearingsGetResponse.builder()
            .caseHearings(List.of(CaseHearing.builder()
                .hearingId(HEARING_REQUEST_ID)
                .hmcStatus(HmcStatus.HEARING_REQUESTED)
                .requestVersion(1L)
                .build()))
            .build();

        given(hmcHearingsApiService.getHearingsRequest(anyString(),eq(null)))
            .willReturn(hearingsGetResponse);

        wrapper.setHearingState(CREATE_HEARING);

        assertThatNoException()
            .isThrownBy(() -> hearingsService.processHearingWrapper(wrapper));
    }

    @DisplayName("When wrapper with a valid create Hearing State is given addHearingResponse should run without error")
    @Test
    void processHearingWrapperUpdate() {
        given(sessionCategoryMaps.getSessionCategory(BENEFIT_CODE,ISSUE_CODE,false,false))
            .willReturn(new SessionCategoryMap(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                false,false,SessionCategory.CATEGORY_03,null));

        given(refData.getHearingDurations()).willReturn(hearingDurations);
        given(refData.getSessionCategoryMaps()).willReturn(sessionCategoryMaps);

        given(venueService.getEpimsIdForVenue(PROCESSING_VENUE)).willReturn("219164");

        given(refData.getVenueService()).willReturn(venueService);

        given(hmcHearingApiService.sendUpdateHearingRequest(any(HearingRequestPayload.class), anyString()))
                .willReturn(HmcUpdateResponse.builder().build());

        wrapper.setHearingState(UPDATE_HEARING);
        wrapper.getCaseData()
            .setHearings(new ArrayList<>(Collections.singletonList(Hearing.builder()
                .value(HearingDetails.builder()
                    .hearingId(String.valueOf(HEARING_REQUEST_ID))
                    .build())
                .build())));

        assertThatNoException()
            .isThrownBy(() -> hearingsService.processHearingWrapper(wrapper));
    }

    @DisplayName("When wrapper with a valid cancel Hearing State is given addHearingResponse should run without error")
    @Test
    void processHearingWrapperCancel() {

        given(hmcHearingApiService.sendCancelHearingRequest(any(HearingCancelRequestPayload.class), anyString()))
                .willReturn(HmcUpdateResponse.builder().build());

        wrapper.setHearingState(CANCEL_HEARING);
        wrapper.getCaseData()
            .setHearings(Collections.singletonList(Hearing.builder()
                .value(HearingDetails.builder()
                    .hearingId(String.valueOf(HEARING_REQUEST_ID))
                    .build())
                .build()));
        wrapper.setCancellationReasons(List.of(OTHER));

        assertThatNoException().isThrownBy(() -> hearingsService.processHearingWrapper(wrapper));
    }
}
