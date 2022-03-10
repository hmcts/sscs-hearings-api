package uk.gov.hmcts.reform.sscs;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.sscs.client.CommonReferenceDataApi;
import org.apache.http.HttpHeaders;
import uk.gov.hmcts.reform.sscs.domain.model.CategoryRequest;
import java.util.Optional;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "common-ref-data-api", port = "8888")
@SpringBootTest({
    // overriding provider address
    // "citizenservice.ribbon.listOfServers: localhost:8888"
})
public class CommonReferenceDataApiConsumerTest {
    @Autowired
    private CommonReferenceDataApi commonReferenceDataApi;

    private static final String AUTH_TOKEN = "Bearer someAuthorizationToken";
    private static final Optional<String> categoryId = Optional.of("1");
    private static final CategoryRequest  categoryRequest = CategoryRequest.builder().build();

    @Pact(provider = "common-ref-data-api", consumer = "common-ref-data-client")
    RequestResponsePact retrieveListOfValuesByCategoryIdPact(PactDslWithProvider builder) {
        // @formatter:off
        return builder
            .given("category id is provided to retrieve list of categories")
            .uponReceiving("a request to GET list of categories")
            .path("refdata/commondata/lov/categories")
            .path("refdata/commondata/lov/categories/1")
            .method("GET")
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .matchHeader(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
            .body(createCategoriesResponse())
            .toPact();
    }

    private PactDslJsonBody createCategoriesResponse() {

        return new PactDslJsonBody()
            .stringType("categoryKey", "cat-child")
            .stringValue("serviceId", "16")
            .stringValue("key", "13")
            .stringValue("valueEn", "en")
            .stringValue("valueCy", "cy")
            .stringValue("hintTextEn", "en-text")
            .stringValue("lovOrder", "asc")
            .stringValue("parentCategory", "cat-parent")
            .stringValue("parentKey", "12")
            .stringValue("activeFlag", "true");
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragment")
    public void verifyPactResponse() {
        var categories = commonReferenceDataApi.retrieveListOfValuesByCategoryId(HttpHeaders.AUTHORIZATION, AUTH_TOKEN, categoryId, categoryRequest);
        Assertions.assertEquals(1, categories.getBody().getListOfCategory().size());
        Assertions.assertEquals("cat-child", categories.getBody().getListOfCategory().get(0).getCategoryKey());
        Assertions.assertEquals("16", categories.getBody().getListOfCategory().get(0).getServiceId());
        Assertions.assertEquals("13", categories.getBody().getListOfCategory().get(0).getKey());
        Assertions.assertEquals("en", categories.getBody().getListOfCategory().get(0).getValueEn());
        Assertions.assertEquals("cy", categories.getBody().getListOfCategory().get(0).getValueCy());
        Assertions.assertEquals("en-text", categories.getBody().getListOfCategory().get(0).getHintTextEn());
        Assertions.assertEquals("asc", categories.getBody().getListOfCategory().get(0).getLovOrder());
        Assertions.assertEquals("cat-parent", categories.getBody().getListOfCategory().get(0).getParentCategory());
        Assertions.assertEquals("12", categories.getBody().getListOfCategory().get(0).getParentKey());
        Assertions.assertEquals("true", categories.getBody().getListOfCategory().get(0).getActiveFlag());
    }
}
