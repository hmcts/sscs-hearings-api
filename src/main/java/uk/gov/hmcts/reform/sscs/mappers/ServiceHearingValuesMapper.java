package uk.gov.hmcts.reform.sscs.mappers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingsCaseMapping;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingsDetailsMapping;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.ServiceHearingValues;
import uk.gov.hmcts.reform.sscs.utils.SscsCaseDataUtils;


@Component
public class ServiceHearingValuesMapper {


    public ServiceHearingValues mapServiceHearingValues(SscsCaseDetails caseDetails) {
        if (caseDetails == null) {
            return null;
        }

        SscsCaseData caseData = caseDetails.getData();

        return ServiceHearingValues.builder()
                .caseName(SscsCaseDataUtils.getCaseName(caseData))
                .autoListFlag(HearingsDetailsMapping.shouldBeAutoListed())
                .hearingType(SscsCaseDataUtils.getHearingType(caseData))
                .caseType(caseData.getBenefitCode())
                .caseSubTypes(SscsCaseDataUtils.getIssueCode(caseData))
                // TODO same method is in HearingsDetailsMapping -> buildHearingWindow
                //  (SSCS-10321-Create-Hearing-POST-Mapping)
                .hearingWindow(SscsCaseDataUtils.getHearingWindow(caseData))
                .duration(0) // TODO SSCS-10116 will provide
                .hearingPriorityType(getHearingPriority(
                    caseData.getAdjournCaseCanCaseBeListedRightAway(),
                    caseData.getUrgentCase()
                ).getType())     // TODO missing mappings
                .numberOfPhysicalAttendees(SscsCaseDataUtils.getNumberOfPhysicalAttendees(caseData))
                // TODO caseData.getLanguagePreferenceWelsh() is for bilingual documents only, future work
                .hearingInWelshFlag(HearingsDetailsMapping.shouldBeHearingsInWelshFlag())
                .hearingLocations(HearingsDetailsMapping.getHearingLocations(caseData.getCaseManagementLocation()))
                .caseAdditionalSecurityFlag(HearingsCaseMapping.shouldBeAdditionalSecurityFlag(caseData))
                .facilitiesRequired(HearingsDetailsMapping.getFacilitiesRequired(caseData))
                .listingComments(HearingsDetailsMapping.getListingComments(caseData.getAppeal(), caseData.getOtherParties()))
                .hearingRequester(HearingsDetailsMapping.getHearingRequester())
                .privateHearingRequiredFlag(HearingsDetailsMapping.isPrivateHearingRequired())
                .leadJudgeContractType(HearingsDetailsMapping.getLeadJudgeContractType()) // TODO ref data isn't available yet. List Assist may handle this value
                .judiciary(null) // TODO
                .hearingIsLinkedFlag(false)
                .parties(SscsCaseDataUtils.getParties(caseData)) // TODO missing mappings
                .caseFlags(SscsCaseDataUtils.getCaseFlags(caseData))
                .screenFlow(null)
                .vocabulary(null)
            .build();
    }

    public static HearingPriorityType getHearingPriority(String isAdjournCase, String isUrgentCase) {
        HearingPriorityType hearingPriorityType = HearingPriorityType.NORMAL;

        if (YesNo.isYes(isUrgentCase) || YesNo.isYes(isAdjournCase)) {
            hearingPriorityType = HearingPriorityType.HIGH;
        }
        return hearingPriorityType;
    }




}
