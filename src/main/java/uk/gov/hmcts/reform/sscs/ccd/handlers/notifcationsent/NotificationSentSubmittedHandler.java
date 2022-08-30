package uk.gov.hmcts.reform.sscs.ccd.handlers.notifcationsent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.callback.Callback;
import uk.gov.hmcts.reform.sscs.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.NotificationResponse;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.helper.SscsUtil;
import uk.gov.hmcts.reform.sscs.ccd.model.CallbackHandler;
import uk.gov.hmcts.reform.sscs.ccd.model.CallbackResponse;

import java.util.List;
import javax.validation.Valid;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSentSubmittedHandler implements CallbackHandler<SscsCaseData> {
    private final List<EventType> callbackEventsHandled = List.of(
        EventType.NOTIFICATION_SENT,
        EventType.LETTER_NOTIFICATION_SENT,
        EventType.EMAIL_NOTIFICATION_SENT,
        EventType.SMS_NOTIFICATION_SENT);

    private final List<EventType> notificationEventsHandled = List.of(
        EventType.HEARING_BOOKED);

    @Override
    public boolean canHandle(CallbackType callbackType, @Valid Callback<SscsCaseData> callback) {
        requireNonNull(callback, "callback must not be null");
        requireNonNull(callbackType, "callbacktype must not be null");

        return callbackType == CallbackType.SUBMITTED
            && SscsUtil.isValidEventType(callback.getEvent(), callbackEventsHandled)
            && SscsUtil.isSAndLCase(callback);
    }

    @Override
    public CallbackResponse<SscsCaseData> handle(CallbackType callbackType, @Valid Callback<SscsCaseData> callback) {
        final SscsCaseData caseData = callback.getCaseDetails().getCaseData();

        String caseId = caseData.getCcdCaseId();

        final CallbackResponse<SscsCaseData> response = new CallbackResponse<>(caseData);

        EventType notificationEvent = caseData.getNotificationResponse().getEvent();
        if (SscsUtil.isValidEventType(notificationEvent, notificationEventsHandled)) {
            log.info("Notification Sent Submitted: handling notification Event {} for Case Id {}",
                notificationEvent, caseId);

            //do the thing
        }

        clearTransientFields(caseData);

        return response;
    }

    private void clearTransientFields(SscsCaseData caseData) {
        caseData.setNotificationResponse(NotificationResponse.builder().build());
    }
}
