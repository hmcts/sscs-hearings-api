package uk.gov.hmcts.reform.sscs.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class HmcHearingValidator {

    private static final String MISSING_FIELD_MESSAGE = " is missing/not populated - please correct.";
    private static final String MALFORMED_FIELD_MESSAGE = " is invalid - please correct.";
    private final String schemaResourceLocation;
    private JsonSchema schema = null;

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public HmcHearingValidator(@Value("${hmc.schema.resource.location:/schema/sscs-hmc.json}") String schemaResourceLocation) {
        this.schemaResourceLocation = schemaResourceLocation;
    }

    public Set<String> validate(JSONObject hmcJson, String caseId) {

        Set<String> errorSet = new HashSet<>();
        if (schema == null) {
            schema = prepareSchema(schemaResourceLocation);
        }

        try {

            JsonNode jsonNode = toJsonNode(hmcJson);
            Set<ValidationMessage> validationErrors = validateAgainstSchema(jsonNode);

            if (!CollectionUtils.isEmpty(validationErrors)) {
                errorSet.addAll(getErrors(validationErrors, caseId));
            }
        } catch (JsonProcessingException jsonTransformException) {
            errorSet.add("hmc service failed to transform json");
        }

        return errorSet;
    }

    protected JsonSchema prepareSchema(String name) {

        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        InputStream inputStream = getClass().getResourceAsStream(name);
        return factory.getSchema(inputStream);
    }

    public JsonNode toJsonNode(JSONObject jsonObj) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(jsonObj.toString());
    }

    public Set<ValidationMessage>  validateAgainstSchema(JsonNode node) {
        Set<ValidationMessage> errors = schema.validate(node);
        return errors;
    }

    private Set<String> getErrors(Set<ValidationMessage> validationErrors, String caseId) {
        Set<String> errorSet = new HashSet<>();
        for (ValidationMessage validationError : validationErrors) {

            log.error("Unable to validate hmc json for case id" + caseId + " with error: "
                    + validationError.getMessage() + " and type " + validationError.getType() + " and code "
                    + validationError.getCode());
            errorSet.add(toCcdError(validationError));
        }
        return errorSet;
    }

    private String toCcdError(ValidationMessage error) {

        String ccdError;

        String errorType = error.getType();
        String path = toPath(error.getPath());

        if (errorType.equals("required")) {

            ccdError = requiredErrorMessage(error, path);

        } else if (errorType.equals("minLength")) {
            ccdError = path + MISSING_FIELD_MESSAGE;

        } else if (errorType.equals("pattern")) {
            ccdError = path + MALFORMED_FIELD_MESSAGE;
        } else {
            ccdError = "An unexpected error has occurred. Please raise a ServiceNow ticket"
                    + " - the following field has caused the issue: " + path;
        }

        return ccdError;
    }


    private String requiredErrorMessage(ValidationMessage error, String path) {
        String ccdError;
        if (error.getArguments().length > 0) {
            String field = error.getArguments()[0];
            if (path.length() > 0) {
                ccdError = path + "." + field + MISSING_FIELD_MESSAGE;
            } else {
                ccdError = field + MISSING_FIELD_MESSAGE;
            }
        } else {
            ccdError = path + MISSING_FIELD_MESSAGE;
        }
        return ccdError;
    }

    private String toPath(String input) {
        if (input != null && input.length() > 2) {
            return input.substring(2);
        } else {
            return  "";
        }
    }

}
