package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.sscs.ccd.domain.Adjournment;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.BenefitCode;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseAccessManagementFields;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseManagementLocation;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingSubtype;
import uk.gov.hmcts.reform.sscs.ccd.domain.Issue;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.Representative;
import uk.gov.hmcts.reform.sscs.ccd.domain.SessionCategory;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.model.HearingWrapper;
import uk.gov.hmcts.reform.sscs.model.single.hearing.HearingRequestPayload;
import uk.gov.hmcts.reform.sscs.reference.data.model.HearingDuration;
import uk.gov.hmcts.reform.sscs.reference.data.model.SessionCategoryMap;
import uk.gov.hmcts.reform.sscs.reference.data.service.HearingDurationsService;
import uk.gov.hmcts.reform.sscs.reference.data.service.SessionCategoryMapService;
import uk.gov.hmcts.reform.sscs.service.VenueService;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.YES;

class HearingsMappingTest extends HearingsMappingBase {

    @Mock
    private HearingDurationsService hearingDurations;

    @Mock
    private SessionCategoryMapService sessionCategoryMaps;

    @Mock
    private ReferenceDataServiceHolder referenceDataServiceHolder;

    @Mock
    private VenueService venueService;

    @DisplayName("When a valid hearing wrapper is given buildHearingPayload returns the correct Hearing Request Payload")
    @Test
    void buildHearingPayload() throws InvalidMappingException, Exception {
        given(hearingDurations.getHearingDuration(BENEFIT_CODE,ISSUE_CODE))
                .willReturn(new HearingDuration(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                                                60,75,30));
        given(sessionCategoryMaps.getSessionCategory(BENEFIT_CODE,ISSUE_CODE,false,false))
                .willReturn(new SessionCategoryMap(BenefitCode.PIP_NEW_CLAIM, Issue.DD,
                                                   false, false, SessionCategory.CATEGORY_03, null));

        given(referenceDataServiceHolder.getHearingDurations()).willReturn(hearingDurations);
        given(referenceDataServiceHolder.getSessionCategoryMaps()).willReturn(sessionCategoryMaps);
        given(referenceDataServiceHolder.getVenueService()).willReturn(venueService);

        SscsCaseData caseData = SscsCaseData.builder()
            .ccdCaseId(String.valueOf(CASE_ID))
            .benefitCode(BENEFIT_CODE)
            .issueCode(ISSUE_CODE)
            .caseCreated(CASE_CREATED)
            .caseAccessManagementFields(CaseAccessManagementFields.builder()
                .caseNameHmctsInternal(CASE_NAME_INTERNAL)
                .caseNamePublic(CASE_NAME_PUBLIC)
                .build())
            .appeal(Appeal.builder()
                .rep(Representative.builder().hasRepresentative("no").build())
                .hearingOptions(HearingOptions.builder().wantsToAttend("yes").build())
                .hearingType("test")
                .hearingSubtype(HearingSubtype.builder().wantsHearingTypeFaceToFace("yes").build())
                .appellant(Appellant.builder()
                    .name(Name.builder()
                        .title("title")
                        .firstName("first")
                        .lastName("last")
                        .build())
                    .build())
                .build())
            .caseManagementLocation(CaseManagementLocation.builder()
                .baseLocation(EPIMS_ID)
                .region(REGION)
                .build())
            .adjournment(Adjournment.builder().isAdjournmentInProgress(YES).build())
            .build();

        HearingWrapper wrapper = HearingWrapper.builder()
                .caseData(caseData)
                .caseData(caseData)
                .build();
        HearingRequestPayload result = HearingsMapping.buildHearingPayload(wrapper, referenceDataServiceHolder);

        assertThat(result).isNotNull();
        assertThat(result.getRequestDetails()).isNotNull();
        assertThat(result.getHearingDetails()).isNotNull();
        assertThat(result.getCaseDetails()).isNotNull();
        assertThat(result.getRequestDetails()).isNotNull();
    }
}
