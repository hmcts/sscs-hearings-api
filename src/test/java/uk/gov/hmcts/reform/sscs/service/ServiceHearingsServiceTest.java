package uk.gov.hmcts.reform.sscs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.BenefitCode;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseManagementLocation;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingSubtype;
import uk.gov.hmcts.reform.sscs.ccd.domain.Issue;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.Representative;
import uk.gov.hmcts.reform.sscs.ccd.domain.SessionCategory;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsIndustrialInjuriesData;
import uk.gov.hmcts.reform.sscs.model.service.ServiceHearingRequest;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.ServiceHearingValues;
import uk.gov.hmcts.reform.sscs.reference.data.model.SessionCategoryMap;
import uk.gov.hmcts.reform.sscs.reference.data.service.HearingDurationsService;
import uk.gov.hmcts.reform.sscs.reference.data.service.SessionCategoryMapService;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.sscs.ccd.domain.EventType.UPDATE_CASE_ONLY;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMappingBase.BENEFIT_CODE;
import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMappingBase.ISSUE_CODE;

@ExtendWith(MockitoExtension.class)
class ServiceHearingsServiceTest {

    private static final long CASE_ID = 12345L;

    @Mock
    private SessionCategoryMapService sessionCategoryMaps;

    @Mock
    private HearingDurationsService hearingDurations;

    @Mock
    private VenueService venueService;

    @Mock
    private ReferenceDataServiceHolder referenceDataServiceHolder;

    @Mock
    private CcdCaseService ccdCaseService;

    @InjectMocks
    private ServiceHearingsService serviceHearingsService;

    private SscsCaseData caseData;
    private SscsCaseDetails caseDetails;


    @BeforeEach
    void setup() {
        caseData = SscsCaseData.builder()
            .ccdCaseId("1234")
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .urgentCase("Yes")
            .adjournCaseCanCaseBeListedRightAway("Yes")
            .caseManagementLocation(CaseManagementLocation.builder()
                .baseLocation("LIVERPOOL SOCIAL SECURITY AND CHILD SUPPORT TRIBUNAL")
                .region("North West")
                .build())
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
                    .build())
                .rep(Representative.builder()
                    .hasRepresentative("Yes")
                    .name(Name.builder()
                        .title("Mr")
                        .firstName("Harry")
                        .lastName("Potter")
                        .build())
                    .build())
                .build())
            .languagePreferenceWelsh("No")
            .linkedCasesBoolean("No")
            .sscsIndustrialInjuriesData(SscsIndustrialInjuriesData.builder()
                .panelDoctorSpecialism("cardiologist")
                .secondPanelDoctorSpecialism("eyeSurgeon")
                .build())
            .build();

        caseDetails = SscsCaseDetails.builder()
            .data(caseData)
            .build();
    }

    @DisplayName("When a case data is retrieved an entity which does not have a Id, that a new Id will be generated and the method updateCaseData will be called once")
    @Test
    void testGetServiceHearingValuesNoIds() throws Exception {
        ServiceHearingRequest request = ServiceHearingRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .build();

        given(sessionCategoryMaps.getSessionCategory(BENEFIT_CODE,ISSUE_CODE,true,false))
            .willReturn(new SessionCategoryMap(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                false,false, SessionCategory.CATEGORY_03,null));

        given(referenceDataServiceHolder.getSessionCategoryMaps()).willReturn(sessionCategoryMaps);

        given(hearingDurations.getHearingDuration(BENEFIT_CODE,ISSUE_CODE)).willReturn(null);

        given(referenceDataServiceHolder.getHearingDurations()).willReturn(hearingDurations);

        given(venueService.getEpimsIdForVenue(caseData.getProcessingVenue())).willReturn(Optional.of("9876"));

        given(referenceDataServiceHolder.getVenueService()).willReturn(venueService);

        given(ccdCaseService.getCaseDetails(String.valueOf(CASE_ID))).willReturn(caseDetails);

        ServiceHearingValues result = serviceHearingsService.getServiceHearingValues(request);

        assertThat(result.getParties())
            .extracting("partyID")
            .doesNotContainNull();

        verify(ccdCaseService, times(1)).updateCaseData(any(SscsCaseData.class), eq(UPDATE_CASE_ONLY),anyString(),anyString());
    }

    @DisplayName("When a case data is retrieved where all valid entities have a Id the method updateCaseData will never be called")
    @Test
    void testGetServiceHearingValuesWithIds() throws Exception {
        ServiceHearingRequest request = ServiceHearingRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .build();

        caseData.getAppeal().getAppellant().setId("1");
        caseData.getAppeal().getRep().setId("2");

        given(sessionCategoryMaps.getSessionCategory(BENEFIT_CODE,ISSUE_CODE,true,false))
            .willReturn(new SessionCategoryMap(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                false,false, SessionCategory.CATEGORY_03,null));

        given(referenceDataServiceHolder.getSessionCategoryMaps()).willReturn(sessionCategoryMaps);

        given(hearingDurations.getHearingDuration(BENEFIT_CODE,ISSUE_CODE)).willReturn(null);

        given(referenceDataServiceHolder.getHearingDurations()).willReturn(hearingDurations);

        given(venueService.getEpimsIdForVenue(caseData.getProcessingVenue())).willReturn(Optional.of("9876"));

        given(referenceDataServiceHolder.getVenueService()).willReturn(venueService);

        given(ccdCaseService.getCaseDetails(String.valueOf(CASE_ID))).willReturn(caseDetails);

        ServiceHearingValues result = serviceHearingsService.getServiceHearingValues(request);

        assertThat(result.getParties())
            .extracting("partyID")
            .doesNotContainNull();

        verify(ccdCaseService, never()).updateCaseData(any(SscsCaseData.class), any(EventType.class),anyString(),anyString());
    }
}
