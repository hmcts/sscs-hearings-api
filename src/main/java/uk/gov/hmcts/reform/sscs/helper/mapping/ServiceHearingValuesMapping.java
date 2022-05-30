package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.HearingWindow;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.HearingWindowDateRange;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.Judiciary;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PanelPreference;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.ServiceHearingValues;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMapping.getSessionCaseCode;


public final class ServiceHearingValuesMapping {

    private ServiceHearingValuesMapping() {
        throw new IllegalStateException("Utility class");
    }


    public static ServiceHearingValues mapServiceHearingValues(SscsCaseDetails caseDetails, ReferenceDataServiceHolder referenceDataServiceHolder) 
        throws InvalidMappingException {
        if (caseDetails == null) {
            return null;
        }

        SscsCaseData caseData = caseDetails.getData();
        boolean shouldBeAutoListed = HearingsAutoListMapping.shouldBeAutoListed(caseData, referenceData);

        return ServiceHearingValues.builder()
                .caseName(HearingsCaseMapping.getInternalCaseName(caseData))
                .caseNamePublic(HearingsCaseMapping.getPublicCaseName(caseData))
                .autoListFlag(shouldBeAutoListed)
                .hearingType(HearingsDetailsMapping.getHearingType())
                .caseType(caseData.getBenefitCode())
                .caseCategories(HearingsCaseMapping.buildCaseCategories(caseData, referenceDataServiceHolder))
                .hearingWindow(buildHearingWindow(caseData, shouldBeAutoListed))
                .duration(HearingsDetailsMapping.getHearingDuration(caseData, referenceDataServiceHolder))
                .hearingPriorityType(HearingsDetailsMapping.getHearingPriority(caseData))
                .numberOfPhysicalAttendees(HearingsDetailsMapping.getNumberOfPhysicalAttendees(caseData))
                // TODO caseData.getLanguagePreferenceWelsh() is for bilingual documents only, future work
                .hearingInWelshFlag(HearingsDetailsMapping.shouldBeHearingsInWelshFlag())
                .hearingLocations(HearingsDetailsMapping.getHearingLocations(caseData.getProcessingVenue(), referenceDataServiceHolder))
                .caseAdditionalSecurityFlag(HearingsCaseMapping.shouldBeAdditionalSecurityFlag(caseData))
                .facilitiesRequired(HearingsDetailsMapping.getFacilitiesRequired(caseData))
                .listingComments(HearingsDetailsMapping.getListingComments(caseData))
                .hearingRequester(HearingsDetailsMapping.getHearingRequester())
                .privateHearingRequiredFlag(HearingsDetailsMapping.isPrivateHearingRequired())
                .leadJudgeContractType(HearingsDetailsMapping.getLeadJudgeContractType()) // TODO ref data isn't available yet. List Assist may handle this value
                .judiciary(getJudiciary(caseDetails, referenceDataServiceHolder))
                .hearingIsLinkedFlag(HearingsDetailsMapping.isCaseLinked(caseData))
                .parties(ServiceHearingPartiesMapping.buildServiceHearingPartiesDetails(caseData, referenceData))
                .caseFlags(PartyFlagsMapping.getCaseFlags(caseData))
                .screenFlow(null)
                .vocabulary(null)
            .build();
    }

    public static Judiciary getJudiciary(SscsCaseDetails caseDetails, ReferenceDataServiceHolder referenceDataServiceHolder) {
        SscsCaseData sscsCaseData = caseDetails.getData();
        return Judiciary.builder()
                .roleType(HearingsDetailsMapping.getRoleTypes())
                .authorisationTypes(HearingsDetailsMapping.getAuthorisationTypes())
                .authorisationSubType(HearingsDetailsMapping.getAuthorisationSubTypes())
                .judiciarySpecialisms(HearingsDetailsMapping.getPanelSpecialisms(sscsCaseData, getSessionCaseCode(sscsCaseData, referenceDataServiceHolder)))
                .judiciaryPreferences(getPanelPreferences())
                .build();
    }

    public static List<PanelPreference> getPanelPreferences() {
        //TODO Need to retrieve PanelPreferences from caseData and/or ReferenceData
        return Collections.emptyList();
    }

    public static HearingWindow buildHearingWindow(SscsCaseData caseData, boolean autoListed) {
        return HearingWindow.builder()
                .hearingWindowFirstDate(null)
                .hearingWindowDateRange(HearingWindowDateRange.builder()
                        .hearingWindowStartDateRange(HearingsDetailsMapping.getHearingWindowStart(caseData, autoListed).toString())
                        .hearingWindowEndDateRange(null)
                        .build())
                .build();
    }
}
