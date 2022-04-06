package uk.gov.hmcts.reform.sscs.model.hmcmessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.sscs.ResourceLoader;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class HmcMessageTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    private HmcMessage message;

    @BeforeEach
    public void setUp() throws Exception {
        String jsonResource = ResourceLoader.loadJson("hmcMessageValues.json");
        message = mapper.readValue(jsonResource, HmcMessage.class);
    }

    @Test
    void jsonShouldBeReadCorrectlyWhenEnumValueIsProvided() {
        String expected = "DRAFT";
        Assertions.assertEquals(expected, message.getHearingUpdate().getHearingListingStatus().toString());
    }

    @Test
    void stringDateForHearingEventBroadcastDateTimeShouldBeConvertedToLocalDateTimeWhenParsed() {
        LocalDateTime expected = LocalDateTime.of(2022, 4, 5, 11, 44, 13);
        Assertions.assertEquals(expected, message.getHearingUpdate().getHearingEventBroadcastDateTime());
    }

    @Test
    void stringDateForHearingResponseReceivedDateTimeShouldBeConvertedToLocalDateTimeWhenParsed() {
        LocalDateTime expected = LocalDateTime.of(2022, 4, 5, 11, 44, 13);
        Assertions.assertEquals(expected, message.getHearingUpdate().getHearingResponseReceivedDateTime());
    }

    @Test
    void stringDateForNextHearingDateShouldBeConvertedToLocalDateTimeWhenParsed() {
        LocalDate expected = LocalDate.of(2022, 4, 5);
        Assertions.assertEquals(expected, message.getHearingUpdate().getNextHearingDate());
    }
}
