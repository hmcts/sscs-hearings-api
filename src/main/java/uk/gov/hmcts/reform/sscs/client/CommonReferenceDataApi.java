package uk.gov.hmcts.reform.sscs.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.sscs.domain.model.CaseFlag;
import uk.gov.hmcts.reform.sscs.domain.model.Categories;
import uk.gov.hmcts.reform.sscs.domain.model.CategoryRequest;

import java.util.Optional;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@FeignClient(
    name = "common-ref-data-api",
    url = "${common-ref.api.url}"
)
public interface CommonReferenceDataApi {
    String SERVICE_AUTHORIZATION = "serviceAuthorization";

    @RequestMapping(
        method = RequestMethod.GET,
        value = {"refdata/commondata/lov/categories", "refdata/commondata/lov/categories/{categoryId}"},
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    ResponseEntity<Categories> retrieveListOfValuesByCategoryId(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @PathVariable(value = "categoryId") Optional<String> categoryId,
        CategoryRequest categoryRequest
    );


    @RequestMapping(
        method = RequestMethod.GET,
        value = {"refdata/commondata/caseflags", "refdata/commondata/caseflags/service-id={service-id}"},
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    ResponseEntity<CaseFlag> retrieveCaseFlagsByServiceId(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @PathVariable(value = "service-id") String serviceId,
        @RequestParam(value = "flag-type", required = false) String flagType,
        @RequestParam(value = "welsh-required", required = false) String welshRequired
    );
}
