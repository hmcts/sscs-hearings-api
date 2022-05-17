package uk.gov.hmcts.reform.sscs.mappers;

import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.CcdValue;
import uk.gov.hmcts.reform.sscs.ccd.domain.OtherParty;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingsDetailsMapping;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.ServiceHearingValues;
import uk.gov.hmcts.reform.sscs.utils.SscsCaseDataUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


@Component
public class ServiceHearingValuesMapper {


    public ServiceHearingValues mapServiceHearingValues(SscsCaseDetails caseDetails) {
        if (caseDetails == null) {
            return null;
        }

        SscsCaseData caseData = caseDetails.getData();

        return ServiceHearingValues.builder()
                .caseName(SscsCaseDataUtils.getCaseName(caseData))
                .autoListFlag(false) // TODO to be provided in a future story, right now not populated
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
                .hearingInWelshFlag(YesNo.isYes("No"))
                // TODO get hearingLocations from the method created in SSCS-10245-send-epimsID-to-HMC
                .hearingLocations(new ArrayList<>())
                // TODO the method below "getAdditionalSecurityFlag" is already created in
                //  SSCS-10321-Create-Hearing-POST-Mapping, HearingsCaseMapping ->  shouldBeAdditionalSecurityFlag
                .caseAdditionalSecurityFlag(getAdditionalSecurityFlag(caseData.getOtherParties(), caseData.getDwpUcb()))
                .facilitiesRequired(SscsCaseDataUtils.getFacilitiesRequired(caseData))
                .listingComments(HearingsDetailsMapping.getListingComments(caseData.getAppeal(), caseData.getOtherParties()))
                .hearingRequester(null)
                .privateHearingRequiredFlag(false)
                .leadJudgeContractType(null) // TODO ref data isn't availible yet. List Assist may handle this value
                .judiciary(null) // TODO
                .hearingIsLinkedFlag(false)
                .parties(SscsCaseDataUtils.getParties(caseData)) // TODO missing mappings
                .caseFlags(SscsCaseDataUtils.getCaseFlags(caseData))
                .screenFlow(null)
                .vocabulary(null)
            .build();
    }

    private boolean getAdditionalSecurityFlag(List<CcdValue<OtherParty>> otherParties, String dwpUcb) {
        AtomicReference<Boolean> securityFlag = new AtomicReference<>(false);
        if (Objects.nonNull(otherParties)) {
            otherParties
                .forEach(party -> {
                    if (YesNo.isYes(party.getValue().getUnacceptableCustomerBehaviour())) {
                        securityFlag.set(true);
                    }
                });

        }
        if (YesNo.isYes(dwpUcb)) {
            securityFlag.set(true);
        }
        return securityFlag.get();
    }

    public static HearingPriorityType getHearingPriority(String isAdjournCase, String isUrgentCase) {
        HearingPriorityType hearingPriorityType = HearingPriorityType.NORMAL;

        if (YesNo.isYes(isUrgentCase) || YesNo.isYes(isAdjournCase)) {
            hearingPriorityType = HearingPriorityType.HIGH;
        }
        return hearingPriorityType;
    }




}
