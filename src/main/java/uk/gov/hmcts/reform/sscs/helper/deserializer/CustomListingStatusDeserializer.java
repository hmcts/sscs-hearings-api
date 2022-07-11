package uk.gov.hmcts.reform.sscs.helper.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import uk.gov.hmcts.reform.sscs.model.hmc.reference.ListingStatus;

import java.io.IOException;

public class CustomListingStatusDeserializer extends StdDeserializer<ListingStatus> {

    public CustomListingStatusDeserializer() {
        super(ListingStatus.class);
    }

    @Override
    public ListingStatus deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        String label = node.get("label").asText();

        for (ListingStatus listingStatus : ListingStatus.values()) {
            if (listingStatus.getLabel().equals(label)) {
                return listingStatus;
            }
        }
        return ListingStatus.NONE;
    }
}
