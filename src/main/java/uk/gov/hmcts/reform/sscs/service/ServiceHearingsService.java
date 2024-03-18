package uk.gov.hmcts.reform.sscs.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.ccd.client.CcdClient;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseLink;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseLinkDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.SscsCcdConvertService;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.ListingException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingsCaseMapping;
import uk.gov.hmcts.reform.sscs.helper.mapping.ServiceHearingValuesMapping;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.model.service.ServiceHearingRequest;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.ServiceHearingValues;
import uk.gov.hmcts.reform.sscs.model.service.linkedcases.ServiceLinkedCases;
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
    private final SscsCcdConvertService sscsCcdConvertService;
    private final CcdClient ccdClient;
    private final IdamService idamService;
    private static String summary = "Updating caseDetails IDs";
    private static String description = "IDs updated for caseDetails due to ServiceHearingValues request";
    @Value("${flags.service-hearing-service-v2.enabled}")
    private boolean isV2ServiceHearingServiceEnabled;

    private final ReferenceDataServiceHolder refData;
    private static final String EVENT_TYPE = EventType.UPDATE_CASE_ONLY.getCcdType();

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public ServiceHearingValues getServiceHearingValues(ServiceHearingRequest request)
        throws GetCaseException, UpdateCaseException, ListingException, JsonProcessingException {

        String originalCaseData;
        ServiceHearingValues model;
        String updatedCaseData;

        if (isV2ServiceHearingServiceEnabled) {
            Long caseId = Long.valueOf(request.getCaseId());

            log.info("UpdateCaseV2 for caseId {} and eventType {}", caseId, EVENT_TYPE);
            final IdamTokens idamTokens = idamService.getIdamTokens();

            StartEventResponse startEventResponse = ccdClient.startEvent(idamTokens, caseId, EVENT_TYPE);
            SscsCaseData data = sscsCcdConvertService.getCaseData(startEventResponse.getCaseDetails().getData());

            /**
             * @see uk.gov.hmcts.reform.sscs.ccd.deserialisation.SscsCaseCallbackDeserializer#deserialize(String)
             * setCcdCaseId & sortCollections are called above, so this functionality has been replicated here preserving existing logic
             */
            data.setCcdCaseId(caseId.toString());
            data.sortCollections();

            originalCaseData = objectMapper.writeValueAsString(data);

            model = ServiceHearingValuesMapping.mapServiceHearingValues(data, refData);

            updatedCaseData = objectMapper.writeValueAsString(data);

            if (!originalCaseData.equals(updatedCaseData)) {
                CaseDataContent caseDataContent = sscsCcdConvertService.getCaseDataContent(data, startEventResponse, summary, description);

                sscsCcdConvertService.getCaseDetails(ccdClient.submitEventForCaseworker(idamTokens, caseId, caseDataContent));
            }
        } else {
            SscsCaseDetails caseDetails = ccdCaseService.getCaseDetails(request.getCaseId());

            SscsCaseData caseData = caseDetails.getData();
            originalCaseData = objectMapper.writeValueAsString(caseData);

            model = ServiceHearingValuesMapping.mapServiceHearingValues(caseData, refData);

            updatedCaseData = objectMapper.writeValueAsString(caseData);

            if (!originalCaseData.equals(updatedCaseData)) {
                log.debug("Updating case data with Service Hearing Values for Case ID {}", caseData.getCcdCaseId());
                ccdCaseService.updateCaseData(
                    caseData,
                    EventType.UPDATE_CASE_ONLY,
                    summary,
                    description);
            }
        }

        return model;
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
