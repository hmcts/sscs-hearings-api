package uk.gov.hmcts.reform.sscs.helper.mapping;

import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.Judiciary;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PanelPreference;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.ServiceHearingValues;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMapping.getSessionCaseCode;


public final class ServiceHearingValuesMapping {

    public static final String BENEFIT = "Benefit";

    private ServiceHearingValuesMapping() {
    }


    public static ServiceHearingValues mapServiceHearingValues(SscsCaseDetails caseDetails, ReferenceDataServiceHolder referenceDataServiceHolder)
        throws InvalidMappingException {
        if (caseDetails == null) {
            return null;
        }

        SscsCaseData caseData = caseDetails.getData();
        boolean shouldBeAutoListed = HearingsAutoListMapping.shouldBeAutoListed(caseData, referenceDataServiceHolder);

        return ServiceHearingValues.builder()
                .publicCaseName(HearingsCaseMapping.getPublicCaseName(caseData))
                .caseDeepLink(HearingsCaseMapping.getCaseDeepLink(caseData, referenceDataServiceHolder))
                .caseManagementLocationCode(HearingsCaseMapping.getCaseManagementLocationCode(caseData))
                .caseRestrictedFlag(HearingsCaseMapping.shouldBeSensitiveFlag())
                .caseSlaStartDate(HearingsCaseMapping.getCaseCreated(caseData))
                .hmctsInternalCaseName(HearingsCaseMapping.getInternalCaseName(caseData))
                .autoListFlag(shouldBeAutoListed)
                .hearingType(HearingsDetailsMapping.getHearingType())
                .caseType(BENEFIT)
                .caseCategories(HearingsCaseMapping.buildCaseCategories(caseData, referenceDataServiceHolder))
                .hearingWindow(HearingsDetailsMapping.buildHearingWindow(caseData, shouldBeAutoListed))
                .duration(HearingsDetailsMapping.getHearingDuration(caseData, referenceDataServiceHolder))
                .hearingPriorityType(HearingsDetailsMapping.getHearingPriority(caseData))
                .numberOfPhysicalAttendees(HearingsDetailsMapping.getNumberOfPhysicalAttendees(caseData))
                .hearingInWelshFlag(HearingsDetailsMapping.shouldBeHearingsInWelshFlag())
                .hearingLocations(HearingsDetailsMapping.getHearingLocations(caseData.getProcessingVenue(), referenceDataServiceHolder))
                .caseAdditionalSecurityFlag(HearingsCaseMapping.shouldBeAdditionalSecurityFlag(caseData))
                .facilitiesRequired(HearingsDetailsMapping.getFacilitiesRequired(caseData))
                .listingComments(HearingsDetailsMapping.getListingComments(caseData))
                .hearingRequester(HearingsDetailsMapping.getHearingRequester())
                .privateHearingRequiredFlag(HearingsDetailsMapping.isPrivateHearingRequired())
                .leadJudgeContractType(HearingsDetailsMapping.getLeadJudgeContractType())
                .judiciary(getJudiciary(caseDetails, referenceDataServiceHolder))
                .hearingIsLinkedFlag(HearingsDetailsMapping.isCaseLinked(caseData))
                .parties(ServiceHearingPartiesMapping.buildServiceHearingPartiesDetails(caseData, referenceDataServiceHolder))
                .caseFlags(PartyFlagsMapping.getCaseFlags(caseData))
                .hmctsServiceID(referenceDataServiceHolder.getSscsServiceCode())
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
}
