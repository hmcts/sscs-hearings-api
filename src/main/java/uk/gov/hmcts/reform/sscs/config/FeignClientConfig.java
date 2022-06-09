package uk.gov.hmcts.reform.sscs.config;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
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
    public Decoder feignDecoder() {
        return new JacksonDecoder(objectMapper());
    }

    private ObjectMapper objectMapper() {
        JsonMapper objectMapper = new JsonMapper();

        return new JsonMapper.Builder(objectMapper)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            .addModule(new JavaTimeModule())
            .build();
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
