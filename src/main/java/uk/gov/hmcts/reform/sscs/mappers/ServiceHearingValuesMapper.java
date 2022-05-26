package uk.gov.hmcts.reform.sscs.mappers;

import uk.gov.hmcts.reform.sscs.ccd.domain.Event;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingsCaseMapping;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingsDetailsMapping;
import uk.gov.hmcts.reform.sscs.helper.mapping.PartyFlagsMapping;
import uk.gov.hmcts.reform.sscs.helper.mapping.ServiceHearingPartiesMapping;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.HearingWindow;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.HearingWindowDateRange;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.Judiciary;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PanelPreference;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.ServiceHearingValues;
import uk.gov.hmcts.reform.sscs.service.ReferenceData;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
                .autoListFlag(HearingsDetailsMapping.shouldBeAutoListed(caseData))
                .hearingType(HearingsDetailsMapping.getHearingType())
                .caseType(caseData.getBenefitCode())
                .caseCategories(HearingsCaseMapping.buildCaseCategories(caseData, referenceData))
                .hearingWindow(getHearingWindow(caseData))
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
                .hearingIsLinkedFlag(HearingsDetailsMapping.isCaseLinked(caseData))
                .parties(ServiceHearingPartiesMapping.buildServiceHearingPartiesDetails(caseData))
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

    public static HearingWindow getHearingWindow(SscsCaseData caseData) {
        String hearingWindowStart = null;
        if (Objects.nonNull(caseData.getEvents())) {
            Event dwpResponded = caseData.getEvents().stream()
                    .filter(c -> EventType.DWP_RESPOND.equals(c.getValue().getEventType()))
                    .findFirst().orElse(null);

            ZonedDateTime dwpResponseDateTime = Optional.ofNullable(dwpResponded)
                    .map(Event::getValue)
                    .map(EventDetails::getDateTime)
                    .orElse(null);

            if (Objects.nonNull(dwpResponseDateTime)) {
                if (YesNo.isYes(caseData.getUrgentCase())) {
                    hearingWindowStart = dwpResponseDateTime.plusDays(14).toLocalDate().toString();

                } else {
                    hearingWindowStart = dwpResponseDateTime.plusDays(28).toLocalDate().toString();
                }
            }
        }

        return HearingWindow.builder()
                .hearingWindowFirstDate(null)
                .hearingWindowDateRange(HearingWindowDateRange.builder()
                        .hearingWindowStartDateRange(hearingWindowStart)
                        .hearingWindowEndDateRange(null)
                        .build())
                .build();
    }
}
