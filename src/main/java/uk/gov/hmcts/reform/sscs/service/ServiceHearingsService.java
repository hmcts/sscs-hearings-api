package uk.gov.hmcts.reform.sscs.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.ListingException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingsCaseMapping;
import uk.gov.hmcts.reform.sscs.helper.mapping.ServiceHearingValuesMapping;
import uk.gov.hmcts.reform.sscs.model.service.ServiceHearingRequest;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.ServiceHearingValues;
import uk.gov.hmcts.reform.sscs.model.service.linkedcases.ServiceLinkedCases;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceHearingsService {

    public static final int NUM_CASES_EXPECTED = 1;

    private static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[a-zA-Z0-9_!#$%&amp;'*+/=?`{|}~^-]+(?:\\.[a-zA-Z0-9_!#$%&amp;'*+/=?`{|}~^-]+)*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$", Pattern.CASE_INSENSITIVE);

    private final CcdCaseService ccdCaseService;

    private final ReferenceDataServiceHolder referenceDataServiceHolder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ServiceHearingValues getServiceHearingValues(ServiceHearingRequest request)
        throws GetCaseException, UpdateCaseException, ListingException, JsonProcessingException {
        SscsCaseDetails caseDetails = ccdCaseService.getCaseDetails(request.getCaseId());

        SscsCaseData caseData = caseDetails.getData();
        String originalCaseData = objectMapper.writeValueAsString(caseData);

        ServiceHearingValues model = ServiceHearingValuesMapping.mapServiceHearingValues(caseData, referenceDataServiceHolder);
        boolean hasInValidHearingVideoEmail = getHasInValidHearingVideoEmail(caseData);

        String updatedCaseData = objectMapper.writeValueAsString(caseData);

        if (hasInValidHearingVideoEmail) {
            updateCaseDataToListingError(caseData, "Hearing video email address must be valid email address");
        } else if (!originalCaseData.equals(updatedCaseData)) {
            ccdCaseService.updateCaseData(
                caseData,
                EventType.UPDATE_CASE_ONLY,
                "Updating caseDetails IDs",
                "IDs updated for caseDetails due to ServiceHearingValues request");
        }

        return model;
    }

    private void updateCaseDataToListingError(SscsCaseData caseData, String description) throws UpdateCaseException {
        ccdCaseService.updateCaseData(caseData,EventType.LISTING_ERROR,"",description);
    }

    private static boolean isEmailValid(String email) {
        String cleanEmail = Optional.ofNullable(email).orElse("");
        return VALID_EMAIL_ADDRESS_REGEX.matcher(cleanEmail).matches();
    }

    private boolean getHasInValidHearingVideoEmail(SscsCaseData sscsCaseData) {
        HearingSubtype hearingSubtype = sscsCaseData.getAppeal().getHearingSubtype();
        if (hearingSubtype != null && YesNo.isYes(hearingSubtype.getWantsHearingTypeVideo())) {

            String hearingVideoEmail = hearingSubtype.getHearingVideoEmail();
            if (!isEmailValid(hearingVideoEmail)) {
                return true;
            }
        }

        List<CcdValue<OtherParty>> otherParties = Optional.ofNullable(sscsCaseData.getOtherParties()).orElse(Collections.emptyList());

        for (CcdValue<OtherParty> otherParty : otherParties) {
            hearingSubtype = otherParty.getValue().getHearingSubtype();

            if (hearingSubtype != null
                && YesNo.isYes(hearingSubtype.getWantsHearingTypeVideo())) {

                String hearingVideoEmail = hearingSubtype.getHearingVideoEmail();
                if (!isEmailValid(hearingVideoEmail)) {
                    return true;
                }
            }
        }

        return false;
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
