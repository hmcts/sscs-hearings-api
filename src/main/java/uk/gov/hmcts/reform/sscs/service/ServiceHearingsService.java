package uk.gov.hmcts.reform.sscs.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseLink;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseLinkDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.ListingException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingsCaseMapping;
import uk.gov.hmcts.reform.sscs.helper.mapping.ServiceHearingValuesMapping;
import uk.gov.hmcts.reform.sscs.model.service.ServiceHearingRequest;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.PartyDetails;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.ServiceHearingValues;
import uk.gov.hmcts.reform.sscs.model.service.linkedcases.ServiceLinkedCases;
import uk.gov.hmcts.reform.sscs.model.single.hearing.UnavailabilityRange;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceHearingsService {

    public static final int NUM_CASES_EXPECTED = 1;

    private final CcdCaseService ccdCaseService;

    private final ReferenceDataServiceHolder referenceDataServiceHolder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ServiceHearingValues getServiceHearingValues(ServiceHearingRequest request)
        throws GetCaseException, UpdateCaseException, ListingException, JsonProcessingException {
        SscsCaseDetails caseDetails = ccdCaseService.getCaseDetails(request.getCaseId());

        SscsCaseData caseData = caseDetails.getData();
        String originalCaseData = objectMapper.writeValueAsString(caseData);

        ServiceHearingValues model = ServiceHearingValuesMapping.mapServiceHearingValues(
            caseData,
            referenceDataServiceHolder
        );
        Optional<UnavailabilityRange> unavailabilityRangeWithDateProblems = getInvalidUnavailabilityRange(model);

        String partyNameMissing = getPartyNameMissing(model);
        String updatedCaseData = objectMapper.writeValueAsString(caseData);

        if (unavailabilityRangeWithDateProblems.isPresent()) {
            updateCaseDataToListingError(caseData, "One of the parties unavailability end date is before start date");
        } else if (partyNameMissing != null) {
            updateCaseDataToListingError(caseData, partyNameMissing);
        } else if (!originalCaseData.equals(updatedCaseData)) {
            ccdCaseService.updateCaseData(
                caseData,
                EventType.UPDATE_CASE_ONLY,
                "Updating caseDetails IDs",
                "IDs updated for caseDetails due to ServiceHearingValues request"
            );
        }

        return model;
    }

    @NotNull
    private static Optional<UnavailabilityRange> getInvalidUnavailabilityRange(ServiceHearingValues model) {
        return model.getParties().stream()
            .map(PartyDetails::getUnavailabilityRanges)
            .flatMap(unavailabilityRangeList -> unavailabilityRangeList.stream()
                .filter(unavailabilityRange -> unavailabilityRange.getUnavailableFromDate() != null
                    && unavailabilityRange.getUnavailableToDate() != null
                    && unavailabilityRange.getUnavailableToDate().isBefore(unavailabilityRange.getUnavailableFromDate()))
            ).findFirst();
    }

    private void updateCaseDataToListingError(SscsCaseData caseData, String description) throws UpdateCaseException {
        ccdCaseService.updateCaseData(caseData, EventType.LISTING_ERROR, "", description);
    }

    @Nullable
    private static String getPartyNameMissing(ServiceHearingValues model) {
        return model.getParties().stream()
            .map(PartyDetails::getIndividualDetails)
            .filter(individualDetails -> StringUtils.isEmpty(individualDetails.getFirstName())
                && StringUtils.isEmpty(individualDetails.getLastName()))
            .findFirst()
            .map(individualDetails -> "First name and Last name cannot be empty")
            .orElseGet(() -> model.getParties().stream()
                .map(PartyDetails::getIndividualDetails)
                .filter(individualDetails -> StringUtils.isEmpty(individualDetails.getFirstName()))
                .findFirst()
                .map(individualDetails -> "First name cannot be empty")
                .orElseGet(() -> model.getParties().stream()
                    .map(PartyDetails::getIndividualDetails)
                    .filter(individualDetails -> StringUtils.isEmpty(individualDetails.getLastName()))
                    .findFirst()
                    .map(individualDetails -> "Last name cannot be empty")
                    .orElse(null)));
    }

    public List<ServiceLinkedCases> getServiceLinkedCases(ServiceHearingRequest request)
        throws GetCaseException {

        String caseId = request.getCaseId();
        List<SscsCaseDetails> mainCaseData = ccdCaseService.getCasesViaElastic(List.of(request.getCaseId()));

        if (mainCaseData == null || mainCaseData.size() != NUM_CASES_EXPECTED) {
            throw new IllegalStateException(
                "Invalid search data returned: one case is required. Attempted to fetch data for " + caseId);
        }

        SscsCaseData caseData = mainCaseData.get(0).getData();

        List<String> linkedReferences = Optional.ofNullable(caseData.getLinkedCase())
            .orElseGet(Collections::emptyList).stream()
            .filter(Objects::nonNull)
            .map(CaseLink::getValue)
            .filter(Objects::nonNull)
            .map(CaseLinkDetails::getCaseReference)
            .collect(Collectors.toList());

        log.info("{} linked case references found for case: {}", linkedReferences.size(), caseId);

        List<SscsCaseDetails> linkedCases = ccdCaseService.getCasesViaElastic(linkedReferences);

        return linkedCases.stream().map(linkedCase ->
                                            ServiceLinkedCases.builder()
                                                .caseReference(linkedCase.getId().toString())
                                                .caseName(linkedCase.getData().getCaseAccessManagementFields().getCaseNamePublic())
                                                .reasonsForLink(HearingsCaseMapping.getReasonsForLink(caseData))
                                                .build())
            .collect(Collectors.toList());
    }
}
