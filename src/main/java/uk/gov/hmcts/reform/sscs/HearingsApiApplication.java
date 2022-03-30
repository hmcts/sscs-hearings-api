package uk.gov.hmcts.reform.sscs;

import com.microsoft.applicationinsights.web.internal.ApplicationInsightsServletContextListener;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.sscs.ccd.config.CcdRequestDetails;

import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContextListener;

@SpringBootApplication
@EnableScheduling
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
@ComponentScan(basePackages = {"uk.gov.hmcts.reform"})
@EnableFeignClients(basePackages =
    {
        "uk.gov.hmcts.reform.authorisation",
        "uk.gov.hmcts.reform.sscs.idam",
        "uk.gov.hmcts.reform.sscs.service",
        "uk.gov.hmcts.reform.idam"
    })
public class HearingsApiApplication {

    public static void main(final String[] args) {
        SpringApplication.run(HearingsApiApplication.class, args);
    }

    @Bean
    public OkHttpClient okHttpClient() {
        int timeout = 10;
        return new OkHttpClient.Builder()
            .connectTimeout(timeout, TimeUnit.MINUTES)
            .readTimeout(timeout, TimeUnit.MINUTES)
            .retryOnConnectionFailure(true)
            .build();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ServletListenerRegistrationBean<ServletContextListener> appInsightsServletContextListenerRegistrationBean(
        ApplicationInsightsServletContextListener applicationInsightsServletContextListener) {
        ServletListenerRegistrationBean<ServletContextListener> srb =
            new ServletListenerRegistrationBean<>();
        srb.setListener(applicationInsightsServletContextListener);
        return srb;
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationInsightsServletContextListener applicationInsightsServletContextListener() {
        return new ApplicationInsightsServletContextListener();
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource bean = new ReloadableResourceBundleMessageSource();
        bean.setBasename("classpath:application");
        bean.setDefaultEncoding("UTF-8");
        return bean;
    }

    @Bean
    public CcdRequestDetails getRequestDetails(
        @Value("${core_case_data.jurisdictionId}") String coreCaseDataJurisdictionId,
        @Value("${core_case_data.caseTypeId}") String coreCaseDataCaseTypeId) {
        return CcdRequestDetails.builder()
            .caseTypeId(coreCaseDataCaseTypeId)
            .jurisdictionId(coreCaseDataJurisdictionId)
            .build();
    }
}
