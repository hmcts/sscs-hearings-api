package uk.gov.hmcts.reform.sscs.model.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.sscs.ResourceLoader;

import java.util.Set;
import uk.gov.hmcts.reform.sscs.model.service.hearingvalues.ServiceHearingValues;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceHearingValuesTest {

    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void shouldSerializeToPoJoSuccessfully() throws Exception {

        String actualJson = ResourceLoader.loadJson("serviceHearingValues.json");
        ServiceHearingValues serializedObject = mapper.readValue(actualJson, ServiceHearingValues.class);
        String deserializedJson = mapper.writeValueAsString(serializedObject);

        // assert all values are preserved after serializing/deserializing ignoring node order
        assertEquals(mapper.readTree(actualJson), mapper.readTree(deserializedJson));
    }


    @Test
    void givenValidJsonWhenValidateAgainstSchemaThenValidationPass() throws Exception {

        String json = ResourceLoader.loadJson("serviceHearingValues.json");
        String jsonSchema = ResourceLoader.loadJson("serviceHearingValuesSchema.json");

        JsonSchema schema = getJsonSchemaFromStringContent(jsonSchema);
        JsonNode node = getJsonNodeFromStringContent(json);
        Set<ValidationMessage> errors = schema.validate(node);
        assertThat(errors).isEmpty();
    }


    @Test
    void givenInvalidJsonWhenValidateAgainstSchemaThenRaiseError() throws Exception {

        String json = ResourceLoader.loadJson("serviceHearingValuesInvalid.json");
        String jsonSchema = ResourceLoader.loadJson("serviceHearingValuesSchema.json");

        JsonSchema schema = getJsonSchemaFromStringContent(jsonSchema);
        JsonNode node = getJsonNodeFromStringContent(json);
        Set<ValidationMessage> errors = schema.validate(node);
        assertThat(errors).hasSize(3);
    }

    private JsonSchema getJsonSchemaFromStringContent(String schemaContent) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        return factory.getSchema(schemaContent);
    }

    private JsonNode getJsonNodeFromStringContent(String content) throws JsonProcessingException {
        return mapper.readTree(content);
    }
}
