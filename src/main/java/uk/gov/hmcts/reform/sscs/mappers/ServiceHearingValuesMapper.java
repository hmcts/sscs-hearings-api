package uk.gov.hmcts.reform.sscs.mappers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingsCaseMapping;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingsDetailsMapping;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingsPartiesMapping;
import uk.gov.hmcts.reform.sscs.helper.mapping.PartyFlagsMapping;
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
                .caseName(HearingsCaseMapping.getInternalCaseName(caseData))
                .caseNamePublic(HearingsCaseMapping.getPublicCaseName(caseData))
                .autoListFlag(HearingsDetailsMapping.shouldBeAutoListed())
                .hearingType(HearingsDetailsMapping.getHearingType())
                .caseType(caseData.getBenefitCode())
                .caseSubTypes(SscsCaseDataUtils.getIssueCode(caseData))
                .hearingWindow(HearingsDetailsMapping.buildHearingWindow(caseData, HearingsDetailsMapping.shouldBeAutoListed()))
                .duration(HearingsDetailsMapping.getHearingDuration(caseData))
                .hearingPriorityType(HearingsDetailsMapping.getHearingPriority(caseData))
                .numberOfPhysicalAttendees(HearingsDetailsMapping.getNumberOfPhysicalAttendees(caseData))
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
                .hearingIsLinkedFlag(HearingsDetailsMapping.isCaseLinked())
                .parties(HearingsPartiesMapping.buildHearingPartiesDetails(caseData))
                .caseFlags(PartyFlagsMapping.getCaseFlags(caseData))
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
