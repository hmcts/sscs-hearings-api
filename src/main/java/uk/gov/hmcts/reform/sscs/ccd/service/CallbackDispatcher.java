package uk.gov.hmcts.reform.sscs.ccd.service;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.ccd.callback.Callback;
import uk.gov.hmcts.reform.sscs.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.sscs.ccd.domain.CaseData;
import uk.gov.hmcts.reform.sscs.ccd.model.CallbackHandler;
import uk.gov.hmcts.reform.sscs.ccd.model.CallbackResponse;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Component
public class CallbackDispatcher<T extends CaseData> {

    private final List<CallbackHandler<T>> callbackHandlers;

    public CallbackDispatcher(List<CallbackHandler<T>> callbackHandlers) {
        requireNonNull(callbackHandlers, "callbackHandlers must not be null");
        this.callbackHandlers = callbackHandlers;
    }

    public CallbackResponse<T> handle(CallbackType callbackType, Callback<T> callback) {

        requireNonNull(callback, "callback must not be null");
        T caseData = callback.getCaseDetails().getCaseData();

        CallbackResponse<T> callbackResponse = new CallbackResponse<>(caseData);

        dispatchToHandlers(callbackType, callback, callbackHandlers, callbackResponse);

        return callbackResponse;
    }

    private void dispatchToHandlers(CallbackType callbackType, Callback<T> callback,
                                    List<CallbackHandler<T>> callbackHandlers,
                                    CallbackResponse<T> callbackResponse) {

        for (CallbackHandler<T> callbackHandler : callbackHandlers) {
            if (callbackHandler.canHandle(callbackType, callback)) {

                CallbackResponse<T> callbackResponseFromHandler = callbackHandler.handle(callbackType, callback);

                callbackResponse.setData(callbackResponseFromHandler.getData());
                callbackResponse.addErrors(callbackResponseFromHandler.getErrors());
                callbackResponse.addWarnings(callbackResponseFromHandler.getWarnings());
            }
        }
    }
}
