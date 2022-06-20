package uk.gov.hmcts.reform.sscs.service;

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

    public ServiceHearingValues getServiceHearingValues(ServiceHearingRequest request)
            throws GetCaseException, UpdateCaseException, InvalidMappingException {
        SscsCaseDetails caseDetails = ccdCaseService.getCaseDetails(request.getCaseId());
        HearingsMapping.updateIds(caseDetails.getData());
        ServiceHearingValues model = ServiceHearingValuesMapping.mapServiceHearingValues(caseDetails, referenceDataServiceHolder);
        ccdCaseService.updateCaseData(
                caseDetails.getData(), EventType.UPDATE_CASE_ONLY,
                "Updating caseDetails IDs",
                "IDs updated for caseDetails due to ServiceHearingValues request");
        return model;
    }

    public ServiceLinkedCases getServiceLinkedCases(ServiceHearingRequest request)
        throws GetCaseException, InvalidMappingException {

        SscsCaseData caseData = ccdCaseService.getCaseDetails(request.getCaseId()).getData();
        SscsCaseDetails caseDetails = ccdCaseService.getCaseDetails(request.getCaseId());

        List<LinkedCase> linkedCases = LinkedCasesMapping.getLinkedCases(caseData);

        return ServiceLinkedCases.builder()
                .linkedCases(linkedCases)
                .build();
    }

}
