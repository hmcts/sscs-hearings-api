package uk.gov.hmcts.reform.sscs.config;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Getter
@ConditionalOnProperty("flags.tribunals-to-hearings-api.enabled")
@Configuration
public class TribunalsHearingEventQueueProperties {

    @Value("${azure.service-bus.tribunals-to-hearings-api.connectionString}")
    private String connectionString;
    @Value("${azure.service-bus.tribunals-to-hearings-api.queueName}")
    private String queueName;

    @Value("${azure.service-bus.tribunals-to-hearings-api.retryTimeout}")
    private Long retryTimeout;
    @Value("${azure.service-bus.tribunals-to-hearings-api.retryDelay}")
    private Long retryDelay;
    @Value("${azure.service-bus.tribunals-to-hearings-api.maxRetries}")
    private Integer maxRetries;

    AmqpRetryOptions retryOptions() {
        AmqpRetryOptions amqpRetryOptions = new AmqpRetryOptions();
        amqpRetryOptions.setMode(AmqpRetryMode.EXPONENTIAL);
        amqpRetryOptions.setTryTimeout(Duration.ofMinutes(getRetryTimeout()));
        amqpRetryOptions.setMaxRetries(getMaxRetries());
        amqpRetryOptions.setDelay(Duration.ofSeconds(getRetryDelay()));

        return amqpRetryOptions;
    }

}
