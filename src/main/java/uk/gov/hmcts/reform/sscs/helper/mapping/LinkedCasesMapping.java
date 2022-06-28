package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseLink;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseLinkDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.model.service.linkedcases.ServiceLinkedCases;
import uk.gov.hmcts.reform.sscs.service.CcdCaseService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class LinkedCasesMapping {

    private LinkedCasesMapping() {

    }

    public static List<ServiceLinkedCases> getLinkedCases(SscsCaseData caseData) {
        return Optional.ofNullable(caseData.getLinkedCase())
            .orElseGet(Collections::emptyList).stream()
            .filter(Objects::nonNull)
            .map(CaseLink::getValue)
            .filter(Objects::nonNull)
            .map(CaseLinkDetails::getCaseReference)
            .filter(StringUtils::isNotBlank)
            .map(caseReference -> ServiceLinkedCases.builder()
                .caseReference(caseReference)
                .caseName(HearingsCaseMapping.getPublicCaseName(caseData))
                .reasonsForLink(HearingsCaseMapping.getReasonsForLink(caseData))
                .build())
            .collect(Collectors.toList());
    }
    
    @SuppressWarnings("PMD")
    public static List<ServiceLinkedCases> getLinkedCasesWithNameAndReasons(SscsCaseData caseData, CcdCaseService ccdCaseService) throws GetCaseException {
        List<String> linkedReferences = Optional.ofNullable(caseData.getLinkedCase())
            .orElseGet(Collections::emptyList).stream()
            .filter(Objects::nonNull)
            .map(CaseLink::getValue)
            .filter(Objects::nonNull)
            .map(CaseLinkDetails::getCaseReference)
            .collect(Collectors.toList());

        if (linkedReferences.isEmpty()) {
            return Collections.emptyList();
        }

        List<ServiceLinkedCases> serviceLinkedCases = new ArrayList<>();
        ServiceLinkedCases linkedCase;
        for (String linkRef : linkedReferences) {
            if (linkRef != null) {
                //suppressed warning
                linkedCase = new ServiceLinkedCases();
                linkedCase.setCaseReference(linkRef);
                linkedCase.setCaseName(HearingsCaseMapping.getPublicCaseName(getLinkedCaseData(
                    caseData,
                    ccdCaseService,
                    linkRef
                )));
                linkedCase.setReasonsForLink(HearingsCaseMapping.getReasonsForLink(caseData));
                serviceLinkedCases.add(linkedCase);
            }
        }
        return serviceLinkedCases;
    }

    private static SscsCaseData getLinkedCaseData(SscsCaseData caseData, CcdCaseService ccdCaseService, String caseReference) throws GetCaseException {
        List<CaseLink> caseLink = caseData.getLinkedCase();

        String linkedReference = null;

        for (CaseLink link : caseLink) {
            if (link != null && link.getValue() != null && link.getValue().getCaseReference() != null && link.getValue().getCaseReference().equals(caseReference)) {
                linkedReference = link.getValue().getCaseReference();
                return ccdCaseService.getCaseDetails(linkedReference).getData();
            } else {
                return new SscsCaseData();
            }
        }
        return ccdCaseService.getCaseDetails(linkedReference).getData();
    }
}
