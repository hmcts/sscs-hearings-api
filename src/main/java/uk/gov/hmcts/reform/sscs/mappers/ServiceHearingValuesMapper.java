package uk.gov.hmcts.reform.sscs.mappers;

import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingsCaseMapping;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingsDetailsMapping;
import uk.gov.hmcts.reform.sscs.helper.mapping.PartyFlagsMapping;
import uk.gov.hmcts.reform.sscs.helper.mapping.ServiceHearingPartiesMapping;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.Judiciary;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PanelPreference;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.ServiceHearingValues;
import uk.gov.hmcts.reform.sscs.service.ReferenceData;
import uk.gov.hmcts.reform.sscs.utils.SscsCaseDataUtils;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMapping.getSessionCaseCode;


public final class ServiceHearingValuesMapper {

    private ServiceHearingValuesMapper() {
        throw new IllegalStateException("Utility class");
    }


    public static ServiceHearingValues mapServiceHearingValues(SscsCaseDetails caseDetails, ReferenceData referenceData) {
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
                .caseCategories(HearingsCaseMapping.buildCaseCategories(caseData, referenceData))
                .hearingWindow(SscsCaseDataUtils.getHearingWindow(caseData))
                .duration(HearingsDetailsMapping.getHearingDuration(caseData, referenceData))
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
                .judiciary(getJudiciary(caseDetails, referenceData))
                .hearingIsLinkedFlag(HearingsDetailsMapping.isCaseLinked())
                .parties(ServiceHearingPartiesMapping.buildHearingPartiesDetails(caseData))
                .caseFlags(PartyFlagsMapping.getCaseFlags(caseData))
                .screenFlow(null)
                .vocabulary(null)
            .build();
    }

    public static Judiciary getJudiciary(SscsCaseDetails caseDetails, ReferenceData referenceData) {
        SscsCaseData sscsCaseData = caseDetails.getData();
        return Judiciary.builder()
                .roleType(HearingsDetailsMapping.getRoleTypes())
                .authorisationTypes(HearingsDetailsMapping.getAuthorisationTypes())
                .authorisationSubType(HearingsDetailsMapping.getAuthorisationSubTypes())
                .judiciarySpecialisms(HearingsDetailsMapping.getPanelSpecialisms(sscsCaseData, getSessionCaseCode(sscsCaseData, referenceData)))
                .judiciaryPreferences(getPanelPreferences())
                .build();
    }

    public static List<PanelPreference> getPanelPreferences() {
        //TODO Need to retrieve PanelPreferences from caseData and/or ReferenceData
        return new ArrayList<>();
    }
}
