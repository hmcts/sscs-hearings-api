package uk.gov.hmcts.reform.sscs.helper.mapping;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseLink;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseLinkDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.model.service.linkedcases.LinkedCase;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class LinkedCasesMapping {

    private LinkedCasesMapping() {

    }

    public static List<LinkedCase> getLinkedCases(SscsCaseData caseData) {
        return Optional.ofNullable(caseData.getLinkedCase())
                .orElseGet(Collections::emptyList).stream()
                .filter(Objects::nonNull)
                .map(CaseLink::getValue)
                .filter(Objects::nonNull)
                .map(CaseLinkDetails::getCaseReference)
                .filter(StringUtils::isNotBlank)
                .map(caseReference -> LinkedCase.builder()
                    .caseReference(caseReference)
                    .caseName(HearingsCaseMapping.getPublicCaseName(caseData))
                    .reasonsForLink(HearingsCaseMapping.getReasonsForLink(caseData))
                    .build())
                .collect(Collectors.toList());
    }
}
