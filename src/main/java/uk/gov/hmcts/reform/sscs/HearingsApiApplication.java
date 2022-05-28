package uk.gov.hmcts.reform.sscs;

import com.microsoft.applicationinsights.web.internal.ApplicationInsightsServletContextListener;
import okhttp3.OkHttpClient;
import org.apache.qpid.jms.JmsConnectionFactory;
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
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.sscs.ccd.config.CcdRequestDetails;
import uk.gov.hmcts.reform.sscs.helper.servicebus.ConnectionStringResolver;
import uk.gov.hmcts.reform.sscs.model.servicebus.ServiceBusKey;
import uk.gov.hmcts.reform.sscs.service.servicebus.CustomMessageConverter;

import java.util.concurrent.TimeUnit;
import javax.jms.ConnectionFactory;
import javax.servlet.ServletContextListener;

@SpringBootApplication
@EnableScheduling
@EnableJms
@ComponentScan(basePackages = {"uk.gov.hmcts.reform"})
@EnableJms
@EnableFeignClients(basePackages =
    {
        "uk.gov.hmcts.reform.sscs.service",
        "uk.gov.hmcts.reform.authorisation",
        "uk.gov.hmcts.reform.sscs.idam",
        "uk.gov.hmcts.reform.sscs.service",
        "uk.gov.hmcts.reform.idam"
    })
public class HearingsApiApplication {

    @Value("${spring.jms.servicebus.connection-string}")
    private String connectionString;

    @Value("${spring.jms.servicebus.topic-client-id}")
    private String clientId;

    @Value("${spring.jms.servicebus.idle-timeout}")
    private int idleTimeout;

    private static final String AMQP_URI_FORMAT = "amqps://%s?amqp.idleTimeout=%d";


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



    @Bean
    public ConnectionFactory myConnectionFactory() {
        ServiceBusKey serviceBusKey = ConnectionStringResolver.getServiceBusKey(connectionString);
        String host = serviceBusKey.getHost();
        String sasKeyName = serviceBusKey.getSharedAccessKeyName();
        String sasKey = serviceBusKey.getSharedAccessKey();

        String remoteUri = String.format(AMQP_URI_FORMAT, host, idleTimeout);
        JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory();
        jmsConnectionFactory.setRemoteURI(remoteUri);
        jmsConnectionFactory.setClientID(clientId);
        jmsConnectionFactory.setUsername(sasKeyName);
        jmsConnectionFactory.setPassword(sasKey);
        return new CachingConnectionFactory(jmsConnectionFactory);
    }

    @Bean
    public JmsListenerContainerFactory<?> myTopicFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory topicFactory = new DefaultJmsListenerContainerFactory();
        topicFactory.setConnectionFactory(connectionFactory);
        topicFactory.setSubscriptionDurable(Boolean.TRUE);
        topicFactory.setMessageConverter(new CustomMessageConverter());
        return topicFactory;
    }


}
