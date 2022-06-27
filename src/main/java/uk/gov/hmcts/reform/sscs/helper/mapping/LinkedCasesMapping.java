package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseLink;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseLinkDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.model.service.linkedcases.ServiceLinkedCases;
import uk.gov.hmcts.reform.sscs.service.CcdCaseService;

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
                .build())
            .collect(Collectors.toList());
    }

    public static List<ServiceLinkedCases> getLinkedCasesWithNameAndReasons(SscsCaseData caseData, CcdCaseService ccdCaseService) throws GetCaseException {
        return Optional.ofNullable(caseData.getLinkedCase())
            .orElseGet(Collections::emptyList).stream()
            .filter(Objects::nonNull)
            .map(CaseLink::getValue)
            .filter(Objects::nonNull)
            .map(CaseLinkDetails::getCaseReference)
            .filter(StringUtils::isNotBlank)
            .map(caseReference -> {
                try {
                    return ServiceLinkedCases.builder()
                                .caseReference(caseReference)
                                .caseName(HearingsCaseMapping.getPublicCaseName(getLinkedCaseData(caseData, ccdCaseService, caseReference)))
                                .reasonsForLink(HearingsCaseMapping.getReasonsForLink(caseData))
                                .build();
                } catch (GetCaseException e) {
                    e.printStackTrace();
                }
                return null;
            })
            .collect(Collectors.toList());
    }

    private static SscsCaseData getLinkedCaseData(SscsCaseData caseData, CcdCaseService ccdCaseService, String caseReference) throws GetCaseException {
        List<CaseLink> caseLink = caseData.getLinkedCase();

        String linkedReference = null;
        for (CaseLink link : caseLink) {
            if (link.getValue().getCaseReference().equals(caseReference)) {
                linkedReference = link.getValue().getCaseReference();
            }
        }
        return ccdCaseService.getCaseDetails(linkedReference).getData();
    }
}

