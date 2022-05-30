package uk.gov.hmcts.reform.sscs.service.hmc.topic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.sscs.exception.GetCaseException;
import uk.gov.hmcts.reform.sscs.exception.GetHearingException;
import uk.gov.hmcts.reform.sscs.exception.InvalidHmcMessageException;
import uk.gov.hmcts.reform.sscs.exception.InvalidIdException;
import uk.gov.hmcts.reform.sscs.exception.UpdateCaseException;
import uk.gov.hmcts.reform.sscs.model.hmc.message.HearingUpdate;
import uk.gov.hmcts.reform.sscs.model.hmc.message.HmcMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.HmcStatus.UPDATE_REQUESTED;
import static uk.gov.hmcts.reform.sscs.model.hmc.reference.ListAssistCaseStatus.LISTED;

@ExtendWith(MockitoExtension.class)
class CheckMessageServiceTest {

    public static final String SSCS_SERVICE_CODE = "BBA3";

    @Mock
    private ProcessMessageService processMessageService;

    @InjectMocks
    private CheckMessageService checkMessageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(checkMessageService, "sscsServiceCode", SSCS_SERVICE_CODE);
    }

    @DisplayName("When the service code of a message matches the correct this services code "
        + "isMessageRelevantForService returns false")
    @Test
    void testMessageRelevantForService() {
        HmcMessage hmcMessage = HmcMessage.builder()
            .hmctsServiceCode(SSCS_SERVICE_CODE)
            .build();

        boolean result = checkMessageService.isMessageNotRelevantForService(hmcMessage);

        assertThat(result)
            .as("This message does not have the correct service ID.")
            .isFalse();
    }

    @DisplayName("When the service code of a message does not match this service's code "
        + "isMessageRelevantForService returns true")
    @ParameterizedTest
    @ValueSource(strings = {"PP4","SSA1"})
    @EmptySource
    void testMessageRelevantForService(String value) {
        HmcMessage hmcMessage = HmcMessage.builder()
            .hmctsServiceCode(value)
            .build();

        boolean result = checkMessageService.isMessageNotRelevantForService(hmcMessage);

        assertThat(result)
            .as("This service ID should not of matched.")
            .isTrue();
    }

    @Test
    void testValidateHmcMessageshouldThrowExceptionIfHmcMessageIsMissing() {

        // when + then
        assertThatExceptionOfType(InvalidHmcMessageException.class)
                .isThrownBy(() -> checkMessageService.validateHmcMessage(null))
                .withMessageContaining("HMC message must not be mull");
    }

    @Test
    void testValidateHmcMessageshouldThrowExceptionIfHearingIdIsMissing() {
        // given
        HmcMessage hmcMessage = HmcMessage.builder().build();

        // when + then
        assertThatExceptionOfType(InvalidHmcMessageException.class)
                .isThrownBy(() -> checkMessageService.validateHmcMessage(hmcMessage))
                .withMessageContaining("HMC message field hearingID is missing");
    }

    @Test
    void testValidateHmcMessageshouldThrowExceptionIfHearingUpdateIsMissing() {
        // given
        HmcMessage hmcMessage = HmcMessage.builder()
                .hearingId("1234")
                .build();

        // when + then
        assertThatExceptionOfType(InvalidHmcMessageException.class)
                .isThrownBy(() -> checkMessageService.validateHmcMessage(hmcMessage))
                .withMessageContaining("HMC message field HearingUpdate is missing");
    }

    @Test
    void testValidateHmcMessageshouldThrowExceptionIfHmcStatusIsMissing() {
        // given
        HmcMessage hmcMessage = HmcMessage.builder()
                .hearingId("1234")
                .hearingUpdate(HearingUpdate.builder().build())
                .build();

        // when + then
        assertThatExceptionOfType(InvalidHmcMessageException.class)
                .isThrownBy(() -> checkMessageService.validateHmcMessage(hmcMessage))
                .withMessageContaining("HMC message field HmcStatus is missing");
    }

    @Test
    void testValidateHmcMessageShouldNowThrowExceptionIfHmcMessageIsValid() {
        // given
        HmcMessage hmcMessage = HmcMessage.builder()
                .hearingId("1234")
                .hearingUpdate(HearingUpdate.builder()
                        .hmcStatus(UPDATE_REQUESTED)
                        .build())
                .build();

        // when + then
        assertThatNoException()
                .isThrownBy(() -> checkMessageService.validateHmcMessage(hmcMessage));
    }

    @DisplayName("When hmcMessage is valid and relevant to the service, no error is thrown and processEventMessage is called")
    @Test
    void testCheckMessage()
            throws GetHearingException, UpdateCaseException, InvalidHmcMessageException, GetCaseException, InvalidIdException {
        // given
        HmcMessage hmcMessage = HmcMessage.builder()
                .hearingId("1234")
                .hearingUpdate(HearingUpdate.builder()
                        .hmcStatus(UPDATE_REQUESTED)
                        .listAssistCaseStatus(LISTED)
                        .build())
                .hmctsServiceCode("BBA3")
                .build();

        willDoNothing().given(processMessageService).processEventMessage(hmcMessage);

        // when
        assertThatNoException()
                .isThrownBy(() -> checkMessageService.checkMessage(hmcMessage));

        // then
        verify(processMessageService, times(1)).processEventMessage(hmcMessage);
    }

    @DisplayName("When hmcMessage is invalid, the correct error is thrown")
    @Test
    void testCheckMessageNullHmcMessage()
            throws GetHearingException, UpdateCaseException, InvalidHmcMessageException, GetCaseException, InvalidIdException {
        // given + when
        assertThatExceptionOfType(InvalidHmcMessageException.class)
                .isThrownBy(() -> checkMessageService.validateHmcMessage(null));

        // then
        verify(processMessageService, never()).processEventMessage(any());
    }

    @DisplayName("When hmcMessage is valid but message not relevant, no error is thrown but no call to processEventMessage is made")
    @Test
    void testCheckMessageWrongServiceCode()
            throws GetHearingException, UpdateCaseException, InvalidHmcMessageException, GetCaseException, InvalidIdException {
        // given
        HmcMessage hmcMessage = HmcMessage.builder()
                .hearingId("1234")
                .hearingUpdate(HearingUpdate.builder()
                        .hmcStatus(UPDATE_REQUESTED)
                        .listAssistCaseStatus(LISTED)
                        .build())
                .hmctsServiceCode("SBC3")
                .build();

        // when
        assertThatNoException()
                .isThrownBy(() -> checkMessageService.checkMessage(hmcMessage));

        // then
        verify(processMessageService, never()).processEventMessage(any());
    }

}
