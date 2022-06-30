package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
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

    public static List<ServiceLinkedCases> getLinkedCasesWithNameAndReasons(SscsCaseData caseData, CcdCaseService ccdCaseService) throws GetCaseException {
        List<String> linkedReferences = Optional.ofNullable(caseData.getLinkedCase())
            .orElseGet(Collections::emptyList).stream()
            .filter(Objects::nonNull)
            .map(CaseLink::getValue)
            .filter(Objects::nonNull)
            .map(CaseLinkDetails::getCaseReference)
            .collect(Collectors.toList());

        return getServiceLinkedCases(caseData, ccdCaseService, linkedReferences);
    }

    @NotNull
    private static List<ServiceLinkedCases> getServiceLinkedCases(SscsCaseData caseData, CcdCaseService ccdCaseService, List<String> linkedReferences) throws GetCaseException {
        if (linkedReferences.isEmpty()) {
            return Collections.emptyList();
        }

        List<ServiceLinkedCases> serviceLinkedCases = new ArrayList<>();
        for (String linkRef : linkedReferences) {
            if (linkRef != null) {
                serviceLinkedCases.add(
                    ServiceLinkedCases.builder()
                        .caseReference(linkRef)
                        .caseName(getLinkedCaseName(ccdCaseService, linkRef))
                        .reasonsForLink(HearingsCaseMapping.getReasonsForLink(caseData))
                        .build());
            }
        }
        return serviceLinkedCases;
    }

    private static String getLinkedCaseName(CcdCaseService ccdCaseService, String caseReference) throws GetCaseException {
        if (!caseReference.isBlank()) {
            SscsCaseData linkedCase = ccdCaseService.getCaseDetails(caseReference).getData();
            return linkedCase.getCaseAccessManagementFields().getCaseNamePublic();
        }
        return null;
    }

}

