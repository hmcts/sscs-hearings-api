package uk.gov.hmcts.reform.sscs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.sscs.ccd.domain.Adjournment;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.BenefitCode;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseManagementLocation;
import uk.gov.hmcts.reform.sscs.ccd.domain.Hearing;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingSubtype;
import uk.gov.hmcts.reform.sscs.ccd.domain.Issue;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.OverrideFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.Representative;
import uk.gov.hmcts.reform.sscs.ccd.domain.SessionCategory;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HmcUpdateResponse;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingChannel;
import uk.gov.hmcts.reform.sscs.reference.data.model.SessionCategoryMap;
import uk.gov.hmcts.reform.sscs.reference.data.service.HearingDurationsService;
import uk.gov.hmcts.reform.sscs.reference.data.service.SessionCategoryMapService;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.NO;

@ExtendWith(MockitoExtension.class)
public class HearingServiceConsumerTest {
    private static final long HEARING_REQUEST_ID = 12345;
    private static final long CASE_ID = 1625080769409918L;
    private static final String BENEFIT_CODE = "002";
    private static final String ISSUE_CODE = "DD";
    private static final String PROCESSING_VENUE = "Processing Venue";

    private SscsCaseData caseData;

    @Mock
    public HearingDurationsService hearingDurations;

    @Mock
    public SessionCategoryMapService sessionCategoryMaps;

    @Mock
    private VenueService venueService;

    @Mock
    HmcUpdateResponse response;

    @Mock
    private ReferenceDataServiceHolder refData;

    @InjectMocks
    private HearingServiceConsumer hearingServiceConsumer;

    @BeforeEach
    void setup() {
        caseData = SscsCaseData.builder()
            .ccdCaseId(String.valueOf(CASE_ID))
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .caseManagementLocation(CaseManagementLocation.builder().build())
            .adjournment(Adjournment.builder().adjournmentInProgress(YesNo.NO).build())
            .appeal(Appeal.builder()
                        .rep(Representative.builder().hasRepresentative("No").build())
                        .hearingOptions(HearingOptions.builder().wantsToAttend("yes").build())
                        .hearingType("test")
                        .hearingSubtype(HearingSubtype.builder().hearingVideoEmail("email@email.com").wantsHearingTypeFaceToFace(
                            "yes").build())
                        .appellant(Appellant.builder()
                                       .name(Name.builder().firstName("first").lastName("surname").build())
                                       .build())
                        .build())
            .processingVenue(PROCESSING_VENUE)
            .build();

    }

    private void setupResponse() {
        given(response.getHearingRequestId()).willReturn(123L);
        given(response.getVersionNumber()).willReturn(1234L);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void testCreateHearingCaseDetailsConsumer(boolean adjournmentFlagEnabled) {
        setupResponse();
        given(refData.isAdjournmentFlagEnabled()).willReturn(adjournmentFlagEnabled);
        given(sessionCategoryMaps.getSessionCategory(
            BENEFIT_CODE,
            ISSUE_CODE,
            false,
            false
        )).willReturn(new SessionCategoryMap(
            BenefitCode.PIP_NEW_CLAIM,
            Issue.DD,
            false,
            false,
            SessionCategory.CATEGORY_03,
            null
        ));
        given(refData.getHearingDurations()).willReturn(hearingDurations);
        given(refData.getSessionCategoryMaps()).willReturn(sessionCategoryMaps);
        given(refData.getVenueService()).willReturn(venueService);
        given(venueService.getEpimsIdForVenue(PROCESSING_VENUE)).willReturn("219164");

        SscsCaseDetails sscsCaseDetails = SscsCaseDetails.builder().data(caseData).build();

        Consumer<SscsCaseDetails> sscsCaseDetailsConsumer = hearingServiceConsumer.getCreateHearingCaseDetailsConsumerV2(
            response,
            HEARING_REQUEST_ID
        );
        //reset the DLVs
        caseData.getSchedulingAndListingFields().setDefaultListingValues(null);
        sscsCaseDetailsConsumer.accept(sscsCaseDetails);
        //if the mutator has been applied then a defaultListingValue should have been added
        OverrideFields defaultListingValues = sscsCaseDetails.getData().getSchedulingAndListingFields().getDefaultListingValues();
        assertThat(defaultListingValues).isNotNull();
        assertThat(defaultListingValues.getDuration()).isEqualTo(0);
        assertThat(defaultListingValues.getAutoList()).isEqualTo(NO);
        assertThat(defaultListingValues.getHearingWindow().getDateRangeStart()).isNotNull();
        assertThat(defaultListingValues.getAppellantHearingChannel()).isEqualTo(HearingChannel.FACE_TO_FACE);
        assertThat(defaultListingValues.getAppellantInterpreter().getIsInterpreterWanted()).isEqualTo(NO);
        assertThat(defaultListingValues.getHearingVenueEpimsIds().get(0)).isNotNull();

        List<Hearing> hearings = caseData.getHearings();
        assertThat(hearings).isNotEmpty();
        assertEquals(1, hearings.size()); // hearing added
        assertEquals("123", hearings.get(0).getValue().getHearingId());
        assertEquals(1234L, hearings.get(0).getValue().getVersionNumber());
    }

    @Test
    public void testCreateHearingCaseDetailsConsumerWithListingException() {
        given(refData.getHearingDurations()).willReturn(hearingDurations);
        given(refData.getSessionCategoryMaps()).willReturn(sessionCategoryMaps);

        SscsCaseDetails sscsCaseDetails = SscsCaseDetails.builder().data(caseData).build();

        Consumer<SscsCaseDetails> sscsCaseDetailsConsumer = hearingServiceConsumer.getCreateHearingCaseDetailsConsumerV2(
            response,
            HEARING_REQUEST_ID
        );
        //reset the DLVs
        caseData.getSchedulingAndListingFields().setDefaultListingValues(null);
        try {
            sscsCaseDetailsConsumer.accept(sscsCaseDetails);
        } catch (RuntimeException re) {
            assertThat(re.getMessage()).contains("Incorrect benefit/issue code combination");
        }
    }

    @Test
    public void testCreateHearingCaseDataConsumer() {
        setupResponse();

        Consumer<SscsCaseData> sscsCaseDataConsumer = hearingServiceConsumer.getCreateHearingCaseDataConsumer(
            response,
            HEARING_REQUEST_ID
        );
        sscsCaseDataConsumer.accept(caseData);

        List<Hearing> hearings = caseData.getHearings();
        assertThat(hearings).isNotEmpty();
        assertEquals(1, hearings.size()); // hearing added
        assertEquals("123", hearings.get(0).getValue().getHearingId());
        assertEquals(1234L, hearings.get(0).getValue().getVersionNumber());
    }

}
