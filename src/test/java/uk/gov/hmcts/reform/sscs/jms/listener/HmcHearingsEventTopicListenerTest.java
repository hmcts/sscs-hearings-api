package uk.gov.hmcts.reform.sscs.jms.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.qpid.jms.message.JmsBytesMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.sscs.exception.CaseException;
import uk.gov.hmcts.reform.sscs.exception.HmcEventProcessingException;
import uk.gov.hmcts.reform.sscs.exception.MessageProcessingException;
import uk.gov.hmcts.reform.sscs.model.hmc.message.HearingUpdate;
import uk.gov.hmcts.reform.sscs.model.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.sscs.service.hmc.topic.ProcessHmcMessageService;

import java.nio.charset.StandardCharsets;
import javax.jms.JMSException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.ADJOURNED;


@ExtendWith(MockitoExtension.class)
class HmcHearingsEventTopicListenerTest {

    public static final String SERVICE_CODE = "BBA3";

    private HmcHearingsEventTopicListener hmcHearingsEventTopicListener;

    @Mock
    private ProcessHmcMessageService processHmcMessageService;

    @Mock
    private JmsBytesMessage bytesMessage;

    @Mock
    private ObjectMapper mockObjectMapper;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @BeforeEach
    void setup() {
        hmcHearingsEventTopicListener = new HmcHearingsEventTopicListener(SERVICE_CODE, processHmcMessageService);
        ReflectionTestUtils.setField(hmcHearingsEventTopicListener, "objectMapper", mockObjectMapper);
    }

    @Test
    @DisplayName("Messages should not be processed if their service code does not match the service.")
    void testOnMessage_serviceCodeNotApplicable() throws Exception {

        HmcMessage hmcMessage = createHmcMessage("BBA4");

        byte[] messageBytes = OBJECT_MAPPER.writeValueAsString(hmcMessage).getBytes(StandardCharsets.UTF_8);

        given(bytesMessage.getBodyLength()).willReturn((long) messageBytes.length);
        given(mockObjectMapper.readValue(any(String.class), eq(HmcMessage.class))).willReturn(hmcMessage);

        hmcHearingsEventTopicListener.onMessage(bytesMessage);

        verify(processHmcMessageService, never()).processEventMessage((any(HmcMessage.class)));
    }

    @Test
    @DisplayName("Messages should be processed if their service code matches the service.")
    void testOnMessage_serviceCodeApplicable() throws Exception {

        HmcMessage hmcMessage = createHmcMessage(SERVICE_CODE);

        byte[] messageBytes = OBJECT_MAPPER.writeValueAsString(hmcMessage).getBytes(StandardCharsets.UTF_8);

        given(bytesMessage.getBodyLength()).willReturn((long) messageBytes.length);
        given(mockObjectMapper.readValue(any(String.class), eq(HmcMessage.class))).willReturn(hmcMessage);

        hmcHearingsEventTopicListener.onMessage(bytesMessage);

        verify(processHmcMessageService).processEventMessage((any(HmcMessage.class)));
    }

    @Test
    @DisplayName("A HmcEventProcessingException should be thrown if a JsonProcessing exception is encountered.")
    void testOnMessage_JsonProcessingException() throws JsonProcessingException, JMSException {

        HmcMessage hmcMessage = createHmcMessage(SERVICE_CODE);

        byte[] messageBytes = OBJECT_MAPPER.writeValueAsString(hmcMessage).getBytes(StandardCharsets.UTF_8);

        given(bytesMessage.getBodyLength()).willReturn((long) messageBytes.length);
        given(mockObjectMapper.readValue(any(String.class), eq(HmcMessage.class)))
            .willThrow(JsonProcessingException.class);

        assertThatExceptionOfType(HmcEventProcessingException.class)
            .isThrownBy(() -> hmcHearingsEventTopicListener.onMessage(bytesMessage))
            .withCauseInstanceOf(JsonProcessingException.class);
    }

    @Test
    @DisplayName("A HmcEventProcessingException exception should be thrown an exception is encountered.")
    void testOnMessage_HmcEventProcessingException()
            throws JsonProcessingException, JMSException, CaseException, MessageProcessingException {

        HmcMessage hmcMessage = createHmcMessage(SERVICE_CODE);

        byte[] messageBytes = OBJECT_MAPPER.writeValueAsString(hmcMessage).getBytes(StandardCharsets.UTF_8);

        given(bytesMessage.getBodyLength()).willReturn((long) messageBytes.length);
        given(mockObjectMapper.readValue(any(String.class), eq(HmcMessage.class))).willReturn(hmcMessage);

        doThrow(MessageProcessingException.class)
            .when(processHmcMessageService)
            .processEventMessage(hmcMessage);

        assertThatExceptionOfType(HmcEventProcessingException.class)
            .isThrownBy(() -> hmcHearingsEventTopicListener.onMessage(bytesMessage))
            .withCauseInstanceOf(MessageProcessingException.class);
    }

    private HmcMessage createHmcMessage(String messageServiceCode) {
        return HmcMessage.builder()
            .hmctsServiceCode(messageServiceCode)
            .hearingId("testId")
            .caseId(1234L)
            .hearingUpdate(HearingUpdate.builder()
                    .hmcStatus(ADJOURNED)
                    .build())
            .build();
    }
}
