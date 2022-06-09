package uk.gov.hmcts.reform.sscs.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import feign.jackson.JacksonDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.sscs.service.AppInsightsService;
import uk.gov.hmcts.reform.sscs.service.exceptions.FeignClientErrorDecoder;

public class FeignClientConfig {

    private final AppInsightsService appInsightsService;

    public FeignClientConfig(AppInsightsService appInsightsService) {
        this.appInsightsService = appInsightsService;
    }

    @Bean
    @Primary
    Decoder feignDecoder(ObjectMapper objectMapper) {
        objectMapper.registerModule(new JavaTimeModule());
        return new JacksonDecoder(objectMapper);
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignClientErrorDecoder(appInsightsService);
    }

    @Bean
    public Retryer retryer(@Value("${feign.client.retryer.period}") long period,
                           @Value("${feign.client.retryer.maxPeriod}") long maxPeriod,
                           @Value("${feign.client.retryer.maxAttempts}") int maxAttempts) {
        return new Retryer.Default(period, maxPeriod, maxAttempts);
    }

}
