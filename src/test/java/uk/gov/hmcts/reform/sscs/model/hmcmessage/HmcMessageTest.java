package uk.gov.hmcts.reform.sscs.model.hmcmessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.sscs.ResourceLoader;

public class HmcMessageTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testJson() throws Exception {
        String jsonResource = ResourceLoader.loadJson("hmcMessageValues.json");
        HmcMessage message = mapper.readValue(jsonResource, HmcMessage.class);
        String actual = "DRAFT";
        Assertions.assertEquals(actual, message.getHearingUpdate().getHearingListingStatus().toString());
    }
}
