package uk.gov.hmcts.reform.sscs.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.InvalidMappingException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.helper.mapping.HearingsMapping;
import uk.gov.hmcts.reform.sscs.helper.mapping.LinkedCasesMapping;
import uk.gov.hmcts.reform.sscs.helper.mapping.ServiceHearingValuesMapping;
import uk.gov.hmcts.reform.sscs.model.service.ServiceHearingRequest;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.ServiceHearingValues;
import uk.gov.hmcts.reform.sscs.model.service.linkedcases.LinkedCase;
import uk.gov.hmcts.reform.sscs.model.service.linkedcases.ServiceLinkedCases;
import uk.gov.hmcts.reform.sscs.service.holder.ReferenceDataServiceHolder;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceHearingsService {

    private final CcdCaseService ccdCaseService;

    private final ReferenceDataServiceHolder referenceDataServiceHolder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ServiceHearingValues getServiceHearingValues(ServiceHearingRequest request)
        throws GetCaseException, UpdateCaseException, InvalidMappingException, JsonProcessingException {
        SscsCaseDetails caseDetails = ccdCaseService.getCaseDetails(request.getCaseId());

        SscsCaseData caseData = caseDetails.getData();
        String originalCaseData = objectMapper.writeValueAsString(caseData);

        HearingsMapping.updateIds(caseData);
        ServiceHearingValues model = ServiceHearingValuesMapping.mapServiceHearingValues(caseData, referenceDataServiceHolder);

        String updatedCaseData = objectMapper.writeValueAsString(caseData);

        if (!originalCaseData.equals(updatedCaseData)) {
            ccdCaseService.updateCaseData(
                caseData,
                EventType.UPDATE_CASE_ONLY,
                "Updating caseDetails IDs",
                "IDs updated for caseDetails due to ServiceHearingValues request");
        }

        return model;
    }

    public ServiceLinkedCases getServiceLinkedCases(ServiceHearingRequest request)
        throws GetCaseException, InvalidMappingException {

        SscsCaseData caseData = ccdCaseService.getCaseDetails(request.getCaseId()).getData();

        List<LinkedCase> linkedCases = LinkedCasesMapping.getLinkedCases(caseData);

        return ServiceLinkedCases.builder()
                .linkedCases(linkedCases)
                .build();
    }
}
