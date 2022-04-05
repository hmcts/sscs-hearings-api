package uk.gov.hmcts.reform.sscs.helper;

import com.azure.messaging.servicebus.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
public class QueueHelper {
    public static void processError(ServiceBusErrorContext context, CountDownLatch countdownLatch) {
        log.error("Error when receiving messages from namespace: '{}'. Entity: '{}'%n",
                  context.getFullyQualifiedNamespace(), context.getEntityPath()
        );

        if (!(context.getException() instanceof ServiceBusException)) {
            log.warn("Non-ServiceBusException occurred: {}%n", context.getException().toString());
            return;
        }

        ServiceBusException exception = (ServiceBusException) context.getException();
        ServiceBusFailureReason reason = exception.getReason();

        if (Objects.equals(reason, ServiceBusFailureReason.MESSAGING_ENTITY_DISABLED)
            || Objects.equals(reason, ServiceBusFailureReason.MESSAGING_ENTITY_NOT_FOUND)
            || Objects.equals(reason, ServiceBusFailureReason.UNAUTHORIZED)) {
            log.error("An unrecoverable error occurred. Stopping processing with reason {}: {}%n",
                      reason, exception.getMessage()
            );
            countdownLatch.countDown();
        } else if (Objects.equals(reason, ServiceBusFailureReason.MESSAGE_LOCK_LOST)) {
            log.warn("Message lock lost for message: {}%n", context.getException().toString());
        } else if (Objects.equals(reason, ServiceBusFailureReason.SERVICE_BUSY)) {
            try {
                SECONDS.sleep(1);
            } catch (InterruptedException e) {
                log.warn("Unable to sleep for period of time");
            }
        } else {
            log.error("Error source {}, reason {}, message: {}%n", context.getErrorSource(),
                      reason, context.getException()
            );
        }
    }
}
