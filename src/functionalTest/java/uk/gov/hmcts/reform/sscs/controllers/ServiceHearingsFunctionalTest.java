package uk.gov.hmcts.reform.sscs.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.sscs.FunctionalTestBase;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.model.service.ServiceHearingRequest;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.ServiceHearingValues;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
class ServiceHearingsFunctionalTest extends FunctionalTestBase {
    @DisplayName("Test generating service hearing values for a case using the '/serviceHearingValues' endpoint")
    @Test
    public void testServiceHearingValues() throws Exception {
        SscsCaseDetails caseDetails = createCase();
        assertThat(caseDetails).isNotNull();

        String ccdCaseId = caseDetails.getData().getCcdCaseId();
        assertThat(ccdCaseId).isNotBlank();

        ServiceHearingRequest request = new ServiceHearingRequest();
        request.setCaseId(ccdCaseId);
        request.setHearingId(null);

        HttpHeaders headers = new HttpHeaders();
        headers.set("ServiceAuthorization", idamService.generateServiceAuthorization());
        headers.setContentType(APPLICATION_JSON);

        HttpEntity<ServiceHearingRequest> entity = new HttpEntity<>(request, headers);


        URI url = new URI(baseUrl + "/serviceHearingValues");
        ResponseEntity<ServiceHearingValues> response = restTemplate.exchange(
            url, POST, entity, ServiceHearingValues.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        ServiceHearingValues hearingValues = response.getBody();
        assertThat(hearingValues).isNotNull();
        // TODO Additional assertions to verify the returned values
    }
}
