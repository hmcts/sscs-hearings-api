package uk.gov.hmcts.reform.sscs.mappers;

import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.hearing.mapping.PartyFlagsMapping;
import uk.gov.hmcts.reform.sscs.model.servicehearingvalues.ServiceHearingValues;
import uk.gov.hmcts.reform.sscs.model.servicehearingvalues.ShvCaseFlags;
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
                .shvHearingWindow(SscsCaseDataUtils.getHearingWindow(caseData))
                .duration(0) // TODO SSCS-10116 will provide
                .hearingPriorityType(getHearingPriority(
                    caseData.getAdjournCaseCanCaseBeListedRightAway(),
                    caseData.getUrgentCase()
                ).getType())     // TODO missing mappings
                .numberOfPhysicalAttendees(SscsCaseDataUtils.getNumberOfPhysicalAttendees(caseData))
                // TODO caseData.getLanguagePreferenceWelsh() is for bilingual documents only, future work
                .hearingInWelshFlag(YesNo.isYes("No"))
                // TODO get hearingLocations from the method created in SSCS-10245-send-epimsID-to-HMC
                .shvHearingLocations(new ArrayList<>())
                // TODO the method below "getAdditionalSecurityFlag" is already created in
                //  SSCS-10321-Create-Hearing-POST-Mapping, HearingsCaseMapping ->  shouldBeAdditionalSecurityFlag
                .caseAdditionalSecurityFlag(getAdditionalSecurityFlag(caseData.getOtherParties(), caseData.getDwpUcb()))
                .facilitiesRequired(SscsCaseDataUtils.getFacilitiesRequired(caseData))
                .listingComments(getListingComments(caseData.getAppeal(), caseData.getOtherParties()))
                .hearingRequester(null)
                .privateHearingRequiredFlag(false)
                .leadJudgeContractType(null) // TODO ref data isn't availible yet. List Assist may handle this value
                .shvJudiciary(null) // TODO
                .hearingIsLinkedFlag(false)
                .shvParties(SscsCaseDataUtils.getParties(caseData)) // TODO missing mappings
                .shvCaseFlags(getCaseFlags(caseData))
                .shvScreenFlow(null)
                .shvVocabulary(null)
                .hmctsServiceID("BBA3")
            .build();
    }

    private boolean getAdditionalSecurityFlag(List<CcdValue<OtherParty>> otherParties, String dwpUcb) {
        AtomicReference<Boolean> securityFlag = new AtomicReference<>(false);
        if (Objects.nonNull(otherParties)) {
            otherParties.stream()
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


    private static String getListingComments(Appeal appeal, List<CcdValue<OtherParty>> otherParties) {
        List<String> listingComments = new ArrayList<>();
        if (Objects.nonNull(appeal)
            && Objects.nonNull(appeal.getHearingOptions())
            && Objects.nonNull(appeal.getHearingOptions().getOther())) {
            listingComments.add(appeal.getHearingOptions().getOther());
        }

        if (Objects.nonNull(otherParties)) {
            listingComments.addAll(otherParties.stream()
                                       .map(o -> o.getValue().getHearingOptions().getOther())
                                       .filter(StringUtils::isNotBlank)
                                       .collect(Collectors.toList()));
        }

        return listingComments.isEmpty() ? null : String.join("\n", listingComments);
    }

    private static ShvCaseFlags getCaseFlags(SscsCaseData sscsCaseData) {
        return ShvCaseFlags.builder()
            .shvFlags(PartyFlagsMapping.getPartyFlags(sscsCaseData).stream()
                       .filter(Objects::nonNull)
                       .collect(Collectors.toList()))
            .flagAmendUrl(null)
            .build();
    }


}
